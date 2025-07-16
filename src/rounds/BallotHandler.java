package rounds;

import model.NominationTracker;
import model.Team;
import model.RoleType;

import java.util.*;

public class BallotHandler {
    private final Scanner scanner = new Scanner(System.in);

// Ballot entry per-round. Tournament admin enters one ballot at a time per matchup. Total points & nominations are inputted.
    public void enterBallotsForRound(
            List<Matchup> roundMatchups,
            int numJudges,
            int numWitnesses,
            NominationTracker nominationTracker
    ) {
        System.out.println("Starting ballot entry for round...");
        for (Matchup matchup : roundMatchups) {
            System.out.println("\nEntering ballots for matchup: " +
                    matchup.plaintiffTeam.id + " vs " + matchup.defenseTeam.id);

            List<Ballot> ballots = new ArrayList<>();

            for (int judgeNum = 1; judgeNum <= numJudges; judgeNum++) {
                System.out.println("\nBallot " + judgeNum + " of " + numJudges);
                Ballot ballot = inputSingleBallot(matchup.plaintiffTeam, matchup.defenseTeam);

                ballots.add(ballot);

                // Optional: enter nominations per judge
                enterJudgeNominations(numWitnesses, nominationTracker);
            }

            matchup.ballots = ballots;

            System.out.println("Finished entering ballots for this matchup. Type 'next' to continue.");
            while (true) {
                String cmd = scanner.nextLine().trim().toLowerCase();
                if ("next".equals(cmd)) {
                    break;
                } else {
                    System.out.println("Type 'next' to move to the next matchup.");
                }
            }
        }

        System.out.println("Finished ballot entry for entire round.");
    }

    /**
     * Input a single ballot’s scores for plaintiff and defense.
     */
    public Ballot inputSingleBallot(Team plaintiff, Team defense) {
        System.out.println("Enter scores for matchup: " + plaintiff.id + " vs " + defense.id);
        int pScore = inputScore("Plaintiff score");
        int dScore = inputScore("Defense score");
        return new Ballot(plaintiff, defense, pScore, dScore);
    }

    /**
     * Input nominations for one judge.
     */
    public void enterJudgeNominations(int numWitnesses, NominationTracker nominationTracker) {
        int numRanks = Math.min(3, numWitnesses);

        System.out.println("Enter top " + numRanks + " ATTORNEYS for this judge (any team):");
        for (int i = 1; i <= numRanks; i++) {
            System.out.print(i + getOrdinalSuffix(i) + " place: ");
            String name = scanner.nextLine().trim();
            nominationTracker.addNomination(name, RoleType.ATTORNEY, i);
        }

        System.out.println("Enter top " + numRanks + " WITNESSES for this judge (any team):");
        for (int i = 1; i <= numRanks; i++) {
            System.out.print(i + getOrdinalSuffix(i) + " place: ");
            String name = scanner.nextLine().trim();
            nominationTracker.addNomination(name, RoleType.WITNESS, i);
        }
    }

    private int inputScore(String prompt) {
        int score = -1;
        while (score < 0) {
            try {
                System.out.print(prompt + ": ");
                String line = scanner.nextLine().trim();
                score = Integer.parseInt(line);
                if (score < 0) {
                    System.out.println("Score must be zero or positive. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a valid integer score.");
            }
        }
        return score;
    }

    private String getOrdinalSuffix(int number) {
        if (number >= 11 && number <= 13) return "th";
        return switch (number % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}

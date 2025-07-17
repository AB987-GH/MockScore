package rounds;

import model.NominationTracker;
import model.Student;
import model.Team;
import model.RoleType;
import model.Roster;
import model.RoleAssignment;

import java.util.*;

public class BallotHandler {
    private final Scanner scanner = new Scanner(System.in);

    public void enterBallotsForRound(
            List<Matchup> roundMatchups,
            int numJudges,
            int numWitnesses,
            NominationTracker nominationTracker
    ) {
        System.out.println("Starting ballot entry for round...");

        boolean[] completed = new boolean[roundMatchups.size()];
        int completedCount = 0;

        while (completedCount < roundMatchups.size()) {
            System.out.println("\nType the matchup number to enter ballots, or type 'viewmatchups' to view all matchups:");
            String input = scanner.nextLine().trim().toLowerCase();

            if ("viewmatchups".equals(input)) {
                printMatchupList(roundMatchups, completed);
                continue;
            }

            try {
                int index = Integer.parseInt(input) - 1;
                if (index < 0 || index >= roundMatchups.size()) {
                    System.out.println("Invalid matchup number.");
                    continue;
                }
                if (completed[index]) {
                    System.out.println("Ballots for this matchup have already been entered.");
                    continue;
                }

                Matchup matchup = roundMatchups.get(index);
                System.out.println("\nEntering ballots for matchup: " +
                        matchup.plaintiffTeam.id + " vs " + matchup.defenseTeam.id);

                printTeamRoster("Plaintiff", matchup.plaintiffTeam.plaintiffRoster);
                printTeamRoster("Defense", matchup.defenseTeam.defenseRoster);

                List<Ballot> ballots = new ArrayList<>();

                for (int judgeNum = 1; judgeNum <= numJudges; judgeNum++) {
                    System.out.println("\nBallot " + judgeNum + " of " + numJudges);
                    Ballot ballot = inputSingleBallot(matchup.plaintiffTeam, matchup.defenseTeam);
                    ballots.add(ballot);

                    enterJudgeNominations(numWitnesses, nominationTracker);
                }

                matchup.ballots = ballots;
                completed[index] = true;
                completedCount++;
                System.out.println("Finished entering ballots for this matchup.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Type a matchup number or 'viewmatchups'.");
            }
        }

        System.out.println("Finished ballot entry for entire round.");
    }

    private void printMatchupList(List<Matchup> matchups, boolean[] completed) {
        System.out.println("\nMatchups:");
        for (int i = 0; i < matchups.size(); i += 2) {
            StringBuilder line = new StringBuilder();
            Matchup m1 = matchups.get(i);
            line.append((i + 1)).append(". ")
                    .append(m1.plaintiffTeam.id).append(" vs ")
                    .append(m1.defenseTeam.id);
            line.append(completed[i] ? "  ✅" : "  ❌");

            if (i + 1 < matchups.size()) {
                Matchup m2 = matchups.get(i + 1);
                line.append("\t\t").append((i + 2)).append(". ")
                        .append(m2.plaintiffTeam.id).append(" vs ")
                        .append(m2.defenseTeam.id);
                line.append(completed[i + 1] ? "  ✅" : "  ❌");
            }
            System.out.println(line);
        }
    }

    public Ballot inputSingleBallot(Team plaintiff, Team defense) {
        System.out.println("Enter scores for matchup: " + plaintiff.id + " vs " + defense.id);
        int pScore = inputScore("Plaintiff score");
        int dScore = inputScore("Defense score");
        return new Ballot(plaintiff, defense, pScore, dScore);
    }

    public void enterJudgeNominations(int numWitnesses, NominationTracker nominationTracker) {
        int numRanks = Math.min(3, numWitnesses);

        System.out.println("Enter top " + numRanks + " ATTORNEYS for this judge (any team):");
        for (int i = 1; i <= numRanks; i++) {
            System.out.print(i + getSuffix(i) + " place: ");
            String name = scanner.nextLine().trim();
            nominationTracker.addNomination(name, RoleType.ATTORNEY, i);
        }

        System.out.println("Enter top " + numRanks + " WITNESSES for this judge (any team):");
        for (int i = 1; i <= numRanks; i++) {
            System.out.print(i + getSuffix(i) + " place: ");
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

    private String getSuffix(int number) {
        if (number >= 11 && number <= 13) return "th";
        return switch (number % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    private void printTeamRoster(String label, Roster roster) {
        System.out.println("\n" + label + " Roster:");
        for (RoleAssignment attorney : roster.getAttorneys()) {
            System.out.println("- " + attorney.getStudent().getName() + " (Attorney)");
        }
        for (RoleAssignment witness : roster.getWitnesses()) {
            System.out.println("- " + witness.getStudent().getName() + " (Witness)");
        }
    }
}

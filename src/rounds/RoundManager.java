package rounds;

import model.Team;
import java.util.*;

public class RoundManager {
    private int roundCounter = 1;
    private final List<Round> rounds = new ArrayList<>();

    public Round createRound(List<Team> teams, int numJudges, int totalRounds) {
        // Sort to power-match
        teams.sort(Comparator.comparingInt((Team t) -> t.ballotsWon).reversed()
                .thenComparingInt(t -> t.pointDiff).reversed()
                .thenComparingInt(t -> t.totalPoints).reversed());

        List<Matchup> matchups = new ArrayList<>();

        for (int i = 0; i < teams.size(); i += 2) {
            Team t1 = teams.get(i);
            Team t2 = teams.get(i + 1);

            // Need to do each side (P/D) same # of times
            int targetEachSide = totalRounds / 2;

            int t1PNeed = targetEachSide - t1.plaintiffCount;
            int t1DNeed = targetEachSide - t1.defenseCount;
            int t2PNeed = targetEachSide - t2.plaintiffCount;
            int t2DNeed = targetEachSide - t2.defenseCount;

            boolean t1ShouldBeP = t1PNeed > t1DNeed;
            boolean t2ShouldBeP = t2PNeed > t2DNeed;

            boolean t1IsPlaintiff;

            if (t1ShouldBeP && !t2ShouldBeP) {
                t1IsPlaintiff = true;
            } else if (!t1ShouldBeP && t2ShouldBeP) {
                t1IsPlaintiff = false;
            } else {
                // Which team is further from balance
                int t1Diff = t1PNeed - t1DNeed;
                int t2Diff = t2PNeed - t2DNeed;

                if (t1Diff > t2Diff) {
                    t1IsPlaintiff = true;
                } else if (t1Diff < t2Diff) {
                    t1IsPlaintiff = false;
                } else {
                    // Random pick, both are equally far
                    t1IsPlaintiff = Math.random() < 0.5;
                }
            }

            Team pTeam = t1IsPlaintiff ? t1 : t2;
            Team dTeam = t1IsPlaintiff ? t2 : t1;

            // Assign sides, create matchup
            pTeam.plaintiffCount++;
            dTeam.defenseCount++;

            Matchup matchup = new Matchup();
            matchup.plaintiffTeam = pTeam;
            matchup.defenseTeam = dTeam;
            matchup.courtroomOrZoom = "Courtroom " + (i / 2 + 1);
            matchup.ballots = new ArrayList<>(Collections.nCopies(numJudges, null));

            matchups.add(matchup);
        }

        Round round = new Round();
        round.roundNumber = roundCounter++;
        round.matchups = matchups;
        rounds.add(round);
        return round;
    }

    public void enterBallotResults(Round round, BallotHandler ballotHandler) {
        for (Matchup m : round.matchups) {
            for (int i = 0; i < m.ballots.size(); i++) {
                System.out.println("\nJudge " + (i + 1) + " for " + m.plaintiffTeam.id + " vs. " + m.defenseTeam.id);
                Ballot b = ballotHandler.inputSingleBallot(m.plaintiffTeam, m.defenseTeam);
                m.ballots.set(i, b);

                // Update record
                b.plaintiffTeam.totalPoints += b.plaintiffPoints;
                b.defenseTeam.totalPoints += b.defensePoints;

                b.plaintiffTeam.pointDiff += b.margin;
                b.defenseTeam.pointDiff -= b.margin;

                if (b.winner == b.plaintiffTeam) {
                    b.plaintiffTeam.ballotsWon++;
                } else if (b.winner == b.defenseTeam) {
                    b.defenseTeam.ballotsWon++;
                }
            }
        }
    }

    public List<Round> getAllRounds() {
        return rounds;
    }
}

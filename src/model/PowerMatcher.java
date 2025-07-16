package model;

import model.Team;
import rounds.Matchup;

import java.util.*;

public class PowerMatcher {

    private static final String BYE_ID = "BYE_BUSTER";
    private final Map<String, Set<String>> pastMatchups;

    public PowerMatcher(Map<String, Set<String>> pastMatchups) {
        this.pastMatchups = pastMatchups;
    }

    public List<Matchup> createRound(List<Team> teams, int numJudges, int totalRounds) {
        List<Team> allTeams = new ArrayList<>(teams);

        if (allTeams.size() % 2 != 0) {
            Team bye = new Team();
            bye.id = BYE_ID;
            allTeams.add(bye);
        }

        allTeams.sort(Comparator.comparingInt((Team t) -> t.ballotsWon).reversed()
                .thenComparingInt(t -> t.pointDiff).reversed()
                .thenComparingInt(t -> t.totalPoints).reversed());

        List<Matchup> matchups = new ArrayList<>();
        Set<String> paired = new HashSet<>();

        for (int i = 0; i < allTeams.size(); i++) {
            Team t1 = allTeams.get(i);
            if (paired.contains(t1.id) || t1.id.equals(BYE_ID)) continue;

            Team bestPartner = null;
            boolean t1IsPlaintiff = true;
            int bestScore = Integer.MAX_VALUE;

            for (int j = i + 1; j < allTeams.size(); j++) {
                Team t2 = allTeams.get(j);
                if (paired.contains(t2.id) || t2.id.equals(BYE_ID)) continue;

                SideAssignment sides = findSideAssignment(t1, t2, totalRounds);
                if (sides == null) continue;

                boolean isRepeat = isRematchSameSide(t1, t2, sides);
                if (isRepeat) continue;

                int score = recordDistance(t1, t2);
                if (score < bestScore) {
                    bestScore = score;
                    bestPartner = t2;
                    t1IsPlaintiff = sides.t1IsPlaintiff;
                }
            }

            if (bestPartner == null) {
                for (int j = i + 1; j < allTeams.size(); j++) {
                    Team t2 = allTeams.get(j);
                    if (paired.contains(t2.id) || t2.id.equals(BYE_ID)) continue;

                    SideAssignment sides = findSideAssignment(t1, t2, totalRounds);
                    if (sides == null) continue;

                    boolean oppRematch = isRematchOppositeSide(t1, t2, sides);
                    if (!oppRematch) continue;

                    int score = recordDistance(t1, t2);
                    if (score < bestScore) {
                        bestScore = score;
                        bestPartner = t2;
                        t1IsPlaintiff = sides.t1IsPlaintiff;
                    }
                }
            }

            if (bestPartner == null) {
                Optional<Team> bye = allTeams.stream()
                        .filter(t -> t.id.equals(BYE_ID) && !paired.contains(t.id)).findFirst();
                if (bye.isPresent()) {
                    paired.add(t1.id);
                    paired.add(BYE_ID);
                    t1.ballotsWon++;
                    Matchup byeMatchup = new Matchup();
                    byeMatchup.plaintiffTeam = t1;
                    byeMatchup.defenseTeam = bye.get();
                    byeMatchup.courtroomOrZoom = "Bye";
                    matchups.add(byeMatchup);
                    continue;
                }
            }

            if (bestPartner != null) {
                paired.add(t1.id);
                paired.add(bestPartner.id);

                if (t1IsPlaintiff) {
                    t1.plaintiffCount++;
                    bestPartner.defenseCount++;
                } else {
                    bestPartner.plaintiffCount++;
                    t1.defenseCount++;
                }

                recordMatchup(t1, bestPartner, t1IsPlaintiff);

                Matchup matchup = new Matchup();
                matchup.plaintiffTeam = t1IsPlaintiff ? t1 : bestPartner;
                matchup.defenseTeam = t1IsPlaintiff ? bestPartner : t1;
                matchup.courtroomOrZoom = "Courtroom " + (matchups.size() + 1);
                matchup.ballots = new ArrayList<>();

                matchups.add(matchup);
            }
        }

        return matchups;
    }

    private int recordDistance(Team t1, Team t2) {
        int win = Math.abs(t1.ballotsWon - t2.ballotsWon) * 10000;
        int margin = Math.abs(t1.pointDiff - t2.pointDiff) * 100;
        int points = Math.abs(t1.totalPoints - t2.totalPoints);
        return win + margin + points;
    }

    private SideAssignment findSideAssignment(Team t1, Team t2, int totalRounds) {
        int goal = totalRounds / 2;
        int t1P = goal - t1.plaintiffCount, t1D = goal - t1.defenseCount;
        int t2P = goal - t2.plaintiffCount, t2D = goal - t2.defenseCount;

        if (t1P > t1D && t2D > t2P) return new SideAssignment(true);
        if (t2P > t2D && t1D > t1P) return new SideAssignment(false);
        return new SideAssignment(Math.random() < 0.5);
    }

    private boolean isRematchSameSide(Team t1, Team t2, SideAssignment sides) {
        String key = matchupKey(t1.id, t2.id);
        String sideStr = sides.t1IsPlaintiff ? "P-D" : "D-P";
        return pastMatchups.getOrDefault(key, Set.of()).contains(sideStr);
    }

    private boolean isRematchOppositeSide(Team t1, Team t2, SideAssignment sides) {
        String key = matchupKey(t1.id, t2.id);
        String sideStr = sides.t1IsPlaintiff ? "D-P" : "P-D";
        return pastMatchups.getOrDefault(key, Set.of()).contains(sideStr);
    }

    public void recordMatchup(Team t1, Team t2, boolean t1Plaintiff) {
        String key = matchupKey(t1.id, t2.id);
        String side = t1Plaintiff ? "P-D" : "D-P";
        pastMatchups.computeIfAbsent(key, k -> new HashSet<>()).add(side);
    }

    private String matchupKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "#" + b : b + "#" + a;
    }

    private static class SideAssignment {
        boolean t1IsPlaintiff;
        SideAssignment(boolean t1IsPlaintiff) {
            this.t1IsPlaintiff = t1IsPlaintiff;
        }
    }
}
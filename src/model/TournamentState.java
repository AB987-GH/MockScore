package model;

import model.NominationTracker;
import model.Team;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TournamentState implements Serializable {
    public List<Team> teams;
    public Map<String, Set<String>> pastMatchups;
    public NominationTracker nominationTracker;
    public int totalRounds;
    public int numJudges;
    public int numAwards;
    public int roundCounter;

    public TournamentState(List<Team> teams, Map<String, Set<String>> pastMatchups,
                           NominationTracker nominationTracker, int totalRounds,
                           int numJudges, int numAwards, int roundCounter) {
        this.teams = teams;
        this.pastMatchups = pastMatchups;
        this.nominationTracker = nominationTracker;
        this.totalRounds = totalRounds;
        this.numJudges = numJudges;
        this.numAwards = numAwards;
        this.roundCounter = roundCounter;
    }
}

package rounds;

import model.Team;

import java.util.List;

public class Matchup {
    public Team plaintiffTeam;
    public Team defenseTeam;
    public List<Ballot> ballots; // One per judge
    public String courtroomOrZoom;

    public int getBallotsWon(Team team) {
        int count = 0;
        for (Ballot b : ballots) {
            if (b.winner == team) count++;
        }
        return count;
    }

    public int getPointDifferential(Team team) {
        int diff = 0;
        for (Ballot b : ballots) {
            if (b.plaintiffTeam == team) diff += b.plaintiffPoints - b.defensePoints;
            else if (b.defenseTeam == team) diff += b.defensePoints - b.plaintiffPoints;
        }
        return diff;
    }

    public int getTotalPoints(Team team) {
        int total = 0;
        for (Ballot b : ballots) {
            if (b.plaintiffTeam == team) total += b.plaintiffPoints;
            else if (b.defenseTeam == team) total += b.defensePoints;
        }
        return total;
    }
}

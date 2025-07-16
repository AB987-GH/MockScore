package rounds;

import model.Team;

public class Ballot {
    public Team plaintiffTeam;
    public Team defenseTeam;

    public int plaintiffPoints;
    public int defensePoints;

    public int margin;          // plaintiffPoints - defensePoints
    public Team winner;         // can be null for tie

    public void calculateResults() {
        margin = plaintiffPoints - defensePoints;
        if (margin > 0) {
            winner = plaintiffTeam;
        } else if (margin < 0) {
            winner = defenseTeam;
        } else {
            winner = null; // Tie
        }
    }

    public Ballot(Team plaintiffTeam, Team defenseTeam, int plaintiffPoints, int defensePoints){
        this.plaintiffTeam = plaintiffTeam;
        this.defenseTeam = defenseTeam;
        this.plaintiffPoints = plaintiffPoints;
        this.defensePoints = defensePoints;
    }
}

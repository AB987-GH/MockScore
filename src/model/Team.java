package model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    public String id;
    public Roster plaintiffRoster;
    public Roster defenseRoster;


    public int ballotsWon;
    public int totalPoints;
    public int pointDiff;

    public int plaintiffCount = 0; // number of times team has been Plaintiff thus far
    public int defenseCount = 0; // number of times team has been Defense thus far
    public List<Student> students = new ArrayList<>();
}

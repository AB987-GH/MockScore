package model;

import java.util.List;

public class Roster {
    public List<RoleAssignment> attorneys;
    public List<RoleAssignment> witnesses;

    public List<RoleAssignment> getAttorneys(){ return attorneys; }
    public List<RoleAssignment> getWitnesses(){ return witnesses; }
}

package model;

public class RoleAssignment {
    public Student student;
    public RoleType role;
    public TrialSide side;

    public Student getStudent(){ return student; }
    public void setStudent(Student student){ this.student = student; }

    public RoleType getRole(){ return role; }
    public void setRole(RoleType role){this.role = role; }

    public TrialSide getSide(){ return side; }
    public void setSide(TrialSide side){this.side = side; }
}


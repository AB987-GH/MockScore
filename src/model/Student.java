package model;

public class Student {
    private final String name;
    private final RoleType role;

    public Student(String name, RoleType role){
        this.name = name;
        this.role = role;
    }

    public String getName(){ return name; }
    public RoleType getRole(){ return role; }

    @Override
    public String toString(){
        return name + " (" + role.toString().toLowerCase().charAt(0) + role.toString().toLowerCase().substring(1) + ")";
    }
}

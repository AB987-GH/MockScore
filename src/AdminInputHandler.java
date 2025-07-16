import model.*;

import java.util.*;

public class AdminInputHandler {
    private final Scanner scanner = new Scanner(System.in);

    public Team inputTeam() {
        System.out.print("Enter Team ID/Name");
        String id = scanner.nextLine();

        System.out.println("\nEntering Plaintiff Side Roster");
        Roster plaintiffRoster = inputRoster(TrialSide.PLAINTIFF);

        System.out.println("\nEntering Defense Side Roster");
        Roster defenseRoster = inputRoster(TrialSide.DEFENSE);

        Team team = new Team();
        team.id = id;
        team.plaintiffRoster = plaintiffRoster;
        team.defenseRoster = defenseRoster;

        System.out.println("✅ model.Team " + id + " added successfully!\n");
        return team;
    }

    private Roster inputRoster(TrialSide side) {
        Roster roster = new Roster();
        roster.attorneys = new ArrayList<>();
        roster.witnesses = new ArrayList<>();

        while (true) {
            System.out.println("\nAdd a new role for " + side + " side:");
            System.out.print("Enter student name (or press Enter to finish this side): ");
            String name = scanner.nextLine();
            if (name.isBlank()) break;

            RoleType roleType = promptForRoleType();

            RoleAssignment assignment = new RoleAssignment();
            assignment.studentName = name;
            assignment.role = roleType;
            assignment.side = side;

            if (roleType == RoleType.WITNESS) {
                roster.witnesses.add(assignment);
            } else {
                roster.attorneys.add(assignment);
            }

            System.out.println("Added: " + name + " as " + roleType + " on " + side + " side.");
        }

        return roster;
    }

    private RoleType promptForRoleType() {
        while (true) {
            System.out.println("Select Role Type:");
            for (int i = 0; i < RoleType.values().length; i++) {
                System.out.println((i + 1) + ". " + RoleType.values()[i]);
            }
            System.out.print("Enter the number: ");
            String input = scanner.nextLine();
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < RoleType.values().length) {
                    return RoleType.values()[index];
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("❌ Invalid choice. Please enter a valid number.");
        }
    }
}

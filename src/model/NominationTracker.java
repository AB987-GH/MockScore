package model;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class NominationTracker {
    private final Map<String, EnumMap<RoleType, Integer>> studentAwards = new HashMap<>();

    public void addNomination(String studentName, RoleType role, int placement) {
        int value = switch (placement) {
            case 1 -> 3;
            case 2 -> 2;
            case 3 -> 1;
            default -> 0;
        };

        studentAwards.putIfAbsent(studentName, new EnumMap<>(RoleType.class));
        Map<RoleType, Integer> roleMap = studentAwards.get(studentName);
        roleMap.put(role, roleMap.getOrDefault(role, 0) + value);
    }

    public int getPoints(String studentName, RoleType role) {
        return studentAwards.getOrDefault(studentName, new EnumMap<>(RoleType.class))
                .getOrDefault(role, 0);
    }

    public Map<String, EnumMap<RoleType, Integer>> getAll() {
        return studentAwards;
    }
}


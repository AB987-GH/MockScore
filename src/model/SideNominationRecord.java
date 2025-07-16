package model;

import java.util.*;

public class SideNominationRecord {
    private final Map<TrialSide, Map<RoleType, Integer>> points = new HashMap<>();

    public void addNomination(TrialSide side, RoleType role, int placement) {
        int value;
        switch (placement) {
            case 1 -> value = 3;
            case 2 -> value = 2;
            case 3 -> value = 1;
            default -> value = 0;
        }

        points.putIfAbsent(side, new HashMap<>());
        Map<RoleType, Integer> sideMap = points.get(side);
        sideMap.put(role, sideMap.getOrDefault(role, 0) + value);
    }

    public int getTotalPoints(TrialSide side, RoleType role) {
        return points.getOrDefault(side, Collections.emptyMap()).getOrDefault(role, 0);
    }

    public Map<TrialSide, Map<RoleType, Integer>> getAll() {
        return points;
    }
}

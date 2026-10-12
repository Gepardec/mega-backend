package com.gepardec.mega.hexagon.worktime.domain.model;

import java.util.Arrays;
import java.util.Optional;

public enum Task {
    BEARBEITEN,
    BESPRECHEN,
    DOKUMENTIEREN,
    REISEN,
    UNDEFINIERT;

    public static boolean isJourney(Task task) {
        return task == REISEN;
    }

    public static Optional<Task> fromString(String name) {
        return name == null ? Optional.empty() : Arrays.stream(values())
                .filter(task -> task.name().equalsIgnoreCase(name))
                .findFirst();
    }
}

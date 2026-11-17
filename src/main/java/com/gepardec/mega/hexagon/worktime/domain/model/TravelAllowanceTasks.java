package com.gepardec.mega.hexagon.worktime.domain.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record TravelAllowanceTasks(Map<Integer, Set<Integer>> ownTaskIdsByProject) {

    /** ZEP process "Reisepauschale" of the project "Intern". */
    public static final int INTERN_REISEPAUSCHALE_TASK_ID = 873;

    private static final String TASK_NAME = "Reisepauschale";

    public TravelAllowanceTasks {
        Objects.requireNonNull(ownTaskIdsByProject, "ownTaskIdsByProject must not be null");
        Map<Integer, Set<Integer>> copy = new HashMap<>();
        ownTaskIdsByProject.forEach((projectZepId, taskIds) -> copy.put(projectZepId, Set.copyOf(taskIds)));
        ownTaskIdsByProject = Map.copyOf(copy);
    }

    public static TravelAllowanceTasks none() {
        return new TravelAllowanceTasks(Map.of());
    }

    public static TravelAllowanceTasks from(Collection<ProjectTask> tasks) {
        Map<Integer, Set<Integer>> byProject = new HashMap<>();
        for (ProjectTask task : tasks) {
            if (isOwnTask(task)) {
                byProject.computeIfAbsent(task.projectZepId(), id -> new HashSet<>()).add(task.zepTaskId());
            }
        }
        return new TravelAllowanceTasks(byProject);
    }

    public boolean declaresOwnTask(Integer projectZepId) {
        return !ownTaskIds(projectZepId).isEmpty();
    }

    public Set<Integer> ownTaskIds(Integer projectZepId) {
        if (projectZepId == null) {
            return Set.of();
        }
        return ownTaskIdsByProject.getOrDefault(projectZepId, Set.of());
    }

    private static boolean isOwnTask(ProjectTask task) {
        return task.zepTaskId() != INTERN_REISEPAUSCHALE_TASK_ID
                && task.name() != null
                && TASK_NAME.equalsIgnoreCase(task.name().strip());
    }
}

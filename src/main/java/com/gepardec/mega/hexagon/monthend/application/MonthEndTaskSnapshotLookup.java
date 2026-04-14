package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.util.Map;
import java.util.Objects;

public record MonthEndTaskSnapshotLookup(
        Map<ProjectId, MonthEndProjectSnapshot> projectsById,
        Map<UserId, MonthEndUserSnapshot> usersById
) {

    public MonthEndTaskSnapshotLookup {
        Objects.requireNonNull(projectsById, "projectsById must not be null");
        Objects.requireNonNull(usersById, "usersById must not be null");

        projectsById = Map.copyOf(projectsById);
        usersById = Map.copyOf(usersById);
    }

    public static MonthEndTaskSnapshotLookup empty() {
        return new MonthEndTaskSnapshotLookup(Map.of(), Map.of());
    }

    public MonthEndProjectSnapshot projectFor(ProjectId projectId) {
        MonthEndProjectSnapshot project = projectsById.get(projectId);
        if (project == null) {
            throw new IllegalStateException("project snapshot not found for project %s".formatted(projectId.value()));
        }
        return project;
    }

    public MonthEndUserSnapshot subjectEmployeeFor(UserId subjectEmployeeId) {
        if (subjectEmployeeId == null) {
            return null;
        }

        MonthEndUserSnapshot subjectEmployee = usersById.get(subjectEmployeeId);
        if (subjectEmployee == null) {
            throw new IllegalStateException("subject employee snapshot not found for employee %s"
                    .formatted(subjectEmployeeId.value()));
        }
        return subjectEmployee;
    }
}

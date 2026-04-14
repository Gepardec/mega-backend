package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;

import java.util.Map;
import java.util.Objects;

public record MonthEndTaskSnapshotLookup(
        Map<ProjectId, ProjectRef> projectsById,
        Map<UserId, UserRef> usersById
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

    public ProjectRef projectFor(ProjectId projectId) {
        ProjectRef project = projectsById.get(projectId);
        if (project == null) {
            throw new IllegalStateException("project snapshot not found for project %s".formatted(projectId.value()));
        }
        return project;
    }

    public UserRef subjectEmployeeFor(UserId subjectEmployeeId) {
        if (subjectEmployeeId == null) {
            return null;
        }

        UserRef subjectEmployee = usersById.get(subjectEmployeeId);
        if (subjectEmployee == null) {
            throw new IllegalStateException("subject employee snapshot not found for employee %s"
                    .formatted(subjectEmployeeId.value()));
        }
        return subjectEmployee;
    }
}

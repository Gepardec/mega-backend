package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.time.YearMonth;
import java.util.Objects;
import java.util.Set;

public record MonthEndTask(
        MonthEndTaskId id,
        YearMonth month,
        MonthEndTaskType type,
        ProjectId projectId,
        UserId subjectEmployeeId,
        Set<UserId> eligibleActorIds,
        MonthEndTaskStatus status,
        UserId completedBy
) {

    public MonthEndTask {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(month, "month must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(eligibleActorIds, "eligibleActorIds must not be null");
        Objects.requireNonNull(status, "status must not be null");

        eligibleActorIds = Set.copyOf(eligibleActorIds);

        validateCompletionPolicy(eligibleActorIds, type);
        validateTypeSpecificInvariants(type, subjectEmployeeId, eligibleActorIds);
        validateCompletionState(status, completedBy, eligibleActorIds);
    }

    public static MonthEndTask create(
            MonthEndTaskId id,
            YearMonth month,
            MonthEndTaskType type,
            ProjectId projectId,
            UserId subjectEmployeeId,
            Set<UserId> eligibleActorIds
    ) {
        return new MonthEndTask(
                id,
                month,
                type,
                projectId,
                subjectEmployeeId,
                eligibleActorIds,
                MonthEndTaskStatus.OPEN,
                null
        );
    }

    public static MonthEndTask reconstitute(
            MonthEndTaskId id,
            YearMonth month,
            MonthEndTaskType type,
            ProjectId projectId,
            UserId subjectEmployeeId,
            Set<UserId> eligibleActorIds,
            MonthEndTaskStatus status,
            UserId completedBy
    ) {
        return new MonthEndTask(
                id,
                month,
                type,
                projectId,
                subjectEmployeeId,
                eligibleActorIds,
                status,
                completedBy
        );
    }

    public MonthEndTask complete(UserId actorId) {
        Objects.requireNonNull(actorId, "actorId must not be null");

        if (!eligibleActorIds.contains(actorId)) {
            throw new IllegalArgumentException("actor is not eligible to complete the task");
        }

        if (status == MonthEndTaskStatus.DONE) {
            return this;
        }

        return new MonthEndTask(
                id,
                month,
                type,
                projectId,
                subjectEmployeeId,
                eligibleActorIds,
                MonthEndTaskStatus.DONE,
                actorId
        );
    }

    public MonthEndCompletionPolicy completionPolicy() {
        return type.completionPolicy();
    }

    public boolean isOpen() {
        return status == MonthEndTaskStatus.OPEN;
    }

    public MonthEndTaskKey businessKey() {
        return new MonthEndTaskKey(month, projectId, type, subjectEmployeeId);
    }

    private static void validateCompletionPolicy(Set<UserId> eligibleActorIds, MonthEndTaskType type) {
        if (eligibleActorIds.isEmpty()) {
            throw new IllegalArgumentException("eligibleActorIds must not be empty");
        }

        if (type.completionPolicy() == MonthEndCompletionPolicy.INDIVIDUAL_ACTOR && eligibleActorIds.size() != 1) {
            throw new IllegalArgumentException("individual-actor tasks must have exactly one eligible actor");
        }
    }

    private static void validateTypeSpecificInvariants(
            MonthEndTaskType type,
            UserId subjectEmployeeId,
            Set<UserId> eligibleActorIds
    ) {
        switch (type) {
            case EMPLOYEE_TIME_CHECK, LEISTUNGSNACHWEIS -> {
                if (subjectEmployeeId == null) {
                    throw new IllegalArgumentException("employee-owned tasks require a subject employee");
                }
                if (!eligibleActorIds.contains(subjectEmployeeId)) {
                    throw new IllegalArgumentException(
                            "employee-owned tasks must reference their eligible employee actor"
                    );
                }
            }
            case PROJECT_LEAD_REVIEW -> {
                if (subjectEmployeeId == null) {
                    throw new IllegalArgumentException("project lead review tasks require a subject employee");
                }
            }
            case ABRECHNUNG -> {
                if (subjectEmployeeId != null) {
                    throw new IllegalArgumentException("abrechnung tasks must not reference a subject employee");
                }
            }
            default -> throw new IllegalArgumentException("unsupported task type: " + type);
        }
    }

    private static void validateCompletionState(
            MonthEndTaskStatus status,
            UserId completedBy,
            Set<UserId> eligibleActorIds
    ) {
        if (status == MonthEndTaskStatus.OPEN && completedBy != null) {
            throw new IllegalArgumentException("open tasks must not record a completing actor");
        }

        if (status == MonthEndTaskStatus.DONE) {
            if (completedBy == null) {
                throw new IllegalArgumentException("completed tasks must record the completing actor");
            }
            if (!eligibleActorIds.contains(completedBy)) {
                throw new IllegalArgumentException("completedBy must be part of the eligible actor set");
            }
        }
    }
}

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
        MonthEndCompletionPolicy completionPolicy,
        MonthEndTaskStatus status,
        UserId completedBy
) {

    public MonthEndTask {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(month, "month must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(eligibleActorIds, "eligibleActorIds must not be null");
        Objects.requireNonNull(completionPolicy, "completionPolicy must not be null");
        Objects.requireNonNull(status, "status must not be null");

        eligibleActorIds = Set.copyOf(eligibleActorIds);

        validateCompletionPolicy(eligibleActorIds, completionPolicy);
        validateTypeSpecificInvariants(type, subjectEmployeeId, completionPolicy);
        validateCompletionState(status, completedBy, eligibleActorIds);
    }

    public static MonthEndTask create(
            MonthEndTaskId id,
            YearMonth month,
            MonthEndTaskType type,
            ProjectId projectId,
            UserId subjectEmployeeId,
            Set<UserId> eligibleActorIds,
            MonthEndCompletionPolicy completionPolicy
    ) {
        return new MonthEndTask(
                id,
                month,
                type,
                projectId,
                subjectEmployeeId,
                eligibleActorIds,
                completionPolicy,
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
            MonthEndCompletionPolicy completionPolicy,
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
                completionPolicy,
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
                completionPolicy,
                MonthEndTaskStatus.DONE,
                actorId
        );
    }

    public boolean isOpen() {
        return status == MonthEndTaskStatus.OPEN;
    }

    public MonthEndTaskKey businessKey() {
        return new MonthEndTaskKey(month, projectId, type, subjectEmployeeId);
    }

    private static void validateCompletionPolicy(
            Set<UserId> eligibleActorIds,
            MonthEndCompletionPolicy completionPolicy
    ) {
        if (eligibleActorIds.isEmpty()) {
            throw new IllegalArgumentException("eligibleActorIds must not be empty");
        }

        if (completionPolicy == MonthEndCompletionPolicy.INDIVIDUAL_ACTOR && eligibleActorIds.size() != 1) {
            throw new IllegalArgumentException("individual-actor tasks must have exactly one eligible actor");
        }
    }

    private static void validateTypeSpecificInvariants(
            MonthEndTaskType type,
            UserId subjectEmployeeId,
            MonthEndCompletionPolicy completionPolicy
    ) {
        switch (type) {
            case EMPLOYEE_TIME_CHECK, LEISTUNGSNACHWEIS -> {
                if (subjectEmployeeId != null) {
                    throw new IllegalArgumentException("employee-owned tasks must not reference a subject employee");
                }
                if (completionPolicy != MonthEndCompletionPolicy.INDIVIDUAL_ACTOR) {
                    throw new IllegalArgumentException("employee-owned tasks must use the individual-actor policy");
                }
            }
            case PROJECT_LEAD_REVIEW -> {
                if (subjectEmployeeId == null) {
                    throw new IllegalArgumentException("project lead review tasks require a subject employee");
                }
                if (completionPolicy != MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR) {
                    throw new IllegalArgumentException("project lead review tasks must use the shared-actor policy");
                }
            }
            case ABRECHNUNG -> {
                if (subjectEmployeeId != null) {
                    throw new IllegalArgumentException("abrechnung tasks must not reference a subject employee");
                }
                if (completionPolicy != MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR) {
                    throw new IllegalArgumentException("abrechnung tasks must use the shared-actor policy");
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

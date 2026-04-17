package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndActorNotAuthorizedException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationClosedException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndValidationException;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Set;

public record MonthEndClarification(
        MonthEndClarificationId id,
        YearMonth month,
        ProjectId projectId,
        UserId subjectEmployeeId,
        UserId createdBy,
        Set<UserId> eligibleProjectLeadIds,
        MonthEndClarificationStatus status,
        String text,
        String resolutionNote,
        UserId resolvedBy,
        Instant createdAt,
        Instant resolvedAt,
        Instant lastModifiedAt
) {

    public MonthEndClarification {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(month, "month must not be null");
        Objects.requireNonNull(projectId, "projectId must not be null");
        Objects.requireNonNull(createdBy, "createdBy must not be null");
        Objects.requireNonNull(eligibleProjectLeadIds, "eligibleProjectLeadIds must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(lastModifiedAt, "lastModifiedAt must not be null");

        eligibleProjectLeadIds = Set.copyOf(eligibleProjectLeadIds);
        text = requireNonBlank(text, "text must not be blank");
        resolutionNote = normalizeOptionalText(resolutionNote);

        validateEligibleLeads(eligibleProjectLeadIds);
        validateCreator(createdBy, subjectEmployeeId, eligibleProjectLeadIds);
        validateTimestamps(createdAt, resolvedAt, lastModifiedAt);
        validateResolutionState(status, resolutionNote, resolvedBy, resolvedAt);
    }

    public static MonthEndClarification create(
            MonthEndClarificationId id,
            YearMonth month,
            ProjectId projectId,
            UserId subjectEmployeeId,
            UserId createdBy,
            Set<UserId> eligibleProjectLeadIds,
            String text,
            Instant createdAt
    ) {
        return new MonthEndClarification(
                id,
                month,
                projectId,
                subjectEmployeeId,
                createdBy,
                eligibleProjectLeadIds,
                MonthEndClarificationStatus.OPEN,
                text,
                null,
                null,
                createdAt,
                null,
                createdAt
        );
    }

    public MonthEndClarification editText(UserId actorId, String updatedText, Instant modifiedAt) {
        Objects.requireNonNull(actorId, "actorId must not be null");
        Objects.requireNonNull(modifiedAt, "modifiedAt must not be null");

        if (status == MonthEndClarificationStatus.DONE) {
            throw new MonthEndClarificationClosedException("done clarifications cannot be edited");
        }

        if (!canEditText(actorId)) {
            throw new MonthEndActorNotAuthorizedException("actor is not allowed to edit clarification text");
        }

        return new MonthEndClarification(
                id,
                month,
                projectId,
                subjectEmployeeId,
                createdBy,
                eligibleProjectLeadIds,
                status,
                updatedText,
                resolutionNote,
                resolvedBy,
                createdAt,
                resolvedAt,
                modifiedAt
        );
    }

    public MonthEndClarification resolve(UserId actorId, String note, Instant completedAt) {
        Objects.requireNonNull(actorId, "actorId must not be null");
        Objects.requireNonNull(completedAt, "completedAt must not be null");

        if (status == MonthEndClarificationStatus.DONE) {
            return this;
        }

        if (!canBeResolvedBy(actorId)) {
            throw new MonthEndActorNotAuthorizedException("actor is not allowed to resolve clarification");
        }

        return new MonthEndClarification(
                id,
                month,
                projectId,
                subjectEmployeeId,
                createdBy,
                eligibleProjectLeadIds,
                MonthEndClarificationStatus.DONE,
                text,
                note,
                actorId,
                createdAt,
                completedAt,
                completedAt
        );
    }

    public boolean isOpen() {
        return status == MonthEndClarificationStatus.OPEN;
    }

    public boolean canEditText(UserId actorId) {
        Objects.requireNonNull(actorId, "actorId must not be null");
        return isOpen() && actorId.equals(createdBy);
    }

    public boolean canBeResolvedBy(UserId actorId) {
        Objects.requireNonNull(actorId, "actorId must not be null");
        return isOpen() && isInvolved(actorId) && !actorId.equals(createdBy);
    }

    public boolean canDelete(UserId actorId) {
        Objects.requireNonNull(actorId, "actorId must not be null");
        return isOpen() && actorId.equals(createdBy);
    }

    public boolean isInvolved(UserId actorId) {
        Objects.requireNonNull(actorId, "actorId must not be null");
        return eligibleProjectLeadIds.contains(actorId)
                || (subjectEmployeeId != null && subjectEmployeeId.equals(actorId));
    }

    private static void validateEligibleLeads(Set<UserId> eligibleProjectLeadIds) {
        if (eligibleProjectLeadIds.isEmpty()) {
            throw new MonthEndValidationException("eligibleProjectLeadIds must not be empty");
        }
    }

    private static void validateCreator(
            UserId createdBy,
            UserId subjectEmployeeId,
            Set<UserId> eligibleProjectLeadIds
    ) {
        boolean creatorIsEmployee = subjectEmployeeId != null && subjectEmployeeId.equals(createdBy);
        boolean creatorIsLead = eligibleProjectLeadIds.contains(createdBy);
        if (!creatorIsEmployee && !creatorIsLead) {
            throw new MonthEndValidationException("clarification creator must be the subject employee or an eligible lead");
        }
    }

    private static void validateTimestamps(Instant createdAt, Instant resolvedAt, Instant lastModifiedAt) {
        if (lastModifiedAt.isBefore(createdAt)) {
            throw new MonthEndValidationException("lastModifiedAt must not be before createdAt");
        }

        if (resolvedAt != null && resolvedAt.isBefore(createdAt)) {
            throw new MonthEndValidationException("resolvedAt must not be before createdAt");
        }
    }

    private static void validateResolutionState(
            MonthEndClarificationStatus status,
            String resolutionNote,
            UserId resolvedBy,
            Instant resolvedAt
    ) {
        if (status == MonthEndClarificationStatus.OPEN) {
            if (resolutionNote != null || resolvedBy != null || resolvedAt != null) {
                throw new MonthEndValidationException("open clarifications must not have resolution metadata");
            }
            return;
        }

        if (resolvedBy == null || resolvedAt == null) {
            throw new MonthEndValidationException("done clarifications must have resolver and resolvedAt");
        }
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new MonthEndValidationException(message);
        }
        return value;
    }

    private static String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}

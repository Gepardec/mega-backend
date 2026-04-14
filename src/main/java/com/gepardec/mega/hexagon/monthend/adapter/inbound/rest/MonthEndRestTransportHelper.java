package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.error.MonthEndRequestValidationException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class MonthEndRestTransportHelper {

    public YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(Objects.requireNonNull(month, "month must not be null"));
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new MonthEndRequestValidationException("invalid month format: " + month, exception);
        }
    }

    public ProjectId parseProjectId(String projectId) {
        return ProjectId.of(parseUuid(projectId, "projectId"));
    }

    public ProjectId toProjectId(UUID projectId) {
        return ProjectId.of(requireUuid(projectId, "projectId"));
    }

    public UserId parseUserId(String userId) {
        return UserId.of(parseUuid(userId, "userId"));
    }

    public UserId toUserId(UUID userId) {
        return UserId.of(requireUuid(userId, "userId"));
    }

    public MonthEndTaskId toTaskId(UUID taskId) {
        return MonthEndTaskId.of(requireUuid(taskId, "taskId"));
    }

    public MonthEndClarificationId toClarificationId(UUID clarificationId) {
        return MonthEndClarificationId.of(requireUuid(clarificationId, "clarificationId"));
    }

    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(Objects.requireNonNull(value, fieldName + " must not be null"));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new MonthEndRequestValidationException("invalid " + fieldName + ": " + value, exception);
        }
    }

    private UUID requireUuid(UUID value, String fieldName) {
        if (value == null) {
            throw new MonthEndRequestValidationException(fieldName + " must not be null", null);
        }
        return value;
    }
}

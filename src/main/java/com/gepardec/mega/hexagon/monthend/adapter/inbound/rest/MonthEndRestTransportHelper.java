package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkiverse.httpproblem.validation.Violation;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import static com.gepardec.mega.hexagon.shared.adapter.inbound.rest.RequestValidationProblems.invalid;

@ApplicationScoped
public class MonthEndRestTransportHelper {

    public YearMonth parseMonth(String month, Violation.In in) {
        if (month == null) {
            throw invalid(in, "month", "must not be null");
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException exception) {
            throw invalid(in, "month", "invalid month format: " + month);
        }
    }

    public ProjectId toProjectId(UUID projectId) {
        return ProjectId.of(requireUuid(projectId, "projectId", Violation.In.body));
    }

    public UserId toUserId(UUID userId, String field) {
        return UserId.of(requireUuid(userId, field, Violation.In.body));
    }

    public MonthEndTaskId toTaskId(UUID taskId) {
        return MonthEndTaskId.of(requireUuid(taskId, "taskId", Violation.In.path));
    }

    public MonthEndClarificationId toClarificationId(UUID clarificationId) {
        return MonthEndClarificationId.of(requireUuid(clarificationId, "clarificationId", Violation.In.path));
    }

    private UUID requireUuid(UUID value, String field, Violation.In in) {
        if (value == null) {
            throw invalid(in, field, "must not be null");
        }
        return value;
    }
}

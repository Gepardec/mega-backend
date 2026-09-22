package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import io.quarkiverse.httpproblem.validation.HttpValidationProblem;
import io.quarkiverse.httpproblem.validation.Violation;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonthEndRestTransportHelperTest {

    private final MonthEndRestTransportHelper helper = new MonthEndRestTransportHelper();

    @Test
    void parseMonth_shouldParseIsoYearMonth() {
        YearMonth month = helper.parseMonth("2026-03", Violation.In.path);

        assertThat(month).isEqualTo(YearMonth.of(2026, 3));
    }

    @Test
    void parseMonth_shouldRejectInvalidMonthAsValidationProblem() {
        assertThatThrownBy(() -> helper.parseMonth("2026-13", Violation.In.path))
                .isInstanceOfSatisfying(HttpValidationProblem.class, problem -> {
                    assertThat(problem.getStatusCode()).isEqualTo(400);
                    assertThat(problem.getParameters()).doesNotContainKey("code");
                    assertThat(problem.getViolations()).singleElement().satisfies(violation -> {
                        assertThat(violation.field).isEqualTo("month");
                        assertThat(violation.in).isEqualTo("path");
                        assertThat(violation.message).contains("2026-13");
                    });
                });
    }

    @Test
    void toProjectId_shouldRejectMissingBodyValue() {
        assertThatThrownBy(() -> helper.toProjectId(null))
                .isInstanceOfSatisfying(HttpValidationProblem.class, problem ->
                        assertThat(problem.getViolations()).singleElement().satisfies(violation -> {
                            assertThat(violation.field).isEqualTo("projectId");
                            assertThat(violation.in).isEqualTo("body");
                        }));
    }

    @Test
    void toDomainIds_shouldWrapTransportUuids() {
        UUID projectUuid = Instancio.create(UUID.class);
        UUID userUuid = Instancio.create(UUID.class);
        UUID taskUuid = Instancio.create(UUID.class);
        UUID clarificationUuid = Instancio.create(UUID.class);

        ProjectId projectId = helper.toProjectId(projectUuid);
        UserId userId = helper.toUserId(userUuid, "subjectEmployeeId");
        MonthEndTaskId taskId = helper.toTaskId(taskUuid);
        MonthEndClarificationId clarificationId = helper.toClarificationId(clarificationUuid);

        assertThat(projectId.value()).isEqualTo(projectUuid);
        assertThat(userId.value()).isEqualTo(userUuid);
        assertThat(taskId.value()).isEqualTo(taskUuid);
        assertThat(clarificationId.value()).isEqualTo(clarificationUuid);
    }
}

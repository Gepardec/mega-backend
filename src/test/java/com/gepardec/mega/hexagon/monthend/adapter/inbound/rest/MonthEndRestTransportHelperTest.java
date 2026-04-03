package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.error.MonthEndRequestValidationException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
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
        YearMonth month = helper.parseMonth("2026-03");

        assertThat(month).isEqualTo(YearMonth.of(2026, 3));
    }

    @Test
    void parseProjectId_shouldRejectInvalidUuid() {
        assertThatThrownBy(() -> helper.parseProjectId("not-a-uuid"))
                .isInstanceOf(MonthEndRequestValidationException.class)
                .hasMessageContaining("invalid projectId");
    }

    @Test
    void toDomainIds_shouldWrapTransportUuids() {
        UUID projectUuid = Instancio.create(UUID.class);
        UUID userUuid = Instancio.create(UUID.class);
        UUID taskUuid = Instancio.create(UUID.class);
        UUID clarificationUuid = Instancio.create(UUID.class);

        ProjectId projectId = helper.toProjectId(projectUuid);
        UserId userId = helper.toUserId(userUuid);
        MonthEndTaskId taskId = helper.toTaskId(taskUuid);
        MonthEndClarificationId clarificationId = helper.toClarificationId(clarificationUuid);

        assertThat(projectId.value()).isEqualTo(projectUuid);
        assertThat(userId.value()).isEqualTo(userUuid);
        assertThat(taskId.value()).isEqualTo(taskUuid);
        assertThat(clarificationId.value()).isEqualTo(clarificationUuid);
    }
}

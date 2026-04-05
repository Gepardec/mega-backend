package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndClarificationResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndStatusOverviewResponse;
import com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.generated.model.MonthEndWorklistResponse;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewProject;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistClarificationItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistItem;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MonthEndRestMapperTest {

    private final MonthEndRestMapper mapper = Mappers.getMapper(MonthEndRestMapper.class);

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final String projectName = "Project Mapper";
    private final UserId employeeId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadId = UserId.of(Instancio.create(UUID.class));

    @Test
    void toResponse_shouldMapWorklistIdsAndCollections() {
        MonthEndTaskId taskId = MonthEndTaskId.of(Instancio.create(UUID.class));
        MonthEndClarificationId clarificationId = MonthEndClarificationId.of(Instancio.create(UUID.class));
        Instant createdAt = Instant.parse("2026-03-25T09:15:00Z");
        Instant modifiedAt = Instant.parse("2026-03-26T10:45:00Z");
        MonthEndWorklist worklist = new MonthEndWorklist(
                employeeId,
                month,
                List.of(new MonthEndWorklistItem(taskId, MonthEndTaskType.EMPLOYEE_TIME_CHECK, projectId, employeeId)),
                List.of(new MonthEndWorklistClarificationItem(
                        clarificationId,
                        projectId,
                        employeeId,
                        employeeId,
                        MonthEndClarificationSide.EMPLOYEE,
                        MonthEndClarificationStatus.OPEN,
                        "Please review",
                        createdAt,
                        modifiedAt
                ))
        );

        MonthEndWorklistResponse response = mapper.toResponse(worklist);

        assertThat(response.getMonth()).isEqualTo("2026-03");
        assertThat(response.getTasks()).singleElement().satisfies(task -> {
            assertThat(task.getTaskId()).isEqualTo(taskId.value());
            assertThat(task.getProjectId()).isEqualTo(projectId.value());
            assertThat(task.getSubjectEmployeeId()).isEqualTo(employeeId.value());
        });
        assertThat(response.getClarifications()).singleElement().satisfies(clarification -> {
            assertThat(clarification.getClarificationId()).isEqualTo(clarificationId.value());
            assertThat(clarification.getProjectId()).isEqualTo(projectId.value());
            assertThat(clarification.getCreatedAt()).isEqualTo(OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC));
            assertThat(clarification.getLastModifiedAt()).isEqualTo(OffsetDateTime.ofInstant(modifiedAt, ZoneOffset.UTC));
        });
    }

    @Test
    void toResponse_shouldMapClarificationTimestampsToUtcOffsetDateTime() {
        Instant createdAt = Instant.parse("2026-03-25T09:15:00Z");
        Instant resolvedAt = Instant.parse("2026-03-27T14:30:00Z");
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.of(Instancio.create(UUID.class)),
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(leadId),
                "Initial clarification",
                createdAt
        ).resolve(leadId, "Resolved", resolvedAt);

        MonthEndClarificationResponse response = mapper.toResponse(clarification);

        assertThat(response.getClarificationId()).isEqualTo(clarification.id().value());
        assertThat(response.getProjectId()).isEqualTo(projectId.value());
        assertThat(response.getSubjectEmployeeId()).isEqualTo(employeeId.value());
        assertThat(response.getResolvedBy()).isEqualTo(leadId.value());
        assertThat(response.getCreatedAt()).isEqualTo(OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC));
        assertThat(response.getResolvedAt()).isEqualTo(OffsetDateTime.ofInstant(resolvedAt, ZoneOffset.UTC));
        assertThat(response.getLastModifiedAt()).isEqualTo(OffsetDateTime.ofInstant(resolvedAt, ZoneOffset.UTC));
    }

    @Test
    void toResponse_shouldMapStatusOverviewProjectName() {
        MonthEndStatusOverview overview = new MonthEndStatusOverview(
                employeeId,
                month,
                List.of(new MonthEndStatusOverviewItem(
                        MonthEndTaskId.of(Instancio.create(UUID.class)),
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        MonthEndTaskStatus.OPEN,
                        new MonthEndStatusOverviewProject(projectId, projectName),
                        employeeId,
                        null
                ))
        );

        MonthEndStatusOverviewResponse response = mapper.toResponse(overview);

        assertThat(response.getMonth()).isEqualTo("2026-03");
        assertThat(response.getEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.getProject().getId()).isEqualTo(projectId.value());
            assertThat(entry.getProject().getName()).isEqualTo(projectName);
            assertThat(entry.getSubjectEmployeeId()).isEqualTo(employeeId.value());
        });
    }
}

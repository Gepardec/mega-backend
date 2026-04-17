package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.MonthEndOverviewClarificationEntry;
import com.gepardec.mega.hexagon.generated.model.MonthEndStatusOverviewResponse;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MonthEndRestMapperTest {

    private final MonthEndRestMapper mapper = Mappers.getMapper(MonthEndRestMapper.class);

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final String projectName = "Project Mapper";
    private final UserId employeeId = UserId.of(Instancio.create(UUID.class));
    private final String employeeName = "Mapper Employee";
    private final UserId leadId = UserId.of(Instancio.create(UUID.class));
    private final UserId otherUserId = UserId.of(Instancio.create(UUID.class));

    @Test
    void toResponse_shouldMapStatusOverviewProjectAndClarificationUserReferences() {
        Instant createdAt = Instant.parse("2026-03-25T10:15:00Z");
        Instant resolvedAt = Instant.parse("2026-03-25T10:30:00Z");
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.of(Instancio.create(UUID.class)),
                month,
                projectId,
                employeeId,
                employeeId,
                Set.of(leadId),
                "Please update the proof.",
                createdAt
        ).resolve(leadId, "Handled in review.", resolvedAt);

        MonthEndStatusOverview overview = new MonthEndStatusOverview(
                employeeId,
                month,
                List.of(new MonthEndStatusOverviewItem(
                        MonthEndTaskId.of(Instancio.create(UUID.class)),
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        MonthEndTaskStatus.OPEN,
                        projectRef(),
                        employeeRef(),
                        true,
                        null
                )),
                List.of(clarification)
        );
        Map<UserId, UserRef> userRefs = Map.of(
                employeeId, employeeRef(),
                leadId, leadRef()
        );

        MonthEndStatusOverviewResponse response = mapper.toResponse(overview, userRefs, employeeId);

        assertThat(response.getMonth()).isEqualTo("2026-03");
        assertThat(response.getEntries()).singleElement().satisfies(entry -> {
            assertThat(entry.getProject().getId()).isEqualTo(projectId.value());
            assertThat(entry.getProject().getName()).isEqualTo(projectName);
            assertThat(entry.getSubjectEmployee()).isNotNull();
            assertThat(entry.getSubjectEmployee().getId()).isEqualTo(employeeId.value());
            assertThat(entry.getSubjectEmployee().getFullName()).isEqualTo(employeeName);
            assertThat(entry.getCanComplete()).isTrue();
        });
        assertThat(response.getClarifications()).singleElement().satisfies(c -> {
            assertThat(c.getProjectId()).isEqualTo(projectId.value());
            assertThat(c.getSubjectEmployee().getId()).isEqualTo(employeeId.value());
            assertThat(c.getSubjectEmployee().getFullName()).isEqualTo(employeeName);
            assertThat(c.getCreatedBy().getId()).isEqualTo(employeeId.value());
            assertThat(c.getCreatedBy().getFullName()).isEqualTo("Mapper Employee");
            assertThat(c.getResolvedBy().getId()).isEqualTo(leadId.value());
            assertThat(c.getResolvedBy().getFullName()).isEqualTo("Mapper Lead");
            assertThat(c.getResolvedAt()).isEqualTo(OffsetDateTime.ofInstant(resolvedAt, ZoneOffset.UTC));
            assertThat(c.getCanResolve()).isFalse();
        });
    }

    @Test
    void toResponse_shouldMapStatusOverviewCanCompleteFalseForSubjectOnlyEntry() {
        MonthEndStatusOverview overview = new MonthEndStatusOverview(
                employeeId,
                month,
                List.of(new MonthEndStatusOverviewItem(
                        MonthEndTaskId.of(Instancio.create(UUID.class)),
                        MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        MonthEndTaskStatus.OPEN,
                        projectRef(),
                        employeeRef(),
                        false,
                        null
                )),
                List.of()
        );

        MonthEndStatusOverviewResponse response = mapper.toResponse(overview, Map.of(), employeeId);

        assertThat(response.getEntries()).singleElement()
                .satisfies(entry -> assertThat(entry.getCanComplete()).isFalse());
    }

    @Test
    void toResponse_shouldOmitStatusOverviewSubjectEmployeeForAbrechnung() {
        MonthEndStatusOverview overview = new MonthEndStatusOverview(
                employeeId,
                month,
                List.of(new MonthEndStatusOverviewItem(
                        MonthEndTaskId.of(Instancio.create(UUID.class)),
                        MonthEndTaskType.ABRECHNUNG,
                        MonthEndTaskStatus.OPEN,
                        projectRef(),
                        null,
                        true,
                        null
                )),
                List.of()
        );

        MonthEndStatusOverviewResponse response = mapper.toResponse(overview, Map.of(), employeeId);

        assertThat(response.getEntries()).singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getSubjectEmployee()).isNull();
                    assertThat(entry.getCanComplete()).isTrue();
                });
    }

    @Test
    void toClarificationEntry_asCreator_shouldAllowEditAndDeleteButNotResolve() {
        Instant createdAt = Instant.parse("2026-03-25T09:00:00Z");
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.of(Instancio.create(UUID.class)),
                month,
                projectId,
                employeeId,
                employeeId,
                Set.of(leadId),
                "Please clarify these hours.",
                createdAt
        );
        Map<UserId, UserRef> userRefs = Map.of(
                employeeId, employeeRef(),
                leadId, leadRef()
        );

        MonthEndOverviewClarificationEntry entry = mapper.toClarificationEntry(clarification, userRefs, employeeId);

        assertThat(entry.getCanEditText()).isTrue();
        assertThat(entry.getCanDelete()).isTrue();
        assertThat(entry.getCanResolve()).isFalse();
        assertThat(entry.getCreatedBy().getId()).isEqualTo(employeeId.value());
        assertThat(entry.getSubjectEmployee().getId()).isEqualTo(employeeId.value());
        assertThat(entry.getResolvedBy()).isNull();
        assertThat(entry.getCreatedAt()).isEqualTo(OffsetDateTime.ofInstant(createdAt, ZoneOffset.UTC));
    }

    @Test
    void toClarificationEntry_asEligibleLead_shouldAllowResolveButNotEditOrDelete() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.of(Instancio.create(UUID.class)),
                month,
                projectId,
                employeeId,
                employeeId,
                Set.of(leadId),
                "Please clarify these hours.",
                Instant.parse("2026-03-25T09:00:00Z")
        );
        Map<UserId, UserRef> userRefs = Map.of(
                employeeId, employeeRef(),
                leadId, leadRef()
        );

        MonthEndOverviewClarificationEntry entry = mapper.toClarificationEntry(clarification, userRefs, leadId);

        assertThat(entry.getCanResolve()).isTrue();
        assertThat(entry.getCanEditText()).isFalse();
        assertThat(entry.getCanDelete()).isFalse();
    }

    @Test
    void toClarificationEntry_asNonInvolved_shouldDisableAllPermissions() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.of(Instancio.create(UUID.class)),
                month,
                projectId,
                employeeId,
                employeeId,
                Set.of(leadId),
                "Please clarify these hours.",
                Instant.parse("2026-03-25T09:00:00Z")
        );
        Map<UserId, UserRef> userRefs = Map.of(
                employeeId, employeeRef(),
                leadId, leadRef()
        );

        MonthEndOverviewClarificationEntry entry = mapper.toClarificationEntry(clarification, userRefs, otherUserId);

        assertThat(entry.getCanResolve()).isFalse();
        assertThat(entry.getCanEditText()).isFalse();
        assertThat(entry.getCanDelete()).isFalse();
    }

    @Test
    void toClarificationEntry_whenDone_shouldDisableAllPermissionsForCreator() {
        Instant createdAt = Instant.parse("2026-03-25T09:00:00Z");
        Instant resolvedAt = Instant.parse("2026-03-25T10:00:00Z");
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.of(Instancio.create(UUID.class)),
                month,
                projectId,
                employeeId,
                employeeId,
                Set.of(leadId),
                "Please clarify these hours.",
                createdAt
        ).resolve(leadId, "Looks good.", resolvedAt);
        Map<UserId, UserRef> userRefs = Map.of(
                employeeId, employeeRef(),
                leadId, leadRef()
        );

        MonthEndOverviewClarificationEntry entry = mapper.toClarificationEntry(clarification, userRefs, employeeId);

        assertThat(entry.getCanResolve()).isFalse();
        assertThat(entry.getCanEditText()).isFalse();
        assertThat(entry.getCanDelete()).isFalse();
        assertThat(entry.getResolvedBy().getId()).isEqualTo(leadId.value());
        assertThat(entry.getResolutionNote()).isEqualTo("Looks good.");
        assertThat(entry.getResolvedAt()).isEqualTo(OffsetDateTime.ofInstant(resolvedAt, ZoneOffset.UTC));
    }

    @Test
    void toClarificationEntry_withNullSubjectEmployee_shouldMapSubjectToNull() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.of(Instancio.create(UUID.class)),
                month,
                projectId,
                null,
                leadId,
                Set.of(leadId),
                "General project note.",
                Instant.parse("2026-03-25T09:00:00Z")
        );
        Map<UserId, UserRef> userRefs = Map.of(leadId, leadRef());

        MonthEndOverviewClarificationEntry entry = mapper.toClarificationEntry(clarification, userRefs, leadId);

        assertThat(entry.getSubjectEmployee()).isNull();
        assertThat(entry.getCreatedBy().getId()).isEqualTo(leadId.value());
    }

    private ProjectRef projectRef() {
        return new ProjectRef(projectId, 77, projectName);
    }

    private UserRef employeeRef() {
        return new UserRef(employeeId, FullName.of("Mapper", "Employee"), ZepUsername.of("mapper.employee"));
    }

    private UserRef leadRef() {
        return new UserRef(leadId, FullName.of("Mapper", "Lead"), ZepUsername.of("mapper.lead"));
    }
}

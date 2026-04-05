package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.UserStatus;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateMonthEndClarificationServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId employeeId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadA = UserId.of(Instancio.create(UUID.class));
    private final UserId leadB = UserId.of(Instancio.create(UUID.class));
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-31T08:00:00Z"), ZoneOffset.UTC);

    private MonthEndClarificationRepository clarificationRepository;
    private ResolveMonthEndEmployeeProjectContextService contextResolver;
    private CreateMonthEndClarificationService service;

    @BeforeEach
    void setUp() {
        clarificationRepository = mock(MonthEndClarificationRepository.class);
        contextResolver = mock(ResolveMonthEndEmployeeProjectContextService.class);
        service = new CreateMonthEndClarificationService(clarificationRepository, contextResolver, clock);
    }

    @Test
    void create_shouldPersistClarification_whenEmployeeCreatesInValidContext() {
        when(contextResolver.resolve(month, projectId, employeeId))
                .thenReturn(employeeContext(Set.of(leadA, leadB)));

        MonthEndClarification result = service.create(
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.EMPLOYEE,
                "Please check this project entry."
        );

        assertThat(result.creatorSide()).isEqualTo(MonthEndClarificationSide.EMPLOYEE);
        assertThat(result.eligibleProjectLeadIds()).containsExactlyInAnyOrder(leadA, leadB);
        assertThat(result.createdAt()).isEqualTo(clock.instant());
        verify(clarificationRepository).save(any(MonthEndClarification.class));
    }

    @Test
    void create_shouldAllowLeadSideForDualRoleUser_whenRequestedExplicitly() {
        when(contextResolver.resolve(month, projectId, employeeId))
                .thenReturn(employeeContext(Set.of(employeeId, leadB)));

        MonthEndClarification result = service.create(
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.PROJECT_LEAD,
                "Please update this from the lead perspective."
        );

        assertThat(result.creatorSide()).isEqualTo(MonthEndClarificationSide.PROJECT_LEAD);
        verify(clarificationRepository).save(any(MonthEndClarification.class));
    }

    @Test
    void create_shouldThrow_whenProjectContextIsMissing() {
        when(contextResolver.resolve(month, projectId, employeeId))
                .thenThrow(new IllegalArgumentException("month-end project context not found"));

        assertThatThrownBy(() -> service.create(
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.EMPLOYEE,
                "Please check this."
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("context not found");
    }

    private MonthEndEmployeeProjectContext employeeContext(Set<UserId> eligibleLeadIds) {
        return new MonthEndEmployeeProjectContext(
                month,
                new MonthEndProjectSnapshot(
                        projectId,
                        77,
                        "Project-77",
                        LocalDate.of(2025, 1, 1),
                        null,
                        true,
                        eligibleLeadIds
                ),
                new MonthEndUserSnapshot(
                        employeeId,
                        "Employee User",
                        "employee",
                        UserStatus.ACTIVE,
                        new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null))
                ),
                eligibleLeadIds
        );
    }
}

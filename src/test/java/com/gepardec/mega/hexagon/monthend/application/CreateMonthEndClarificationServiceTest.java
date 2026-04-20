package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationCreatedEvent;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployeeProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.services.MonthEndEmployeeProjectContextService;
import com.gepardec.mega.hexagon.monthend.domain.services.MonthEndProjectContextService;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.SourceSystem;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import jakarta.enterprise.event.Event;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
    private MonthEndEmployeeProjectContextService employeeContextService;
    private MonthEndProjectContextService projectContextService;
    private Event<ClarificationCreatedEvent> clarificationCreatedEvent;
    private CreateMonthEndClarificationService service;

    @BeforeEach
    void setUp() {
        clarificationRepository = mock(MonthEndClarificationRepository.class);
        employeeContextService = mock(MonthEndEmployeeProjectContextService.class);
        projectContextService = mock(MonthEndProjectContextService.class);
        clarificationCreatedEvent = mock(Event.class);
        service = new CreateMonthEndClarificationService(
                clarificationRepository,
                employeeContextService,
                projectContextService,
                clock,
                clarificationCreatedEvent
        );
    }

    @Test
    void create_shouldPersistClarification_whenEmployeeCreatesInValidContext() {
        when(employeeContextService.resolve(month, projectId, employeeId))
                .thenReturn(employeeContext(Set.of(leadA, leadB)));

        MonthEndClarification result = service.create(
                month,
                projectId,
                employeeId,
                employeeId,
                "Please check this project entry."
        );

        assertThat(result.subjectEmployeeId()).isEqualTo(employeeId);
        assertThat(result.createdBy()).isEqualTo(employeeId);
        assertThat(result.sourceSystem()).isEqualTo(SourceSystem.MEGA);
        assertThat(result.eligibleProjectLeadIds()).containsExactlyInAnyOrder(leadA, leadB);
        assertThat(result.createdAt()).isEqualTo(clock.instant());
        verify(clarificationRepository).save(any(MonthEndClarification.class));
        verify(clarificationCreatedEvent).fire(argThat(event ->
                event.creator().equals(employeeId)
                        && event.subjectEmployeeId().equals(employeeId)
                        && event.sourceSystem() == SourceSystem.MEGA
        ));
    }

    @Test
    void create_shouldPersistClarification_whenLeadCreatesForEmployee() {
        when(employeeContextService.resolve(month, projectId, employeeId))
                .thenReturn(employeeContext(Set.of(leadA, leadB)));

        MonthEndClarification result = service.create(
                month,
                projectId,
                employeeId,
                leadA,
                "Please update this from the lead perspective."
        );

        assertThat(result.subjectEmployeeId()).isEqualTo(employeeId);
        assertThat(result.createdBy()).isEqualTo(leadA);
        verify(clarificationRepository).save(any(MonthEndClarification.class));
    }

    @Test
    void create_shouldPersistProjectLevelClarification_whenLeadCreatesWithNoSubjectEmployee() {
        when(projectContextService.resolve(month, projectId))
                .thenReturn(projectContext(Set.of(leadA, leadB)));

        MonthEndClarification result = service.create(
                month,
                projectId,
                null,
                leadA,
                "Cross-lead discussion."
        );

        assertThat(result.subjectEmployeeId()).isNull();
        assertThat(result.createdBy()).isEqualTo(leadA);
        assertThat(result.eligibleProjectLeadIds()).containsExactlyInAnyOrder(leadA, leadB);
        verify(clarificationRepository).save(any(MonthEndClarification.class));
    }

    @Test
    void create_shouldThrow_whenProjectContextIsMissing() {
        when(employeeContextService.resolve(month, projectId, employeeId))
                .thenThrow(new IllegalArgumentException("month-end project context not found"));

        assertThatThrownBy(() -> service.create(
                month,
                projectId,
                employeeId,
                employeeId,
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
                        true,
                        eligibleLeadIds
                ),
                new UserRef(
                        employeeId,
                        FullName.of("Employee", "User"),
                        ZepUsername.of("employee")
                ),
                eligibleLeadIds
        );
    }

    private MonthEndProjectContext projectContext(Set<UserId> eligibleLeadIds) {
        return new MonthEndProjectContext(
                month,
                new MonthEndProjectSnapshot(
                        projectId,
                        77,
                        "Project-77",
                        true,
                        eligibleLeadIds
                ),
                eligibleLeadIds
        );
    }
}

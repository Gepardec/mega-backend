package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndErrorCode;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationCreatedEvent;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployeeProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.SourceSystem;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
        when(projectContextService.resolve(month, projectId))
                .thenReturn(projectContext(Set.of(leadA, leadB)));
        when(employeeContextService.resolve(any(MonthEndProjectContext.class), eq(employeeId)))
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
                        && event.eligibleProjectLeadIds().equals(Set.of(leadA, leadB))
        ));
    }

    @Test
    void create_shouldPersistClarification_whenLeadCreatesForEmployee() {
        when(projectContextService.resolve(month, projectId))
                .thenReturn(projectContext(Set.of(leadA, leadB)));
        when(employeeContextService.resolve(any(MonthEndProjectContext.class), eq(employeeId)))
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
        verify(clarificationCreatedEvent).fire(argThat(event ->
                event.creator().equals(leadA)
                        && event.subjectEmployeeId() == null
                        && event.eligibleProjectLeadIds().equals(Set.of(leadA, leadB))
        ));
    }

    @Test
    void create_shouldThrowNotAuthorized_whenNonLeadOmitsSubject() {
        when(projectContextService.resolve(month, projectId))
                .thenReturn(projectContext(Set.of(leadA, leadB)));

        assertThatThrownBy(() -> service.create(month, projectId, null, employeeId, "Need support."))
                .isInstanceOfSatisfying(MonthEndException.class,
                        thrown -> assertThat(thrown.errorCode()).isEqualTo(MonthEndErrorCode.ACTOR_NOT_AUTHORIZED));

        verifyNoInteractions(clarificationRepository, clarificationCreatedEvent);
    }

    @Test
    void create_shouldThrowNotAuthorized_whenUserLeadsOnlyADifferentProjectAndOmitsSubject() {
        UserId otherProjectLead = UserId.of(Instancio.create(UUID.class));
        when(projectContextService.resolve(month, projectId))
                .thenReturn(projectContext(Set.of(leadA, leadB)));

        assertThatThrownBy(() -> service.create(month, projectId, null, otherProjectLead, "Need support."))
                .isInstanceOfSatisfying(MonthEndException.class,
                        thrown -> assertThat(thrown.errorCode()).isEqualTo(MonthEndErrorCode.ACTOR_NOT_AUTHORIZED));

        verifyNoInteractions(clarificationRepository, clarificationCreatedEvent);
    }

    @Test
    void create_shouldUseLeadAsSubject_whenEligibleLeadNamesThemselves() {
        when(projectContextService.resolve(month, projectId))
                .thenReturn(projectContext(Set.of(leadA, leadB)));
        when(employeeContextService.resolve(any(MonthEndProjectContext.class), eq(leadA)))
                .thenReturn(employeeContext(Set.of(leadA, leadB)));

        MonthEndClarification result = service.create(month, projectId, leadA, leadA, "Own entries.");

        assertThat(result.subjectEmployeeId()).isEqualTo(leadA);
        assertThat(result.createdBy()).isEqualTo(leadA);
    }

    @Test
    void create_shouldThrowNotAuthorized_whenNonLeadNamesAnotherEmployee() {
        UserId otherEmployee = UserId.of(Instancio.create(UUID.class));
        when(projectContextService.resolve(month, projectId))
                .thenReturn(projectContext(Set.of(leadA, leadB)));

        assertThatThrownBy(() -> service.create(month, projectId, otherEmployee, employeeId, "Not allowed."))
                .isInstanceOfSatisfying(MonthEndException.class,
                        thrown -> assertThat(thrown.errorCode()).isEqualTo(MonthEndErrorCode.ACTOR_NOT_AUTHORIZED));

        verifyNoInteractions(employeeContextService, clarificationRepository, clarificationCreatedEvent);
    }

    @Test
    void create_shouldThrowProjectContextNotFound_whenProjectIsUnknown() {
        when(projectContextService.resolve(month, projectId))
                .thenThrow(new MonthEndException(MonthEndErrorCode.PROJECT_CONTEXT_NOT_FOUND, "month-end project context not found"));

        assertThatThrownBy(() -> service.create(month, projectId, employeeId, employeeId, "Please check this."))
                .isInstanceOfSatisfying(MonthEndException.class,
                        thrown -> assertThat(thrown.errorCode()).isEqualTo(MonthEndErrorCode.PROJECT_CONTEXT_NOT_FOUND))
                .hasMessageContaining("context not found");

        verifyNoInteractions(employeeContextService, clarificationRepository, clarificationCreatedEvent);
    }

    @Test
    void create_shouldThrowNotAssigned_whenLeadNamesUnassignedEmployee() {
        when(projectContextService.resolve(month, projectId))
                .thenReturn(projectContext(Set.of(leadA, leadB)));
        when(employeeContextService.resolve(any(MonthEndProjectContext.class), eq(employeeId)))
                .thenThrow(new MonthEndException(MonthEndErrorCode.EMPLOYEE_NOT_ASSIGNED_TO_PROJECT, "not assigned"));

        assertThatThrownBy(() -> service.create(month, projectId, employeeId, leadA, "Not assigned."))
                .isInstanceOfSatisfying(MonthEndException.class,
                        thrown -> assertThat(thrown.errorCode()).isEqualTo(MonthEndErrorCode.EMPLOYEE_NOT_ASSIGNED_TO_PROJECT));

        verifyNoInteractions(clarificationRepository, clarificationCreatedEvent);
    }

    @Test
    void create_shouldLoadProjectContextOnceAndReuseItForSubject() {
        MonthEndProjectContext projectContext = projectContext(Set.of(leadA, leadB));
        when(projectContextService.resolve(month, projectId)).thenReturn(projectContext);
        when(employeeContextService.resolve(projectContext, employeeId))
                .thenReturn(employeeContext(Set.of(leadA, leadB)));

        service.create(month, projectId, employeeId, employeeId, "Please check this.");

        verify(projectContextService).resolve(month, projectId);
        verify(employeeContextService).resolve(projectContext, employeeId);
        verifyNoMoreInteractions(employeeContextService);
    }

    private MonthEndEmployeeProjectContext employeeContext(Set<UserId> eligibleLeadIds) {
        return new MonthEndEmployeeProjectContext(
                month,
                new MonthEndProjectSnapshot(
                        projectId,
                        77,
                        "Project-77",
                        true,
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
                        true,
                        eligibleLeadIds
                ),
                eligibleLeadIds
        );
    }
}

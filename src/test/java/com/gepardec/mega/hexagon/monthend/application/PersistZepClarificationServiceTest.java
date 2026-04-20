package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationCreatedEvent;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployeeProjectContext;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PersistZepClarificationServiceTest {

    private final YearMonth month = YearMonth.of(2023, 11);
    private final UserId creatorId = UserId.of(Instancio.create(UUID.class));
    private final UserId subjectEmployeeId = UserId.of(Instancio.create(UUID.class));
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final Clock clock = Clock.fixed(Instant.parse("2023-11-03T10:15:30Z"), ZoneOffset.UTC);

    private MonthEndClarificationRepository monthEndClarificationRepository;
    private Event<ClarificationCreatedEvent> clarificationCreatedEvent;
    private PersistZepClarificationService service;

    @BeforeEach
    void setUp() {
        monthEndClarificationRepository = mock(MonthEndClarificationRepository.class);
        clarificationCreatedEvent = mock(Event.class);
        service = new PersistZepClarificationService(
                monthEndClarificationRepository,
                clock,
                clarificationCreatedEvent
        );
    }

    @Test
    void persist_shouldSaveClarificationAndFireCreatedEvent() {
        service.persist(
                month,
                employeeContext(),
                creatorId,
                "<table>...</table>"
        );

        verify(monthEndClarificationRepository).save(argThat(clarification ->
                clarification.sourceSystem() == SourceSystem.ZEP
                        && clarification.projectId().equals(projectId)
                        && clarification.subjectEmployeeId().equals(subjectEmployeeId)
                        && clarification.createdBy().equals(creatorId)
                        && clarification.text().equals("<table>...</table>")
                        && clarification.createdAt().equals(clock.instant())
        ));
        verify(clarificationCreatedEvent).fire(argThat(event ->
                event.sourceSystem() == SourceSystem.ZEP
                        && event.creator().equals(creatorId)
                        && event.subjectEmployeeId().equals(subjectEmployeeId)
                        && event.text().equals("<table>...</table>")
        ));
    }

    private MonthEndEmployeeProjectContext employeeContext() {
        return new MonthEndEmployeeProjectContext(
                month,
                new MonthEndProjectSnapshot(projectId, 77, "Gepardec", true, Set.of(creatorId)),
                new UserRef(subjectEmployeeId, FullName.of("Max", "Mustermann"), ZepUsername.of("max.mustermann")),
                Set.of(creatorId)
        );
    }
}

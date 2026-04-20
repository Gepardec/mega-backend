package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationUpdatedEvent;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.event.Event;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateMonthEndClarificationServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId employeeId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadId = UserId.of(Instancio.create(UUID.class));
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-31T09:00:00Z"), ZoneOffset.UTC);

    private MonthEndClarificationRepository clarificationRepository;
    private Event<ClarificationUpdatedEvent> clarificationUpdatedEvent;
    private UpdateMonthEndClarificationService service;

    @BeforeEach
    void setUp() {
        clarificationRepository = mock(MonthEndClarificationRepository.class);
        clarificationUpdatedEvent = mock(Event.class);
        service = new UpdateMonthEndClarificationService(clarificationRepository, clock, clarificationUpdatedEvent);
    }

    @Test
    void updateText_shouldPersistEditedClarification_whenActorIsCreator() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                employeeId,
                Set.of(leadId),
                "Original text",
                Instant.parse("2026-03-31T08:00:00Z")
        );
        when(clarificationRepository.findById(clarification.id())).thenReturn(Optional.of(clarification));

        MonthEndClarification result = service.updateText(clarification.id(), employeeId, "Updated text");

        assertThat(result.text()).isEqualTo("Updated text");
        assertThat(result.lastModifiedAt()).isEqualTo(clock.instant());
        verify(clarificationRepository).save(result);
        verify(clarificationUpdatedEvent).fire(argThat(event ->
                event.clarificationId().equals(result.id())
                        && event.actorId().equals(employeeId)
                        && event.subjectEmployeeId().equals(employeeId)
                        && event.text().equals("Updated text")
        ));
    }

    @Test
    void updateText_shouldThrow_whenActorIsNotCreator() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                employeeId,
                Set.of(leadId),
                "Original text",
                Instant.parse("2026-03-31T08:00:00Z")
        );
        when(clarificationRepository.findById(clarification.id())).thenReturn(Optional.of(clarification));

        assertThatThrownBy(() -> service.updateText(clarification.id(), leadId, "Lead trying to edit"))
                .isInstanceOf(com.gepardec.mega.hexagon.monthend.domain.error.MonthEndActorNotAuthorizedException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void updateText_shouldThrow_whenLeadCreatedClarificationAndOtherLeadAttemptsEdit() {
        UserId leadA = UserId.of(Instancio.create(UUID.class));
        UserId leadB = UserId.of(Instancio.create(UUID.class));
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                null,
                leadA,
                Set.of(leadA, leadB),
                "Project-level note",
                Instant.parse("2026-03-31T08:00:00Z")
        );
        when(clarificationRepository.findById(clarification.id())).thenReturn(Optional.of(clarification));

        assertThatThrownBy(() -> service.updateText(clarification.id(), leadB, "Trying to edit"))
                .isInstanceOf(com.gepardec.mega.hexagon.monthend.domain.error.MonthEndActorNotAuthorizedException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void updateText_shouldThrow_whenClarificationIsMissing() {
        MonthEndClarificationId clarificationId = MonthEndClarificationId.generate();
        when(clarificationRepository.findById(clarificationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateText(clarificationId, employeeId, "Updated text"))
                .isInstanceOf(MonthEndClarificationNotFoundException.class)
                .hasMessageContaining("not found");
    }
}

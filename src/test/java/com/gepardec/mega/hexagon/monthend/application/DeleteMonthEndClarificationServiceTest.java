package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndActorNotAuthorizedException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationClosedException;
import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndClarificationNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationDeletedEvent;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.event.Event;
import org.assertj.core.api.ThrowableAssert;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteMonthEndClarificationServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId creatorId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadId = UserId.of(Instancio.create(UUID.class));
    private final Instant createdAt = Instant.parse("2026-03-31T08:00:00Z");

    private MonthEndClarificationRepository clarificationRepository;
    private Event<ClarificationDeletedEvent> clarificationDeletedEvent;
    private DeleteMonthEndClarificationService service;

    @BeforeEach
    void setUp() {
        clarificationRepository = mock(MonthEndClarificationRepository.class);
        clarificationDeletedEvent = mock(Event.class);
        service = new DeleteMonthEndClarificationService(clarificationRepository, clarificationDeletedEvent);
    }

    @Test
    void delete_shouldDeleteClarification_whenActorIsCreatorAndClarificationIsOpen() {
        MonthEndClarification clarification = openEmployeeClarification();
        when(clarificationRepository.findById(clarification.id())).thenReturn(Optional.of(clarification));

        service.delete(clarification.id(), creatorId);

        verify(clarificationRepository).delete(clarification.id());
        verify(clarificationDeletedEvent).fire(argThat(event -> event.clarificationId().equals(clarification.id())));
    }

    @Test
    void delete_shouldThrow_whenActorIsNotCreator() {
        MonthEndClarification clarification = openEmployeeClarification();
        when(clarificationRepository.findById(clarification.id())).thenReturn(Optional.of(clarification));

        ThrowableAssert.ThrowingCallable throwingCallable = () -> service.delete(clarification.id(), leadId);

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(MonthEndActorNotAuthorizedException.class)
                .hasMessageContaining("not allowed");

        verify(clarificationRepository, never()).delete(clarification.id());
    }

    @Test
    void delete_shouldThrow_whenClarificationIsDone() {
        MonthEndClarification done = openEmployeeClarification()
                .resolve(leadId, "Resolved", createdAt.plusSeconds(60));
        when(clarificationRepository.findById(done.id())).thenReturn(Optional.of(done));

        ThrowableAssert.ThrowingCallable throwingCallable = () -> service.delete(done.id(), creatorId);

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(MonthEndClarificationClosedException.class)
                .hasMessageContaining("cannot be deleted");

        verify(clarificationRepository, never()).delete(done.id());
    }

    @Test
    void delete_shouldThrow_whenClarificationIsNotFound() {
        MonthEndClarificationId id = MonthEndClarificationId.generate();
        when(clarificationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id, creatorId))
                .isInstanceOf(MonthEndClarificationNotFoundException.class)
                .hasMessageContaining("not found");
    }

    private MonthEndClarification openEmployeeClarification() {
        return MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                creatorId,
                creatorId,
                Set.of(leadId),
                "Please verify this.",
                createdAt
        );
    }
}

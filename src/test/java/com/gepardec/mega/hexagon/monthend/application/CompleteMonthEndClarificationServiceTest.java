package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationStatus;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompleteMonthEndClarificationServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId employeeId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadA = UserId.of(Instancio.create(UUID.class));
    private final UserId leadB = UserId.of(Instancio.create(UUID.class));
    private final Clock clock = Clock.fixed(Instant.parse("2026-03-31T10:00:00Z"), ZoneOffset.UTC);

    private MonthEndClarificationRepository clarificationRepository;
    private CompleteMonthEndClarificationService service;

    @BeforeEach
    void setUp() {
        clarificationRepository = mock(MonthEndClarificationRepository.class);
        service = new CompleteMonthEndClarificationService(clarificationRepository, clock);
    }

    @Test
    void complete_shouldPersistResolvedClarification_whenActorIsAllowed() {
        MonthEndClarification clarification = employeeCreatedClarification();
        when(clarificationRepository.findById(clarification.id())).thenReturn(Optional.of(clarification));

        MonthEndClarification result = service.complete(clarification.id(), leadA, "Handled");

        assertThat(result.status()).isEqualTo(MonthEndClarificationStatus.DONE);
        assertThat(result.resolvedBy()).isEqualTo(leadA);
        assertThat(result.resolvedAt()).isEqualTo(clock.instant());
        assertThat(result.resolutionNote()).isEqualTo("Handled");
        verify(clarificationRepository).save(result);
    }

    @Test
    void complete_shouldNotPersistAgain_whenClarificationAlreadyDone() {
        MonthEndClarification doneClarification = employeeCreatedClarification()
                .resolve(leadA, "Handled", Instant.parse("2026-03-31T09:00:00Z"));
        when(clarificationRepository.findById(doneClarification.id())).thenReturn(Optional.of(doneClarification));

        MonthEndClarification result = service.complete(doneClarification.id(), leadB, "Later handler");

        assertThat(result.resolvedBy()).isEqualTo(leadA);
        verify(clarificationRepository, never()).save(doneClarification);
    }

    @Test
    void complete_shouldThrow_whenClarificationIsMissing() {
        MonthEndClarificationId clarificationId = MonthEndClarificationId.generate();
        when(clarificationRepository.findById(clarificationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.complete(clarificationId, leadA, "Handled"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    private MonthEndClarification employeeCreatedClarification() {
        return MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(leadA, leadB),
                "Please verify this.",
                Instant.parse("2026-03-31T08:00:00Z")
        );
    }
}

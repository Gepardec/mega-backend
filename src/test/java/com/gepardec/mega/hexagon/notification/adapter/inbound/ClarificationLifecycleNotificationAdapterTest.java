package com.gepardec.mega.hexagon.notification.adapter.inbound;

import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationCompletedEvent;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationCreatedEvent;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationDeletedEvent;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationUpdatedEvent;
import com.gepardec.mega.hexagon.monthend.domain.event.ZepMailProcessingFailedEvent;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.SourceSystem;
import com.gepardec.mega.hexagon.notification.application.port.outbound.NotificationMailPort;
import com.gepardec.mega.hexagon.notification.domain.ClarificationNotificationType;
import com.gepardec.mega.hexagon.shared.domain.SystemActor;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ClarificationLifecycleNotificationAdapterTest {

    private final MonthEndClarificationId clarificationId = MonthEndClarificationId.generate();
    private final UserId creatorId = UserId.of(Instancio.create(UUID.class));
    private final UserId subjectEmployeeId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadAId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadBId = UserId.of(Instancio.create(UUID.class));

    private NotificationMailPort notificationMailPort;
    private UserRepository userRepository;
    private ClarificationLifecycleNotificationAdapter adapter;

    @BeforeEach
    void setUp() {
        notificationMailPort = mock(NotificationMailPort.class);
        userRepository = mock(UserRepository.class);
        adapter = new ClarificationLifecycleNotificationAdapter(notificationMailPort, userRepository);

        when(userRepository.findById(creatorId)).thenReturn(Optional.of(user(creatorId, "Clara", "Creator")));
        when(userRepository.findById(subjectEmployeeId)).thenReturn(Optional.of(user(subjectEmployeeId, "Elli", "Employee")));
        when(userRepository.findById(leadAId)).thenReturn(Optional.of(user(leadAId, "Lara", "Lead")));
        when(userRepository.findById(leadBId)).thenReturn(Optional.of(user(leadBId, "Luca", "Lead")));
    }

    @Test
    void onClarificationCreated_shouldSendClarificationCreatedMailForMegaClarification() {
        adapter.onClarificationCreated(new ClarificationCreatedEvent(
                clarificationId,
                SourceSystem.MEGA,
                creatorId,
                subjectEmployeeId,
                "Please review this.",
                Set.of(leadAId, leadBId)
        ));

        verify(notificationMailPort).send(
                eq(ClarificationNotificationType.CLARIFICATION_CREATED),
                eq(Email.of("elli.employee@example.com")),
                eq("Elli"),
                eq(Locale.GERMAN),
                argThat(parameters ->
                        "Clara".equals(parameters.get("$creator$"))
                                && "Please review this.".equals(parameters.get("$comment$"))
                ),
                eq(List.of("Clara"))
        );
    }

    @Test
    void onClarificationCreated_shouldSuppressMailForZepClarification() {
        adapter.onClarificationCreated(new ClarificationCreatedEvent(
                clarificationId,
                SourceSystem.ZEP,
                creatorId,
                subjectEmployeeId,
                "Imported from ZEP.",
                Set.of(leadAId, leadBId)
        ));

        verifyNoInteractions(notificationMailPort);
    }

    @Test
    void onClarificationCompleted_shouldSendClarificationCompletedMailToCreator() {
        adapter.onClarificationCompleted(new ClarificationCompletedEvent(
                clarificationId,
                creatorId,
                subjectEmployeeId,
                "Please review this.",
                UserId.of(Instancio.create(UUID.class))
        ));

        verify(notificationMailPort).send(
                eq(ClarificationNotificationType.CLARIFICATION_COMPLETED),
                eq(Email.of("clara.creator@example.com")),
                eq("Elli"),
                eq(Locale.GERMAN),
                argThat(parameters ->
                        "Clara".equals(parameters.get("$recipient$"))
                                && "Please review this.".equals(parameters.get("$comment$"))
                ),
                eq(List.of("Clara"))
        );
    }

    @Test
    void onClarificationDeleted_shouldSendClarificationDeletedMailToSubjectEmployee() {
        adapter.onClarificationDeleted(new ClarificationDeletedEvent(
                clarificationId,
                creatorId,
                subjectEmployeeId,
                "Please review this.",
                Set.of(leadAId, leadBId)
        ));

        verify(notificationMailPort).send(
                eq(ClarificationNotificationType.CLARIFICATION_DELETED),
                eq(Email.of("elli.employee@example.com")),
                eq("Elli"),
                eq(Locale.GERMAN),
                argThat(parameters ->
                        "Clara".equals(parameters.get("$creator$"))
                                && "Please review this.".equals(parameters.get("$comment$"))
                ),
                eq(List.of("Clara"))
        );
    }

    @Test
    void onClarificationUpdated_shouldSendClarificationUpdatedMailToSubjectEmployee() {
        adapter.onClarificationUpdated(new ClarificationUpdatedEvent(
                clarificationId,
                creatorId,
                subjectEmployeeId,
                "Edited text.",
                Set.of(leadAId, leadBId)
        ));

        verify(notificationMailPort).send(
                eq(ClarificationNotificationType.CLARIFICATION_UPDATED),
                eq(Email.of("elli.employee@example.com")),
                eq("Elli"),
                eq(Locale.GERMAN),
                argThat(parameters ->
                        "Clara".equals(parameters.get("$creator$"))
                                && "Edited text.".equals(parameters.get("$comment$"))
                ),
                eq(List.of("Clara"))
        );
    }

    @Test
    void onClarificationUpdated_shouldSkipProjectLevelClarifications() {
        adapter.onClarificationUpdated(new ClarificationUpdatedEvent(
                clarificationId,
                creatorId,
                null,
                "Edited text.",
                Set.of(leadAId, leadBId)
        ));

        verifyNoInteractions(notificationMailPort);
    }

    @Test
    void onClarificationCreated_shouldFanOutToEligibleLeadsWhenSubjectEmployeeCreatesClarification() {
        adapter.onClarificationCreated(new ClarificationCreatedEvent(
                clarificationId,
                SourceSystem.MEGA,
                subjectEmployeeId,
                subjectEmployeeId,
                "Please review this.",
                Set.of(leadAId, leadBId)
        ));

        assertFanOutNotification(
                ClarificationNotificationType.CLARIFICATION_CREATED,
                "Please review this."
        );
    }

    @Test
    void onClarificationUpdated_shouldFanOutToEligibleLeadsWhenSubjectEmployeeUpdatesClarification() {
        adapter.onClarificationUpdated(new ClarificationUpdatedEvent(
                clarificationId,
                subjectEmployeeId,
                subjectEmployeeId,
                "Edited text.",
                Set.of(leadAId, leadBId)
        ));

        assertFanOutNotification(
                ClarificationNotificationType.CLARIFICATION_UPDATED,
                "Edited text."
        );
    }

    @Test
    void onClarificationDeleted_shouldFanOutToEligibleLeadsWhenSubjectEmployeeDeletesClarification() {
        adapter.onClarificationDeleted(new ClarificationDeletedEvent(
                clarificationId,
                subjectEmployeeId,
                subjectEmployeeId,
                "Deleted text.",
                Set.of(leadAId, leadBId)
        ));

        assertFanOutNotification(
                ClarificationNotificationType.CLARIFICATION_DELETED,
                "Deleted text."
        );
    }

    @Test
    void onClarificationCompleted_shouldSkipWhenCreatorIsSystemActor() {
        adapter.onClarificationCompleted(new ClarificationCompletedEvent(
                clarificationId,
                SystemActor.USER_ID,
                subjectEmployeeId,
                "Completed by system.",
                creatorId
        ));

        verifyNoInteractions(notificationMailPort);
    }

    @Test
    void onZepMailProcessingFailed_shouldSendErrorMailWhenCreatorEmailIsPresent() {
        adapter.onZepMailProcessingFailed(new ZepMailProcessingFailedEvent(
                creatorId,
                Email.of("clara.creator@example.com"),
                "Max Mustermann",
                "boom",
                "Subject: ZEP\nBody: <table>...</table>"
        ));

        verify(notificationMailPort).send(
                eq(ClarificationNotificationType.ZEP_CLARIFICATION_PROCESSING_ERROR),
                eq(Email.of("clara.creator@example.com")),
                eq("Clara"),
                eq(Locale.GERMAN),
                argThat(parameters ->
                        "Clara".equals(parameters.get("$recipient$"))
                                && "boom".equals(parameters.get("$comment$"))
                                && "Subject: ZEP\nBody: <table>...</table>".equals(parameters.get("$originalMail$"))
                ),
                eq(List.of("Max Mustermann"))
        );
    }

    @Test
    void onZepMailProcessingFailed_shouldSkipWhenCreatorEmailIsMissing() {
        adapter.onZepMailProcessingFailed(new ZepMailProcessingFailedEvent(
                creatorId,
                null,
                "Max Mustermann",
                "boom",
                "raw"
        ));

        verifyNoInteractions(notificationMailPort);
    }

    private User user(UserId userId, String firstname, String lastname) {
        String username = firstname.toLowerCase() + "." + lastname.toLowerCase();
        return new User(
                userId,
                Email.of(username + "@example.com"),
                FullName.of(firstname, lastname),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2024, 1, 1), null)),
                Set.of(Role.EMPLOYEE)
        );
    }

    private void assertFanOutNotification(ClarificationNotificationType notificationType, String comment) {
        ArgumentCaptor<Email> recipientEmailCaptor = ArgumentCaptor.forClass(Email.class);
        ArgumentCaptor<String> recipientFirstNameCaptor = ArgumentCaptor.forClass(String.class);

        verify(notificationMailPort, times(2)).send(
                eq(notificationType),
                recipientEmailCaptor.capture(),
                recipientFirstNameCaptor.capture(),
                eq(Locale.GERMAN),
                argThat(parameters ->
                        "Elli".equals(parameters.get("$creator$"))
                                && comment.equals(parameters.get("$comment$"))
                ),
                eq(List.of("Elli"))
        );

        assertThat(recipientEmailCaptor.getAllValues()).containsExactlyInAnyOrder(
                Email.of("lara.lead@example.com"),
                Email.of("luca.lead@example.com")
        );
        assertThat(recipientFirstNameCaptor.getAllValues()).containsExactlyInAnyOrder("Lara", "Luca");
    }
}

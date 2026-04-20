package com.gepardec.mega.hexagon.notification.adapter.inbound;

import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationCompletedEvent;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationCreatedEvent;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationDeletedEvent;
import com.gepardec.mega.hexagon.monthend.domain.event.ClarificationUpdatedEvent;
import com.gepardec.mega.hexagon.monthend.domain.event.ZepMailProcessingFailedEvent;
import com.gepardec.mega.hexagon.notification.domain.ClarificationNotificationType;
import com.gepardec.mega.hexagon.notification.domain.port.outbound.NotificationMailPort;
import com.gepardec.mega.hexagon.shared.domain.model.SourceSystem;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class ClarificationLifecycleNotificationAdapter {

    private static final Locale DEFAULT_LOCALE = Locale.GERMAN;
    private static final String PARAM_COMMENT = "$comment$";
    private static final String PARAM_CREATOR = "$creator$";
    private static final String PARAM_ORIGINAL_MAIL = "$originalMail$";
    private static final String PARAM_RECIPIENT = "$recipient$";
    private static final String SUBJECT_EMPLOYEE = "subject employee";
    private static final String CREATOR = "creator";

    private final NotificationMailPort notificationMailPort;
    private final UserRepository userRepository;

    @Inject
    public ClarificationLifecycleNotificationAdapter(
            NotificationMailPort notificationMailPort,
            UserRepository userRepository
    ) {
        this.notificationMailPort = notificationMailPort;
        this.userRepository = userRepository;
    }

    void onClarificationCreated(@Observes ClarificationCreatedEvent event) {
        if (event.sourceSystem() == SourceSystem.ZEP) {
            Log.infof("Suppressing clarification-created notification for ZEP clarification %s", event.clarificationId().value());
            return;
        }
        if (event.subjectEmployeeId() == null) {
            Log.infof("Skipping clarification-created notification for project-level clarification %s", event.clarificationId().value());
            return;
        }

        User recipient = loadUser(event.subjectEmployeeId(), SUBJECT_EMPLOYEE);
        User creator = loadUser(event.creator(), CREATOR);

        notificationMailPort.send(
                ClarificationNotificationType.CLARIFICATION_CREATED,
                recipient.email(),
                recipient.name().firstname(),
                DEFAULT_LOCALE,
                Map.of(
                        PARAM_CREATOR, creator.name().firstname(),
                        PARAM_COMMENT, event.text()
                ),
                List.of(creator.name().firstname())
        );
    }

    void onClarificationCompleted(@Observes ClarificationCompletedEvent event) {
        if (event.subjectEmployeeId() == null) {
            Log.infof("Skipping clarification-completed notification for project-level clarification %s", event.clarificationId().value());
            return;
        }

        User creator = loadUser(event.creator(), CREATOR);
        User subjectEmployee = loadUser(event.subjectEmployeeId(), SUBJECT_EMPLOYEE);

        notificationMailPort.send(
                ClarificationNotificationType.CLARIFICATION_COMPLETED,
                creator.email(),
                subjectEmployee.name().firstname(),
                DEFAULT_LOCALE,
                Map.of(
                        PARAM_RECIPIENT, creator.name().firstname(),
                        PARAM_COMMENT, event.text()
                ),
                List.of(creator.name().firstname())
        );
    }

    void onClarificationDeleted(@Observes ClarificationDeletedEvent event) {
        if (event.subjectEmployeeId() == null) {
            Log.infof("Skipping clarification-deleted notification for project-level clarification %s", event.clarificationId().value());
            return;
        }

        User recipient = loadUser(event.subjectEmployeeId(), SUBJECT_EMPLOYEE);
        User creator = loadUser(event.creator(), CREATOR);

        notificationMailPort.send(
                ClarificationNotificationType.CLARIFICATION_DELETED,
                recipient.email(),
                recipient.name().firstname(),
                DEFAULT_LOCALE,
                Map.of(
                        PARAM_CREATOR, creator.name().firstname(),
                        PARAM_COMMENT, event.text()
                ),
                List.of(creator.name().firstname())
        );
    }

    void onClarificationUpdated(@Observes ClarificationUpdatedEvent event) {
        if (event.subjectEmployeeId() == null) {
            Log.infof("Skipping clarification-updated notification for project-level clarification %s", event.clarificationId().value());
            return;
        }

        User recipient = loadUser(event.subjectEmployeeId(), SUBJECT_EMPLOYEE);
        User actor = loadUser(event.actorId(), "actor");

        notificationMailPort.send(
                ClarificationNotificationType.CLARIFICATION_UPDATED,
                recipient.email(),
                recipient.name().firstname(),
                DEFAULT_LOCALE,
                Map.of(
                        PARAM_CREATOR, actor.name().firstname(),
                        PARAM_COMMENT, event.text()
                ),
                List.of(actor.name().firstname())
        );
    }

    void onZepMailProcessingFailed(@Observes ZepMailProcessingFailedEvent event) {
        if (event.creatorEmail() == null) {
            Log.info("Skipping ZEP processing failure notification because creator email is missing");
            return;
        }

        String recipientFirstName = resolveRecipientFirstName(event);

        notificationMailPort.send(
                ClarificationNotificationType.ZEP_CLARIFICATION_PROCESSING_ERROR,
                event.creatorEmail(),
                recipientFirstName,
                DEFAULT_LOCALE,
                Map.of(
                        PARAM_RECIPIENT, recipientFirstName,
                        PARAM_COMMENT, normalizeErrorMessage(event.errorMessage()),
                        PARAM_ORIGINAL_MAIL, event.rawMailContent()
                ),
                List.of(normalizeOriginalRecipient(event.originalRecipient()))
        );
    }

    private User loadUser(UserId userId, String role) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unable to resolve %s user %s".formatted(role, userId.value())));
    }

    private String resolveRecipientFirstName(ZepMailProcessingFailedEvent event) {
        if (event.creatorUserId() == null) {
            return event.creatorEmail().value();
        }

        return userRepository.findById(event.creatorUserId())
                .map(user -> user.name().firstname())
                .orElse(event.creatorEmail().value());
    }

    private String normalizeErrorMessage(String errorMessage) {
        return errorMessage == null || errorMessage.isBlank() ? "Unknown error" : errorMessage;
    }

    private String normalizeOriginalRecipient(String originalRecipient) {
        return originalRecipient == null || originalRecipient.isBlank() ? "unknown" : originalRecipient;
    }
}

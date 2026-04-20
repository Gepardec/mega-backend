package com.gepardec.mega.hexagon.notification.domain.port.outbound;

import com.gepardec.mega.hexagon.notification.domain.MailNotificationId;
import com.gepardec.mega.hexagon.shared.domain.model.Email;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface NotificationMailPort {

    default void send(MailNotificationId mailId, Email recipientEmail, String recipientFirstName, Locale locale) {
        send(mailId, recipientEmail, recipientFirstName, locale, Map.of(), List.of());
    }

    void send(
            MailNotificationId mailId,
            Email recipientEmail,
            String recipientFirstName,
            Locale locale,
            Map<String, String> templateParameters,
            List<String> subjectParameters
    );
}

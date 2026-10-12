package com.gepardec.mega.hexagon.notification.domain;

public sealed interface MailNotificationId permits ReminderType, ClarificationNotificationType {

    String name();
}

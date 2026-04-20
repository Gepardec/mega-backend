package com.gepardec.mega.hexagon.notification.application;

import com.gepardec.mega.hexagon.notification.application.port.inbound.SendScheduledRemindersUseCase;
import com.gepardec.mega.hexagon.notification.domain.ReminderType;
import com.gepardec.mega.hexagon.notification.domain.port.outbound.NotificationMailPort;
import com.gepardec.mega.hexagon.notification.domain.service.ReminderSchedulePolicy;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional(Transactional.TxType.SUPPORTS)
public class SendScheduledRemindersService implements SendScheduledRemindersUseCase {

    private static final Locale DEFAULT_LOCALE = Locale.GERMAN;

    private final ReminderSchedulePolicy reminderSchedulePolicy;
    private final UserRepository userRepository;
    private final NotificationMailPort notificationMailPort;

    @Inject
    public SendScheduledRemindersService(
            ReminderSchedulePolicy reminderSchedulePolicy,
            UserRepository userRepository,
            NotificationMailPort notificationMailPort
    ) {
        this.reminderSchedulePolicy = reminderSchedulePolicy;
        this.userRepository = userRepository;
        this.notificationMailPort = notificationMailPort;
    }

    @Override
    public void send(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        Set<ReminderType> dueReminders = reminderSchedulePolicy.getRemindersForDate(date);
        if (dueReminders.isEmpty()) {
            Log.infof("No scheduled reminders due on %s", date);
            return;
        }

        int sentCount = 0;
        for (ReminderType reminderType : dueReminders) {
            List<User> recipients = userRepository.findByRole(reminderType.targetRole()).stream()
                    .filter(user -> user.isActiveOn(date))
                    .toList();
            if (recipients.isEmpty()) {
                Log.warnf("No active users found for reminder %s and role %s", reminderType, reminderType.targetRole());
                continue;
            }

            for (User recipient : recipients) {
                notificationMailPort.send(
                        reminderType,
                        recipient.email(),
                        recipient.name().firstname(),
                        DEFAULT_LOCALE
                );
                sentCount++;
            }
        }

        Log.infof(
                "Sent %d scheduled reminder mail(s) on %s for %s",
                sentCount,
                date,
                dueReminders.stream().map(ReminderType::name).collect(Collectors.joining(", "))
        );
    }
}

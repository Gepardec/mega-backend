package com.gepardec.mega.hexagon.notification.application;

import com.gepardec.mega.hexagon.notification.domain.ReminderType;
import com.gepardec.mega.hexagon.notification.domain.port.outbound.NotificationMailPort;
import com.gepardec.mega.hexagon.notification.domain.service.ReminderSchedulePolicy;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SendScheduledRemindersServiceTest {

    private ReminderSchedulePolicy reminderSchedulePolicy;
    private UserRepository userRepository;
    private NotificationMailPort notificationMailPort;
    private SendScheduledRemindersService service;

    @BeforeEach
    void setUp() {
        reminderSchedulePolicy = mock(ReminderSchedulePolicy.class);
        userRepository = mock(UserRepository.class);
        notificationMailPort = mock(NotificationMailPort.class);
        service = new SendScheduledRemindersService(reminderSchedulePolicy, userRepository, notificationMailPort);
    }

    @Test
    void send_shouldDispatchReminderMailsToActiveUsersForEachDueReminder() {
        LocalDate today = LocalDate.of(2026, 3, 6);
        User activeEmployee = user("employee", "Emma", "Employee", Set.of(Role.EMPLOYEE), LocalDate.of(2024, 1, 1), null);
        User inactiveEmployee = user("inactive", "Ina", "Inactive", Set.of(Role.EMPLOYEE), LocalDate.of(2024, 1, 1), LocalDate.of(2025, 12, 31));
        User projectLead = user("lead", "Paul", "Lead", Set.of(Role.PROJECT_LEAD), LocalDate.of(2024, 1, 1), null);

        when(reminderSchedulePolicy.getRemindersForDate(today))
                .thenReturn(Set.of(ReminderType.EMPLOYEE_CHECK_PROJECTTIME, ReminderType.PL_PROJECT_CONTROLLING));
        when(userRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of(activeEmployee, inactiveEmployee));
        when(userRepository.findByRole(Role.PROJECT_LEAD)).thenReturn(List.of(projectLead));

        service.send(today);

        verify(notificationMailPort).send(
                ReminderType.EMPLOYEE_CHECK_PROJECTTIME,
                activeEmployee.email(),
                activeEmployee.name().firstname(),
                Locale.GERMAN
        );
        verify(notificationMailPort).send(
                ReminderType.PL_PROJECT_CONTROLLING,
                projectLead.email(),
                projectLead.name().firstname(),
                Locale.GERMAN
        );
        verify(notificationMailPort, never()).send(
                ReminderType.EMPLOYEE_CHECK_PROJECTTIME,
                inactiveEmployee.email(),
                inactiveEmployee.name().firstname(),
                Locale.GERMAN
        );
    }

    @Test
    void send_shouldDoNothingWhenNoRemindersAreDue() {
        LocalDate today = LocalDate.of(2026, 3, 7);
        when(reminderSchedulePolicy.getRemindersForDate(today)).thenReturn(Set.of());

        service.send(today);

        verifyNoInteractions(userRepository, notificationMailPort);
    }

    @Test
    void send_shouldSkipReminderWhenNoRecipientsExistForRole() {
        LocalDate today = LocalDate.of(2026, 3, 8);
        when(reminderSchedulePolicy.getRemindersForDate(today)).thenReturn(Set.of(ReminderType.OM_RELEASE));
        when(userRepository.findByRole(Role.OFFICE_MANAGEMENT)).thenReturn(List.of());

        service.send(today);

        verify(userRepository).findByRole(Role.OFFICE_MANAGEMENT);
        verifyNoInteractions(notificationMailPort);
    }

    private User user(
            String username,
            String firstname,
            String lastname,
            Set<Role> roles,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new User(
                UserId.of(Instancio.create(UUID.class)),
                Email.of(username + "@example.com"),
                FullName.of(firstname, lastname),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(startDate, endDate)),
                roles
        );
    }
}

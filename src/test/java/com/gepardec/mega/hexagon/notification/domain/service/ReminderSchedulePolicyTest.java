package com.gepardec.mega.hexagon.notification.domain.service;

import com.gepardec.mega.hexagon.notification.domain.ReminderType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ReminderSchedulePolicyTest {

    private final ReminderSchedulePolicy reminderSchedulePolicy = new ReminderSchedulePolicy();

    @ParameterizedTest
    @MethodSource("scheduledReminderDates")
    void getRemindersForDate_shouldReturnExpectedReminderTypes(LocalDate date, Set<ReminderType> expectedReminderTypes) {
        assertThat(reminderSchedulePolicy.getRemindersForDate(date)).isEqualTo(expectedReminderTypes);
    }

    private static Stream<Arguments> scheduledReminderDates() {
        return Stream.of(
                Arguments.of(LocalDate.of(2019, 11, 6), Set.of(ReminderType.OM_CONTROL_EMPLOYEES_CONTENT)),
                Arguments.of(LocalDate.of(2019, 11, 25), Set.of(ReminderType.OM_RELEASE)),
                Arguments.of(LocalDate.of(2020, 2, 17), Set.of(ReminderType.OM_ADMINISTRATIVE)),
                Arguments.of(LocalDate.of(2025, 5, 6), Set.of(ReminderType.OM_CONTROL_EMPLOYEES_CONTENT)),
                Arguments.of(LocalDate.of(2019, 11, 7), Set.of())
        );
    }
}

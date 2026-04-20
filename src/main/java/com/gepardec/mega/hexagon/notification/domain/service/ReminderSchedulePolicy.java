package com.gepardec.mega.hexagon.notification.domain.service;

import com.gepardec.mega.hexagon.notification.domain.ReminderType;
import com.gepardec.mega.hexagon.notification.domain.model.MailScheduleType;
import com.gepardec.mega.hexagon.shared.domain.util.OfficeCalendarUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@ApplicationScoped
public class ReminderSchedulePolicy {

    public Set<ReminderType> getRemindersForDate(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");

        return Arrays.stream(ReminderType.values())
                .filter(reminderType -> scheduledDateFor(date, reminderType).equals(date))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ReminderType.class)));
    }

    private LocalDate scheduledDateFor(LocalDate date, ReminderType reminderType) {
        if (reminderType.scheduleType() == MailScheduleType.DAY_OF_MONTH_BASED) {
            return nextWorkingDay(date.withDayOfMonth(reminderType.dayOffset()));
        }
        if (reminderType.dayOffset() > 0) {
            return addWorkingDays(firstWorkingDayOfMonth(date), reminderType.dayOffset() - 1);
        }
        return subtractWorkingDaysFromMonthEnd(date, reminderType.dayOffset());
    }

    private LocalDate firstWorkingDayOfMonth(LocalDate date) {
        LocalDate firstDayOfMonth = date.with(TemporalAdjusters.firstDayOfMonth());
        return Stream.iterate(firstDayOfMonth, current -> current.plusDays(1))
                .dropWhile(current -> !OfficeCalendarUtil.isWorkingDay(current))
                .findFirst()
                .orElse(firstDayOfMonth);
    }

    private LocalDate nextWorkingDay(LocalDate date) {
        if (OfficeCalendarUtil.isWorkingDay(date)) {
            return date;
        }
        return addWorkingDays(date, 1);
    }

    LocalDate addWorkingDays(LocalDate date, int workingDaysToAdd) {
        if (workingDaysToAdd < 1) {
            return date;
        }

        LocalDate seed = date.plusDays(1);
        return Stream.iterate(seed, current -> current.plusDays(1))
                .filter(OfficeCalendarUtil::isWorkingDay)
                .limit(workingDaysToAdd)
                .max(Comparator.naturalOrder())
                .orElse(seed);
    }

    LocalDate subtractWorkingDaysFromMonthEnd(LocalDate date, int workingDaysToSubtract) {
        LocalDate seed = date.with(lastDayOfMonth());
        long daysToSubtract = Math.abs((long) workingDaysToSubtract);
        return Stream.iterate(seed, current -> current.minusDays(1))
                .filter(OfficeCalendarUtil::isWorkingDay)
                .limit(daysToSubtract)
                .min(Comparator.naturalOrder())
                .orElse(seed);
    }
}

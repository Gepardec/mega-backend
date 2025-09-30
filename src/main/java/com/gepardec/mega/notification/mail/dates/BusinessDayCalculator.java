package com.gepardec.mega.notification.mail.dates;

import com.gepardec.mega.notification.mail.Mail;
import com.gepardec.mega.notification.mail.MailType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil.isWorkingDay;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@ApplicationScoped
public class BusinessDayCalculator {

    @Inject
    Logger logger;

    public List<Mail> getRemindersForDate(LocalDate actualDate) {
        logger.info("starting getEventForDate with date {}", actualDate);

        Map<LocalDate, List<Mail>> remindersByDate = HashMap.newHashMap(0);
        LocalDate firstWorkingDayOfMonth = calcFirstWorkingDayOfMonthForDate(actualDate);

        Arrays.stream(Mail.values())
                .forEach(reminder ->
                        // create multi-value map for dates with multiple reminders
                        remindersByDate.computeIfAbsent(
                                        calcDateForReminder(firstWorkingDayOfMonth, reminder),
                                        k -> new ArrayList<>()
                                )
                                .add(reminder)
                );

        var relevantReminders = remindersByDate.getOrDefault(actualDate, Collections.emptyList());
        if (!relevantReminders.isEmpty()) {
            String mailNames = relevantReminders.stream().map(Mail::name).collect(Collectors.joining(", "));
            logger.info("Reminder(s) {} was/were calculated", mailNames);
        }

        return relevantReminders;
    }

    private LocalDate calcDateForReminder(LocalDate firstWorkingDayOfMonth, Mail mail) {
        if (mail.getType() == MailType.DAY_OF_MONTH_BASED) {
            return calcNextWorkingdayForDayOfMonth(firstWorkingDayOfMonth, mail.getDay());
        } else if (mail.getType() == MailType.WORKING_DAY_BASED) {
            if (mail.getDay() > 0) {
                return addWorkingdays(firstWorkingDayOfMonth, mail.getDay() - 1);
            } else {
                return removeWorkingdaysFromNextMonth(firstWorkingDayOfMonth, mail.getDay());
            }
        } else {
            return LocalDate.MIN;
        }
    }

    private LocalDate calcNextWorkingdayForDayOfMonth(LocalDate actualDate, int dayOfMonth) {
        LocalDate dayOfMonthDate = actualDate.withDayOfMonth(dayOfMonth);
        if (isWorkingDay(dayOfMonthDate)) {
            return dayOfMonthDate;
        } else {
            return addWorkingdays(dayOfMonthDate, 1);
        }
    }

    private LocalDate calcFirstWorkingDayOfMonthForDate(LocalDate date) {
        LocalDate firstDayOfMonth = date.with(TemporalAdjusters.firstDayOfMonth());
        return Stream.iterate(firstDayOfMonth, theDate -> theDate.plusDays(1))
                .dropWhile(theDate -> !isWorkingDay(theDate))
                .findFirst()
                .orElse(firstDayOfMonth);
    }

    LocalDate addWorkingdays(LocalDate date, int workdaysToAdd) {
        if (workdaysToAdd < 1) {
            return date;
        }

        // has to be the next day of the given date, because we want to ignore the given date
        LocalDate result = date.plusDays(1);
        return Stream.iterate(result, d -> d.plusDays(1))
                .filter(OfficeCalendarUtil::isWorkingDay)
                .limit(workdaysToAdd)
                .max(Comparator.naturalOrder())
                .orElse(result);
    }

    LocalDate removeWorkingdaysFromNextMonth(LocalDate date, int workingdaysToRemove) {
        // has to be the last day of given month, because we want to ignore the "seed" value of the stream
        LocalDate result = date.with(lastDayOfMonth());
        workingdaysToRemove = Math.abs(workingdaysToRemove);
        return Stream.iterate(result, d -> d.minusDays(1))
                .filter(OfficeCalendarUtil::isWorkingDay)
                .limit(workingdaysToRemove)
                .min(Comparator.naturalOrder())
                .orElse(result);
    }
}

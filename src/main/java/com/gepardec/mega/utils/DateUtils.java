package com.gepardec.mega.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.Objects;

public class DateUtils {

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());

    public static LocalDate toLocalDate(String dateAsString) {
        return LocalDate.parse(dateAsString, DEFAULT_DATE_FORMATTER);
    }

    public static String dateToString(LocalDate date) {
        return date.format(DEFAULT_DATE_FORMATTER);
    }


    /**
     * when we have to secure our tests with specific time, we could set it here
     *
     * @return
     */
    public static LocalDate now() {
        return LocalDate.now();
    }

    public static String getFirstDayOfFollowingMonth(String dateAsString) {
        Objects.requireNonNull(dateAsString, "Date must not be null!");
        return dateToString(
                toLocalDate(dateAsString)
                        .with(TemporalAdjusters.firstDayOfNextMonth()));
    }

    public static String getLastDayOfFollowingMonth(String dateAsString) {
        Objects.requireNonNull(dateAsString, "Date must not be null!");
        return dateToString(
                toLocalDate(dateAsString)
                        .plusMonths(1)
                        .with(TemporalAdjusters.lastDayOfMonth()));
    }

}

package com.gepardec.mega.hexagon.shared.domain.util;

import de.focus_shift.jollyday.core.Holiday;
import de.focus_shift.jollyday.core.HolidayCalendar;
import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class OfficeCalendarUtil {

    private static final HolidayManager HOLIDAY_MANAGER =
            HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.AUSTRIA));
    private static final Predicate<LocalDate> IS_WEEKEND =
            date -> date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    private static final Predicate<LocalDate> IS_HOLIDAY = OfficeCalendarUtil::isHoliday;
    private static final Predicate<LocalDate> IS_FRIDAY = date -> date.getDayOfWeek() == DayOfWeek.FRIDAY;

    private OfficeCalendarUtil() {
    }

    public static List<LocalDate> getWorkingDaysForYearMonth(YearMonth yearMonth) {
        return getWorkingDaysBetween(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    public static List<LocalDate> getWorkingDaysBetween(LocalDate from, LocalDate toInclusive) {
        return from.datesUntil(toInclusive.plusDays(1))
                .filter(IS_WEEKEND.or(IS_HOLIDAY).negate())
                .toList();
    }

    public static boolean isLastWorkingDayOfMonth(LocalDate date) {
        return getWorkingDaysForYearMonth(YearMonth.from(date)).getLast().equals(date);
    }

    public static boolean isHoliday(LocalDate date) {
        return HOLIDAY_MANAGER.isHoliday(date);
    }

    public static boolean isWorkingDay(LocalDate date) {
        return IS_WEEKEND.or(IS_HOLIDAY).negate().test(date);
    }

    public static boolean isFriday(LocalDate date) {
        return IS_FRIDAY.test(date);
    }

    public static Stream<LocalDate> getHolidaysForYear(int year) {
        return HOLIDAY_MANAGER.getHolidays(Year.of(year)).stream()
                .map(Holiday::getDate);
    }

    public static Stream<LocalDate> getHolidaysForMonth(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return HOLIDAY_MANAGER.getHolidays(startDate, endDate).stream()
                .map(Holiday::getDate);
    }
}

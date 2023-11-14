package com.gepardec.mega.notification.mail.dates;

import de.focus_shift.Holiday;
import de.focus_shift.HolidayCalendar;
import de.focus_shift.HolidayManager;
import de.focus_shift.ManagerParameters;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OfficeCalendarUtil {

    private static final HolidayManager HOLIDAY_MANAGER = HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.AUSTRIA));
    private static final Predicate<LocalDate> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    private static final Predicate<LocalDate> isHoliday = OfficeCalendarUtil::isHoliday;

    public static List<LocalDate> getWorkingDaysBetween(LocalDate startDate, LocalDate endDateInclusive) {
        return startDate.datesUntil(endDateInclusive.plusDays(1))
                .filter(isWeekend.or(isHoliday).negate())
                .collect(Collectors.toList());
    }

    public static boolean isHoliday(LocalDate date) {
        return HOLIDAY_MANAGER.isHoliday(date);
    }

    public static boolean isWorkingDay(LocalDate date) {
        return isWeekend.or(isHoliday).negate().test(date);
    }

    public static Stream<LocalDate> getHolidaysForYear(int year) {
        return HOLIDAY_MANAGER.getHolidays(year).stream().map(Holiday::getDate);
    }

    public static Stream<LocalDate> getHolidaysForMonth(YearMonth yearMonth) {
        LocalDate startDate = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1);
        LocalDate endDate = startDate.withDayOfMonth(yearMonth.lengthOfMonth());
        return HOLIDAY_MANAGER.getHolidays(startDate, endDate).stream().map(Holiday::getDate);
    }


}

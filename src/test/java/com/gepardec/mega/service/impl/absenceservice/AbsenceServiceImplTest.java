package com.gepardec.mega.service.impl.absenceservice;

import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.service.impl.AbsenceServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbsenceServiceImplTest {

    // October 2026: 1st is a Thursday, 26th (Nationalfeiertag) is a Monday holiday
    private static final YearMonth OCTOBER_2026 = YearMonth.of(2026, 10);

    private final AbsenceServiceImpl absenceService = new AbsenceServiceImpl();

    @Test
    void getNumberOfDaysAbsent_whenNoAbsences_thenReturnZero() {
        assertThat(absenceService.getNumberOfDaysAbsent(List.of(), OCTOBER_2026)).isZero();
    }

    @Test
    void getNumberOfDaysAbsent_whenAbsenceSpansWeekendAndHoliday_thenCountOnlyWorkingDays() {
        List<AbsenceTime> absences = List.of(absence(AbsenceType.VACATION_DAYS, date(10, 23), date(10, 27)));

        assertThat(absenceService.getNumberOfDaysAbsent(absences, OCTOBER_2026)).isEqualTo(2);
    }

    @Test
    void getNumberOfDaysAbsent_whenAbsenceExtendsIntoNextMonth_thenCountOnlyDaysInMonth() {
        List<AbsenceTime> absences = List.of(absence(AbsenceType.VACATION_DAYS, date(10, 29), LocalDate.of(2026, 11, 6)));

        assertThat(absenceService.getNumberOfDaysAbsent(absences, OCTOBER_2026)).isEqualTo(2);
    }

    @Test
    void getNumberOfDaysAbsent_whenAbsenceStartsInPreviousMonth_thenCountOnlyDaysInMonth() {
        List<AbsenceTime> absences = List.of(absence(AbsenceType.VACATION_DAYS, LocalDate.of(2026, 9, 28), date(10, 2)));

        assertThat(absenceService.getNumberOfDaysAbsent(absences, OCTOBER_2026)).isEqualTo(2);
    }

    @Test
    void getNumberOfDaysAbsent_whenAbsencesOverlap_thenCountEachDayOnce() {
        List<AbsenceTime> absences = List.of(
                absence(AbsenceType.HOME_OFFICE_DAYS, date(10, 5), date(10, 9)),
                absence(AbsenceType.VACATION_DAYS, date(10, 7), date(10, 7))
        );

        assertThat(absenceService.getNumberOfDaysAbsent(absences, OCTOBER_2026)).isEqualTo(5);
    }

    @Test
    void numberOfFridaysAbsent_whenNoAbsences_thenReturnZero() {
        assertThat(absenceService.numberOfFridaysAbsent(List.of(), OCTOBER_2026)).isZero();
    }

    @Test
    void numberOfFridaysAbsent_whenAbsenceExtendsIntoNextMonth_thenCountOnlyFridaysInMonth() {
        // Fridays: 30.10 (in month), 6.11 (outside)
        List<AbsenceTime> absences = List.of(absence(AbsenceType.VACATION_DAYS, date(10, 29), LocalDate.of(2026, 11, 6)));

        assertThat(absenceService.numberOfFridaysAbsent(absences, OCTOBER_2026)).isOne();
    }

    @Test
    void numberOfFridaysAbsent_whenAbsencesOverlapOnFriday_thenCountFridayOnce() {
        List<AbsenceTime> absences = List.of(
                absence(AbsenceType.HOME_OFFICE_DAYS, date(10, 5), date(10, 9)),
                absence(AbsenceType.VACATION_DAYS, date(10, 9), date(10, 9))
        );

        assertThat(absenceService.numberOfFridaysAbsent(absences, OCTOBER_2026)).isOne();
    }

    private static LocalDate date(int month, int day) {
        return LocalDate.of(2026, month, day);
    }

    private static AbsenceTime absence(AbsenceType type, LocalDate from, LocalDate to) {
        return AbsenceTime.builder()
                .reason(type.getAbsenceName())
                .fromDate(from)
                .toDate(to)
                .accepted(true)
                .build();
    }
}

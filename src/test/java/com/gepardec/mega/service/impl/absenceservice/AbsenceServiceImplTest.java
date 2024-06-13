package com.gepardec.mega.service.impl.absenceservice;

import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil;
import com.gepardec.mega.service.api.AbsenceService;
import com.gepardec.mega.service.api.DateHelperService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class AbsenceServiceImplTest {

    @Inject
    AbsenceService absenceService;

    @InjectMock
    DateHelperService dateHelperService;

    @Test
    void testNumberOfFridaysAbsent_whenFourFridaysAbsent_thenReturnThree() {
        when(dateHelperService.getFridaysInRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(1);

        int actual = absenceService.numberOfFridaysAbsent(createAbsenceTimeList());
        assertThat(actual).isEqualTo(3);
    }

    @Test
    void testNumberOfFridaysAbsent_whenNoFridaysAbsent_thenReturnZero() {
        when(dateHelperService.getFridaysInRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0);

        int actual = absenceService.numberOfFridaysAbsent(List.of());
        assertThat(actual).isZero();
    }

    @Test
    void testGetNumberOfDaysAbsent_whenThreeDaysAbsent_thenReturnThree() {
        try (MockedStatic<OfficeCalendarUtil> officeCalendarUtilMock = Mockito.mockStatic(OfficeCalendarUtil.class)) {

            officeCalendarUtilMock.when(() -> OfficeCalendarUtil.isWorkingDay(any(LocalDate.class)))
                    .thenReturn(true);
            officeCalendarUtilMock.when(() -> OfficeCalendarUtil.getHolidaysForMonth(any(YearMonth.class)))
                    .thenReturn(Stream.of());


            int actual = absenceService.getNumberOfDaysAbsent(createAbsencesForDaysAbsent(), LocalDate.of(2024, 4, 1));

            assertThat(actual).isEqualTo(5);
        }
    }

    @Test
    void testGetNumberOfDaysAbsent_whenTwoDaysAreHoliday_thenReturnNumberOfDaysWithoutTheseDay() {
        try (MockedStatic<OfficeCalendarUtil> officeCalendarUtilMock = Mockito.mockStatic(OfficeCalendarUtil.class)) {

            officeCalendarUtilMock.when(() -> OfficeCalendarUtil.isWorkingDay(any(LocalDate.class)))
                    .thenReturn(true);
            officeCalendarUtilMock.when(() -> OfficeCalendarUtil.getHolidaysForMonth(any(YearMonth.class)))
                    .thenReturn(Stream.of(
                            LocalDate.of(2024, 5, 6),
                            LocalDate.of(2024, 5, 10)
                    ));


            int actual = absenceService.getNumberOfDaysAbsent(List.of(
                    createAbsenceTime(AbsenceType.VACATION_DAYS, LocalDate.of(2024, 5, 6), LocalDate.of(2024, 5, 6)),
                    createAbsenceTime(AbsenceType.VACATION_DAYS, LocalDate.of(2024, 5, 9), LocalDate.of(2024, 5, 12))
            ), LocalDate.of(2024, 4, 1));

            assertThat(actual).isEqualTo(3);
        }
    }

    private List<AbsenceTime> createAbsencesForDaysAbsent() {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now();

        if (fromDate.getDayOfMonth() == 30 || fromDate.getDayOfMonth() == 31 || (fromDate.getMonthValue() == 2 && fromDate.getDayOfMonth() == 29)) {
            toDate = fromDate;
            fromDate = LocalDate.now().minusDays(2);
        } else {
            toDate = fromDate.plusDays(2);
        }


        return List.of(
                createAbsenceTime(AbsenceType.VACATION_DAYS, LocalDate.now(), LocalDate.now()),
                createAbsenceTime(AbsenceType.VACATION_DAYS, fromDate, toDate),
                createAbsenceTime(AbsenceType.VACATION_DAYS, LocalDate.now(), LocalDate.now())
        );
    }

    private List<AbsenceTime> createAbsenceTimeList() {

        return List.of(
                createAbsenceTime(AbsenceType.VACATION_DAYS, LocalDate.now(), LocalDate.now()),
                createAbsenceTime(AbsenceType.CONFERENCE_DAYS, LocalDate.now(), LocalDate.now()),
                createAbsenceTime(AbsenceType.PAID_SICK_LEAVE, LocalDate.now(), LocalDate.now())
        );
    }

    private AbsenceTime createAbsenceTime(AbsenceType absenceType, LocalDate from, LocalDate to) {
        return AbsenceTime.builder()
                .reason(absenceType.getAbsenceName())
                .fromDate(from)
                .toDate(to)
                .accepted(true)
                .build();
    }
}

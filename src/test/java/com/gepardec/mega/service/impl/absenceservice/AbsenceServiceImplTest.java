package com.gepardec.mega.service.impl.absenceservice;

import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.service.api.AbsenceService;
import com.gepardec.mega.service.api.DateHelperService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jdk.jfr.Name;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
public class AbsenceServiceImplTest {

    @Inject @Named("InternalAbsenceService")
    AbsenceService absenceService;

    @InjectMock
    DateHelperService dateHelperService;

    @Test
    void testNumberOfFridaysAbsent_whenFourFridaysAbsent_thenReturnFour(){
        when(dateHelperService.getFridaysInRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(1);

        int actual = absenceService.numberOfFridaysAbsent(createAbsenceTimeList());
        assertThat(actual).isEqualTo(4);
    }

    @Test
    void testNumberOfFridaysAbsent_whenNoFridaysAbsent_thenReturnZero(){
        when(dateHelperService.getFridaysInRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0);

        int actual = absenceService.numberOfFridaysAbsent(List.of());
        assertThat(actual).isZero();
    }

    @Test
    void testGetNumberOfDaysAbsent_whenFourDaysAbsent_thenReturnFour(){
        int actual = absenceService.getNumberOfDaysAbsent(createAbsencesForDaysAbsent(), LocalDate.of(2024, 4, 1));
        assertThat(actual).isEqualTo(6);
    }

    private List<AbsenceTime> createAbsencesForDaysAbsent(){
        LocalDate start = LocalDate.of(2024, 4, 2);

        return List.of(
            createAbsenceTime(AbsenceType.VACATION_DAYS, start, start),
            createAbsenceTime(AbsenceType.VACATION_DAYS, start.plusDays(2), start.plusDays(4)), //contains two weekend days
            createAbsenceTime(AbsenceType.VACATION_DAYS, start.plusDays(7), start.plusDays(9))
        );
    }

    private List<AbsenceTime> createAbsenceTimeList(){
        return List.of(
                createAbsenceTime(AbsenceType.VACATION_DAYS, LocalDate.now(), LocalDate.now()),
                createAbsenceTime(AbsenceType.CONFERENCE_DAYS, LocalDate.now(), LocalDate.now()),
                createAbsenceTime(AbsenceType.PAID_SICK_LEAVE, LocalDate.now(), LocalDate.now()),
                createAbsenceTime(AbsenceType.HOME_OFFICE_DAYS, LocalDate.now(), LocalDate.now())
        );
    }

    private AbsenceTime createAbsenceTime(AbsenceType absenceType, LocalDate from, LocalDate to){
        return AbsenceTime.builder()
                        .reason(absenceType.getAbsenceName())
                        .fromDate(from)
                        .toDate(to)
                        .accepted(true)
                        .build();
    }
}

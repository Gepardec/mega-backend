package com.gepardec.mega.service.impl.absenceservice;

import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.service.api.AbsenceService;
import com.gepardec.mega.service.api.DateHelperService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

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
    void testNumberOfFridaysAbsent_whenFourFridaysAbsent_thenReturnThree(){
        when(dateHelperService.getFridaysInRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(1);

        int actual = absenceService.numberOfFridaysAbsent(createAbsenceTimeList());
        assertThat(actual).isEqualTo(3);
    }

    @Test
    void testNumberOfFridaysAbsent_whenNoFridaysAbsent_thenReturnZero(){
        when(dateHelperService.getFridaysInRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0);

        int actual = absenceService.numberOfFridaysAbsent(List.of());
        assertThat(actual).isZero();
    }

    @Test
    void testGetNumberOfDaysAbsent_whenFourDaysAbsent_thenReturnThree(){
        int actual = absenceService.getNumberOfDaysAbsent(createAbsencesForDaysAbsent(), LocalDate.of(2024, 4, 1));
        assertThat(actual).isEqualTo(3);
    }

    private List<AbsenceTime> createAbsencesForDaysAbsent(){
        LocalDate now = LocalDate.now();

        return List.of(
            createAbsenceTime(AbsenceType.VACATION_DAYS, now, now),
            createAbsenceTime(AbsenceType.VACATION_DAYS, now, now),
            createAbsenceTime(AbsenceType.VACATION_DAYS, now, now)
        );
    }

    private List<AbsenceTime> createAbsenceTimeList(){
        return List.of(
                createAbsenceTime(AbsenceType.VACATION_DAYS, LocalDate.now(), LocalDate.now()),
                createAbsenceTime(AbsenceType.CONFERENCE_DAYS, LocalDate.now(), LocalDate.now()),
                createAbsenceTime(AbsenceType.PAID_SICK_LEAVE, LocalDate.now(), LocalDate.now())
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

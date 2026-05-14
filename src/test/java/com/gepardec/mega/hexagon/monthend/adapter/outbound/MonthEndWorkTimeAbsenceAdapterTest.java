package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetEmployeeAbsencesUseCase;
import com.gepardec.mega.hexagon.worktime.domain.model.Absence;
import com.gepardec.mega.hexagon.worktime.domain.model.AbsenceType;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthEndWorkTimeAbsenceAdapterTest {

    @Mock
    private GetEmployeeAbsencesUseCase getEmployeeAbsencesUseCase;

    @InjectMocks
    private MonthEndWorkTimeAbsenceAdapter adapter;

    @Test
    void findQualifyingAbsentDays_shouldFilterHomeOfficeAndExternalTrainingButKeepVacation() {
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        YearMonth month = YearMonth.of(2026, 3);
        LocalDate homeOfficeDay = LocalDate.of(2026, 3, 2);
        LocalDate externalTrainingDay = LocalDate.of(2026, 3, 3);
        LocalDate vacationDay = LocalDate.of(2026, 3, 4);
        when(getEmployeeAbsencesUseCase.getAbsences(employeeId, month)).thenReturn(List.of(
                new Absence(homeOfficeDay, AbsenceType.HOME_OFFICE),
                new Absence(externalTrainingDay, AbsenceType.EXTERNAL_TRAINING),
                new Absence(vacationDay, AbsenceType.VACATION)
        ));

        List<LocalDate> absentDays = adapter.findQualifyingAbsentDays(employeeId, month);

        assertThat(absentDays).containsExactly(vacationDay);
    }
}

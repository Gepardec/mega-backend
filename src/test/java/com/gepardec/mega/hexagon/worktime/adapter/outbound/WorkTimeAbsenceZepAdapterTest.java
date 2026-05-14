package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.domain.model.Absence;
import com.gepardec.mega.hexagon.worktime.domain.model.AbsenceType;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepAbsenceReason;
import com.gepardec.mega.zep.rest.service.AbsenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkTimeAbsenceZepAdapterTest {

    @Mock
    private AbsenceService absenceService;

    private WorkTimeAbsenceZepAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WorkTimeAbsenceZepAdapter(absenceService);
    }

    @Test
    void fetchAbsencesForEmployee_shouldExpandRangesAndMapKnownCodes() {
        YearMonth month = YearMonth.of(2026, 3);
        ZepUsername zepUsername = ZepUsername.of("ada");
        when(absenceService.getZepAbsencesByEmployeeNameForDateRange("ada", month)).thenReturn(List.of(
                zepAbsence(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 3), "UB")
        ));

        List<Absence> absences = adapter.fetchAbsencesForEmployee(zepUsername, month);

        assertThat(absences).containsExactly(
                new Absence(LocalDate.of(2026, 3, 2), AbsenceType.VACATION),
                new Absence(LocalDate.of(2026, 3, 3), AbsenceType.VACATION)
        );
    }

    @Test
    void fetchAbsencesForEmployee_shouldSkipAbsencesWithUnknownCode() {
        YearMonth month = YearMonth.of(2026, 3);
        ZepUsername zepUsername = ZepUsername.of("ada");
        when(absenceService.getZepAbsencesByEmployeeNameForDateRange("ada", month)).thenReturn(List.of(
                zepAbsence(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 3), "UB"),
                zepAbsence(LocalDate.of(2026, 3, 4), LocalDate.of(2026, 3, 4), "???")
        ));

        List<Absence> absences = adapter.fetchAbsencesForEmployee(zepUsername, month);

        assertThat(absences).containsExactly(
                new Absence(LocalDate.of(2026, 3, 2), AbsenceType.VACATION),
                new Absence(LocalDate.of(2026, 3, 3), AbsenceType.VACATION)
        );
    }

    private ZepAbsence zepAbsence(LocalDate startDate, LocalDate endDate, String reason) {
        return ZepAbsence.builder()
                .startDate(startDate)
                .endDate(endDate)
                .absenceReason(ZepAbsenceReason.builder().name(reason).build())
                .build();
    }
}

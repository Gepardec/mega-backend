package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeAbsenceZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeUserNotFoundException;
import com.gepardec.mega.hexagon.worktime.domain.model.Absence;
import com.gepardec.mega.hexagon.worktime.domain.model.AbsenceType;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEmployeeAbsencesServiceTest {

    @Mock
    private WorkTimeUserSnapshotPort workTimeUserSnapshotPort;

    @Mock
    private WorkTimeAbsenceZepPort workTimeAbsenceZepPort;

    private GetEmployeeAbsencesService service;

    @BeforeEach
    void setUp() {
        service = new GetEmployeeAbsencesService(workTimeUserSnapshotPort, workTimeAbsenceZepPort);
    }

    @Test
    void getAbsences_shouldReturnAbsencesForKnownEmployee() {
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        YearMonth month = YearMonth.of(2026, 3);
        ZepUsername zepUsername = ZepUsername.of("ada");
        List<Absence> absences = List.of(new Absence(LocalDate.of(2026, 3, 2), AbsenceType.VACATION));
        when(workTimeUserSnapshotPort.findById(employeeId, month))
                .thenReturn(Optional.of(new UserRef(employeeId, FullName.of("Ada", "Lovelace"), zepUsername)));
        when(workTimeAbsenceZepPort.fetchAbsencesForEmployee(zepUsername, month)).thenReturn(absences);

        List<Absence> result = service.getAbsences(employeeId, month);

        assertThat(result).isEqualTo(absences);
    }

    @Test
    void getAbsences_shouldThrowWhenEmployeeIsUnknown() {
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        YearMonth month = YearMonth.of(2026, 3);
        when(workTimeUserSnapshotPort.findById(employeeId, month)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAbsences(employeeId, month))
                .isInstanceOf(WorkTimeUserNotFoundException.class)
                .hasMessage("user not found: " + employeeId.value());
    }
}

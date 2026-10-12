package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.service.AttendanceService;
import com.gepardec.mega.zep.rest.service.ProjectService;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkTimeZepAdapterTest {

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private WorkTimeZepAdapter adapter;

    @Test
    void fetchAttendancesForEmployee_shouldSplitBillableAndNonBillableHours() {
        when(attendanceService.getAttendanceForUserAndMonthAsync("ada", YearMonth.of(2026, 3))).thenReturn(Uni.createFrom().item(List.of(
                ZepAttendance.builder()
                        .employeeId("ada")
                        .projectId(11)
                        .duration(2.5d)
                        .billable(true)
                        .build(),
                ZepAttendance.builder()
                        .employeeId("ada")
                        .projectId(11)
                        .duration(1.5d)
                        .billable(false)
                        .build()
        )));

        List<WorkTimeAttendance> attendances = adapter.fetchAttendancesForEmployee("ada", YearMonth.of(2026, 3))
                .await().indefinitely();

        assertThat(attendances).containsExactly(
                new WorkTimeAttendance("ada", 11, 2.5d, 0.0d),
                new WorkTimeAttendance("ada", 11, 0.0d, 1.5d)
        );
    }

    @Test
    void fetchProjectMembershipForMonth_shouldReturnDistinctEmployeeIds() {
        when(projectService.getProjectEmployeesForIdAsync(11, YearMonth.of(2026, 3))).thenReturn(Uni.createFrom().item(List.of(
                ZepProjectEmployee.builder().username("ada").build(),
                ZepProjectEmployee.builder().username("grace").build(),
                ZepProjectEmployee.builder().username("ada").build()
        )));

        List<String> employeeIds = adapter.fetchProjectMembershipForMonth(11, YearMonth.of(2026, 3))
                .await().indefinitely();

        assertThat(employeeIds).containsExactly("ada", "grace");
    }
}

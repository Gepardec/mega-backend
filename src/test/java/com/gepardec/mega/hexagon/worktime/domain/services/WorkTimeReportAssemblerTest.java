package com.gepardec.mega.hexagon.worktime.domain.services;

import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEntry;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorkTimeReportAssemblerTest {

    private final WorkTimeReportAssembler assembler = new WorkTimeReportAssembler();

    @Test
    void totalHours_shouldSumHoursAcrossAttendances() {
        double totalHours = assembler.totalHours(List.of(
                new WorkTimeAttendance("ada", 11, 2.5d, 0.0d),
                new WorkTimeAttendance("ada", 11, 0.0d, 1.5d),
                new WorkTimeAttendance("ada", 22, 3.0d, 0.5d)
        ));

        assertThat(totalHours).isEqualTo(7.5d);
    }

    @Test
    void totalHours_shouldReturnZeroForEmptyAttendanceList() {
        double totalHours = assembler.totalHours(List.of());

        assertThat(totalHours).isZero();
    }

    @Test
    void buildEntry_shouldAggregateBillableAndNonBillableHours() {
        WorkTimeEntry entry = assembler.buildEntry(
                employee(),
                project(),
                List.of(
                        new WorkTimeAttendance("ada", 11, 2.5d, 0.0d),
                        new WorkTimeAttendance("ada", 11, 0.0d, 1.5d),
                        new WorkTimeAttendance("ada", 11, 3.0d, 0.5d)
                ),
                7.5d
        );

        assertThat(entry.billableHours()).isEqualTo(5.5d);
        assertThat(entry.nonBillableHours()).isEqualTo(2.0d);
        assertThat(entry.employeeMonthTotalHours()).isEqualTo(7.5d);
    }

    @Test
    void buildEntry_shouldCarryEmployeeAndProjectReferences() {
        UserRef employee = employee();
        ProjectRef project = project();

        WorkTimeEntry entry = assembler.buildEntry(
                employee,
                project,
                List.of(new WorkTimeAttendance("ada", 11, 2.5d, 0.0d)),
                2.5d
        );

        assertThat(entry.employee()).isEqualTo(employee);
        assertThat(entry.project()).isEqualTo(project);
    }

    private UserRef employee() {
        return new UserRef(
                UserId.of(Instancio.create(UUID.class)),
                FullName.of("Ada", "Lovelace"),
                ZepUsername.of("ada")
        );
    }

    private ProjectRef project() {
        return new ProjectRef(ProjectId.of(Instancio.create(UUID.class)), 11, "Alpha");
    }
}

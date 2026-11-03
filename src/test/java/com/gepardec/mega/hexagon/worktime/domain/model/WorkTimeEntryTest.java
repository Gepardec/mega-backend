package com.gepardec.mega.hexagon.worktime.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkTimeEntryTest {

    @Test
    void of_shouldSumBillableAndNonBillableHoursAndKeepReferences() {
        UserRef employee = Instancio.create(UserRef.class);
        ProjectRef project = Instancio.create(ProjectRef.class);

        WorkTimeEntry entry = WorkTimeEntry.of(
                employee,
                project,
                List.of(
                        new WorkTimeAttendance("ada", 11, 2.5d, 0.0d),
                        new WorkTimeAttendance("ada", 11, 0.0d, 1.5d),
                        new WorkTimeAttendance("ada", 11, 3.0d, 0.5d)
                ),
                9.0d
        );

        assertThat(entry).isEqualTo(new WorkTimeEntry(employee, project, 5.5d, 2.0d, 9.0d));
    }

    @Test
    void of_shouldHaveZeroHours_whenNoAttendancesAreGiven() {
        WorkTimeEntry entry = WorkTimeEntry.of(
                Instancio.create(UserRef.class),
                Instancio.create(ProjectRef.class),
                List.of(),
                0.0d
        );

        assertThat(entry.billableHours()).isZero();
        assertThat(entry.nonBillableHours()).isZero();
    }
}

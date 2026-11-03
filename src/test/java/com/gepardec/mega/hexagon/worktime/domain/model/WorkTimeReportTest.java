package com.gepardec.mega.hexagon.worktime.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class WorkTimeReportTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 3);

    @Nested
    class Assemble {

        @Test
        void assemble_shouldCreateOneEntryPerProjectWithSummedHours() {
            UserRef ada = Instancio.create(UserRef.class);
            ProjectRef alpha = project(11);
            ProjectRef beta = project(22);

            WorkTimeReport report = WorkTimeReport.assemble(
                    MONTH,
                    Map.of(ada, List.of(
                            new WorkTimeAttendance("ada", 11, 2.5d, 0.0d),
                            new WorkTimeAttendance("ada", 11, 0.0d, 1.5d),
                            new WorkTimeAttendance("ada", 22, 3.0d, 0.5d)
                    )),
                    Map.of(11, alpha, 22, beta)
            );

            assertThat(report.payrollMonth()).isEqualTo(MONTH);
            assertThat(report.entries()).containsExactlyInAnyOrder(
                    new WorkTimeEntry(ada, alpha, 2.5d, 1.5d, 7.5d),
                    new WorkTimeEntry(ada, beta, 3.0d, 0.5d, 7.5d)
            );
        }

        @Test
        void assemble_shouldCountUnresolvedProjectsTowardsMonthTotal_whenNoEntryIsCreatedForThem() {
            UserRef ada = Instancio.create(UserRef.class);
            ProjectRef alpha = project(11);

            WorkTimeReport report = WorkTimeReport.assemble(
                    MONTH,
                    Map.of(ada, List.of(
                            new WorkTimeAttendance("ada", 11, 2.0d, 0.0d),
                            new WorkTimeAttendance("ada", 99, 4.0d, 1.0d)
                    )),
                    Map.of(11, alpha)
            );

            assertThat(report.entries()).containsExactly(new WorkTimeEntry(ada, alpha, 2.0d, 0.0d, 7.0d));
        }

        @Test
        void assemble_shouldComputeMonthTotalPerEmployee() {
            UserRef ada = Instancio.create(UserRef.class);
            UserRef grace = Instancio.create(UserRef.class);
            ProjectRef alpha = project(11);

            WorkTimeReport report = WorkTimeReport.assemble(
                    MONTH,
                    Map.of(
                            ada, List.of(new WorkTimeAttendance("ada", 11, 5.0d, 0.0d)),
                            grace, List.of(new WorkTimeAttendance("grace", 11, 0.0d, 3.5d))
                    ),
                    Map.of(11, alpha)
            );

            assertThat(report.entries()).containsExactlyInAnyOrder(
                    new WorkTimeEntry(ada, alpha, 5.0d, 0.0d, 5.0d),
                    new WorkTimeEntry(grace, alpha, 0.0d, 3.5d, 3.5d)
            );
        }

        @Test
        void assemble_shouldReturnNoEntries_whenNoAttendancesExist() {
            WorkTimeReport report = WorkTimeReport.assemble(MONTH, Map.of(), Map.of());

            assertThat(report).isEqualTo(WorkTimeReport.empty(MONTH));
        }
    }

    @Test
    void empty_shouldHaveMonthAndNoEntries() {
        WorkTimeReport report = WorkTimeReport.empty(MONTH);

        assertThat(report.payrollMonth()).isEqualTo(MONTH);
        assertThat(report.entries()).isEmpty();
    }

    private ProjectRef project(int zepId) {
        return Instancio.of(ProjectRef.class)
                .set(field(ProjectRef::zepId), zepId)
                .create();
    }
}

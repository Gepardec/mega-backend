package com.gepardec.mega.hexagon.worktime.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
        void assemble_shouldOrderEntriesByEmployeeNameThenProjectName() {
            UserRef ada = user("Ada", "Lovelace", 1);
            UserRef grace = user("Grace", "Hopper", 2);
            ProjectRef alpha = project("Alpha", 11, 1);
            ProjectRef beta = project("Beta", 22, 2);

            WorkTimeReport report = WorkTimeReport.assemble(
                    MONTH,
                    Map.of(
                            grace, List.of(attendance(22), attendance(11)),
                            ada, List.of(attendance(22), attendance(11))
                    ),
                    Map.of(11, alpha, 22, beta)
            );

            assertThat(report.entries()).extracting(WorkTimeEntry::employee, WorkTimeEntry::project)
                    .containsExactly(
                            tuple(ada, alpha),
                            tuple(ada, beta),
                            tuple(grace, alpha),
                            tuple(grace, beta)
                    );
        }

        @Test
        void assemble_shouldFallBackToIds_whenNamesAreEqual() {
            UserRef first = user("Ada", "Lovelace", 1);
            UserRef second = user("Ada", "Lovelace", 2);
            ProjectRef projectOne = project("Alpha", 11, 1);
            ProjectRef projectTwo = project("Alpha", 22, 2);

            WorkTimeReport report = WorkTimeReport.assemble(
                    MONTH,
                    Map.of(
                            second, List.of(attendance(22), attendance(11)),
                            first, List.of(attendance(22), attendance(11))
                    ),
                    Map.of(11, projectOne, 22, projectTwo)
            );

            assertThat(report.entries()).extracting(WorkTimeEntry::employee, WorkTimeEntry::project)
                    .containsExactly(
                            tuple(first, projectOne),
                            tuple(first, projectTwo),
                            tuple(second, projectOne),
                            tuple(second, projectTwo)
                    );
        }

        @Test
        void assemble_shouldProduceSameOrder_whenInputIterationOrderDiffers() {
            UserRef ada = user("Ada", "Lovelace", 1);
            UserRef grace = user("Grace", "Hopper", 2);
            ProjectRef alpha = project("Alpha", 11, 1);

            Map<UserRef, List<WorkTimeAttendance>> adaFirst = new LinkedHashMap<>();
            adaFirst.put(ada, List.of(attendance(11)));
            adaFirst.put(grace, List.of(attendance(11)));
            Map<UserRef, List<WorkTimeAttendance>> graceFirst = new LinkedHashMap<>();
            graceFirst.put(grace, List.of(attendance(11)));
            graceFirst.put(ada, List.of(attendance(11)));

            assertThat(WorkTimeReport.assemble(MONTH, adaFirst, Map.of(11, alpha)))
                    .isEqualTo(WorkTimeReport.assemble(MONTH, graceFirst, Map.of(11, alpha)));
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

    private static WorkTimeAttendance attendance(int projectZepId) {
        return new WorkTimeAttendance("user", projectZepId, 1.0d, 0.0d);
    }

    private static UserRef user(String firstname, String lastname, int idSeed) {
        return Instancio.of(UserRef.class)
                .set(field(UserRef::id), UserId.of(new UUID(0, idSeed)))
                .set(field(UserRef::fullName), FullName.of(firstname, lastname))
                .create();
    }

    private static ProjectRef project(String name, int zepId, int idSeed) {
        return new ProjectRef(ProjectId.of(new UUID(0, idSeed)), zepId, name);
    }

    private ProjectRef project(int zepId) {
        return Instancio.of(ProjectRef.class)
                .set(field(ProjectRef::zepId), zepId)
                .create();
    }
}

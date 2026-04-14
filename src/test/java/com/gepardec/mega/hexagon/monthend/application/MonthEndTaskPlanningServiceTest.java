package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.services.MonthEndTaskPlanningService;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MonthEndTaskPlanningServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);

    private MonthEndTaskPlanningService service;

    @BeforeEach
    void setUp() {
        service = new MonthEndTaskPlanningService();
    }

    @Test
    void planEmployeeOwnedTasks_shouldCreateOnlyTimeCheck_whenProjectIsNonBillable() {
        MonthEndUserSnapshot employee = activeUser("employee");
        MonthEndProjectSnapshot project = activeProject(false, Set.of());

        List<MonthEndTask> tasks = service.planEmployeeOwnedTasks(month, project, employee);

        assertThat(tasks).singleElement()
                .satisfies(task -> {
                    assertThat(task.type()).isEqualTo(MonthEndTaskType.EMPLOYEE_TIME_CHECK);
                    assertThat(task.subjectEmployeeId()).isEqualTo(employee.id());
                    assertThat(task.eligibleActorIds()).containsExactly(employee.id());
                });
    }

    @Test
    void planEmployeeOwnedTasks_shouldCreateTimeCheckAndLeistungsnachweis_whenProjectIsBillable() {
        MonthEndUserSnapshot employee = activeUser("employee");
        MonthEndProjectSnapshot project = activeProject(true, Set.of());

        List<MonthEndTask> tasks = service.planEmployeeOwnedTasks(month, project, employee);

        assertThat(tasks).extracting(MonthEndTask::type)
                .containsExactly(
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        MonthEndTaskType.LEISTUNGSNACHWEIS
                );
        assertThat(tasks).allSatisfy(task -> {
            assertThat(task.subjectEmployeeId()).isEqualTo(employee.id());
            assertThat(task.eligibleActorIds()).containsExactly(employee.id());
        });
    }

    @Test
    void planProjectTasks_shouldIncludeLeadReviewAndAbrechnung_whenProjectIsBillableAndLeadsExist() {
        MonthEndUserSnapshot employeeA = activeUser("employee-a");
        MonthEndUserSnapshot employeeB = activeUser("employee-b");
        UserId leadA = UserId.of(Instancio.create(UUID.class));
        UserId leadB = UserId.of(Instancio.create(UUID.class));
        MonthEndProjectSnapshot project = activeProject(true, Set.of(leadA, leadB));

        List<MonthEndTask> tasks = service.planProjectTasks(
                month,
                project,
                Set.of(leadA, leadB),
                new LinkedHashSet<>(Set.of(employeeA, employeeB))
        );

        assertThat(tasks).hasSize(7);
        assertThat(tasks.stream()
                .filter(task -> task.type() == MonthEndTaskType.PROJECT_LEAD_REVIEW)
                .map(MonthEndTask::subjectEmployeeId)
                .toList())
                .containsExactlyInAnyOrder(employeeA.id(), employeeB.id());
        assertThat(tasks.stream()
                .filter(task -> task.type() == MonthEndTaskType.ABRECHNUNG))
                .singleElement()
                .satisfies(task -> {
                    assertThat(task.subjectEmployeeId()).isNull();
                    assertThat(task.eligibleActorIds()).containsExactlyInAnyOrder(leadA, leadB);
                });
    }

    @Test
    void planProjectTasks_shouldSkipLeadOwnedTasks_whenNoActiveLeadsExist() {
        MonthEndUserSnapshot employee = activeUser("employee");
        MonthEndProjectSnapshot project = activeProject(true, Set.of());

        List<MonthEndTask> tasks = service.planProjectTasks(month, project, Set.of(), Set.of(employee));

        assertThat(tasks).extracting(MonthEndTask::type)
                .containsExactly(
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        MonthEndTaskType.LEISTUNGSNACHWEIS
                );
    }

    private MonthEndUserSnapshot activeUser(String username) {
        return new MonthEndUserSnapshot(
                UserId.of(Instancio.create(UUID.class)),
                username + " User",
                username,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null))
        );
    }

    private MonthEndProjectSnapshot activeProject(boolean billable, Set<UserId> leadIds) {
        return new MonthEndProjectSnapshot(
                ProjectId.of(Instancio.create(UUID.class)),
                91,
                "Project-91",
                LocalDate.of(2025, 1, 1),
                null,
                billable,
                leadIds
        );
    }
}

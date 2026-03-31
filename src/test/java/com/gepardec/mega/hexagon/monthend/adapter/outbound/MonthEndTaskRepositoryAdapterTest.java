package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.project.adapter.outbound.ProjectRepositoryAdapter;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.user.adapter.outbound.UserRepositoryAdapter;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.RegularWorkingTimes;
import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepProfile;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class MonthEndTaskRepositoryAdapterTest {

    @Inject
    MonthEndTaskRepositoryAdapter monthEndTaskRepositoryAdapter;

    @Inject
    UserRepositoryAdapter userRepositoryAdapter;

    @Inject
    ProjectRepositoryAdapter projectRepositoryAdapter;

    @Test
    void findOpenEmployeeTasks_shouldReturnOnlyOpenIndividualActorTasks() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("employee", Set.of(Role.EMPLOYEE));
        User lead = user("lead", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, lead));

        Project project = project(42, true);
        projectRepositoryAdapter.saveAll(List.of(project));

        MonthEndTask openEmployeeTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.getId(),
                employee.getId(),
                Set.of(employee.getId())
        );
        MonthEndTask doneEmployeeTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                project.getId(),
                employee.getId(),
                Set.of(employee.getId())
        ).complete(employee.getId());
        MonthEndTask openLeadTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                project.getId(),
                employee.getId(),
                Set.of(lead.getId())
        );
        monthEndTaskRepositoryAdapter.saveAll(List.of(openEmployeeTask, doneEmployeeTask, openLeadTask));

        List<MonthEndTask> tasks = monthEndTaskRepositoryAdapter.findOpenEmployeeTasks(employee.getId(), month);

        assertThat(tasks).containsExactly(openEmployeeTask);
    }

    @Test
    void findOpenProjectLeadTasks_shouldHideCompletedSharedTaskForAllEligibleLeads() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("subject", Set.of(Role.EMPLOYEE));
        User leadA = user("leadA", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("leadB", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, leadA, leadB));

        Project project = project(99, true);
        projectRepositoryAdapter.saveAll(List.of(project));

        MonthEndTask sharedTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                project.getId(),
                employee.getId(),
                Set.of(leadA.getId(), leadB.getId())
        );
        monthEndTaskRepositoryAdapter.save(sharedTask);

        assertThat(monthEndTaskRepositoryAdapter.findOpenProjectLeadTasks(leadA.getId(), month))
                .containsExactly(sharedTask);
        assertThat(monthEndTaskRepositoryAdapter.findOpenProjectLeadTasks(leadB.getId(), month))
                .containsExactly(sharedTask);

        MonthEndTask completedTask = sharedTask.complete(leadA.getId());
        monthEndTaskRepositoryAdapter.save(completedTask);

        assertThat(monthEndTaskRepositoryAdapter.findOpenProjectLeadTasks(leadA.getId(), month)).isEmpty();
        assertThat(monthEndTaskRepositoryAdapter.findOpenProjectLeadTasks(leadB.getId(), month)).isEmpty();
        assertThat(monthEndTaskRepositoryAdapter.findById(sharedTask.id()))
                .contains(completedTask);
    }

    private User user(String username, Set<Role> roles) {
        return User.create(UserId.generate(), profile(username), roles);
    }

    private ZepProfile profile(String username) {
        return new ZepProfile(
                username,
                username + "@example.com",
                "Test",
                "User",
                null,
                null,
                null,
                null,
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null)),
                RegularWorkingTimes.empty()
        );
    }

    private Project project(int zepId, boolean billable) {
        return Project.create(
                ProjectId.generate(),
                new ZepProjectProfile(zepId, "Project-" + zepId, LocalDate.of(2025, 1, 1), null, billable)
        );
    }
}

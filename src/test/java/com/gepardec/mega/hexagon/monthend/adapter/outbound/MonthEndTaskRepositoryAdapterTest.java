package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.project.adapter.outbound.ProjectRepositoryAdapter;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.adapter.outbound.UserRepositoryAdapter;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.User;
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
                project.id(),
                employee.id(),
                Set.of(employee.id())
        );
        MonthEndTask doneEmployeeTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                project.id(),
                employee.id(),
                Set.of(employee.id())
        ).complete(employee.id());
        MonthEndTask openLeadTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                project.id(),
                employee.id(),
                Set.of(lead.id())
        );
        monthEndTaskRepositoryAdapter.saveAll(List.of(openEmployeeTask, doneEmployeeTask, openLeadTask));

        List<MonthEndTask> tasks = monthEndTaskRepositoryAdapter.findOpenEmployeeTasks(employee.id(), month);

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
                project.id(),
                employee.id(),
                Set.of(leadA.id(), leadB.id())
        );
        monthEndTaskRepositoryAdapter.save(sharedTask);

        assertThat(monthEndTaskRepositoryAdapter.findOpenProjectLeadTasks(leadA.id(), month))
                .containsExactly(sharedTask);
        assertThat(monthEndTaskRepositoryAdapter.findOpenProjectLeadTasks(leadB.id(), month))
                .containsExactly(sharedTask);

        MonthEndTask completedTask = sharedTask.complete(leadA.id());
        monthEndTaskRepositoryAdapter.save(completedTask);

        assertThat(monthEndTaskRepositoryAdapter.findOpenProjectLeadTasks(leadA.id(), month)).isEmpty();
        assertThat(monthEndTaskRepositoryAdapter.findOpenProjectLeadTasks(leadB.id(), month)).isEmpty();
        assertThat(monthEndTaskRepositoryAdapter.findById(sharedTask.id()))
                .contains(completedTask);
    }

    @Test
    void findEmployeeVisibleTasks_shouldReturnOnlyTasksWhereEmployeeIsSubject() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("employee-visible", Set.of(Role.EMPLOYEE));
        User leadA = user("lead-visible-a", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("lead-visible-b", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, leadA, leadB));

        Project project = project(123, true);
        projectRepositoryAdapter.saveAll(List.of(project));

        MonthEndTask subjectOnlyTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                project.id(),
                employee.id(),
                Set.of(leadA.id(), leadB.id())
        );
        MonthEndTask subjectAndEligibleTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.id(),
                employee.id(),
                Set.of(employee.id())
        );
        MonthEndTask abrechnungTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.ABRECHNUNG,
                project.id(),
                null,
                Set.of(leadA.id())
        );
        monthEndTaskRepositoryAdapter.saveAll(List.of(subjectOnlyTask, subjectAndEligibleTask, abrechnungTask));

        List<MonthEndTask> tasks = monthEndTaskRepositoryAdapter.findEmployeeVisibleTasks(employee.id(), month);

        assertThat(tasks).containsExactlyInAnyOrder(subjectOnlyTask, subjectAndEligibleTask);
    }

    @Test
    void findLeadProjectTasks_shouldNotReturnTasksFromProjectsWhereLeadIsOnlyAnEmployee() {
        YearMonth month = YearMonth.of(2026, 3);
        User lead = user("lead-emp-only", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User otherLead = user("other-lead-emp", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(lead, otherLead));

        Project ledProject = project(301, true);
        Project employeeOnlyProject = project(302, true);
        projectRepositoryAdapter.saveAll(List.of(ledProject, employeeOnlyProject));

        // led project: lead is in eligibleActorIds of PLR/ABRECHNUNG
        MonthEndTask ledPlr = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                ledProject.id(), lead.id(), Set.of(lead.id())
        );
        // employee-only project: lead has their own ETC task (lead in eligibleActorIds as employee)
        // but NO PLR/ABRECHNUNG with lead as eligible
        MonthEndTask ownEtc = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                employeeOnlyProject.id(), lead.id(), Set.of(lead.id())
        );
        MonthEndTask otherLeadPlr = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                employeeOnlyProject.id(), lead.id(), Set.of(otherLead.id())
        );
        monthEndTaskRepositoryAdapter.saveAll(List.of(ledPlr, ownEtc, otherLeadPlr));

        List<MonthEndTask> result = monthEndTaskRepositoryAdapter.findLeadProjectTasks(lead.id(), month);

        assertThat(result).containsExactly(ledPlr);
    }

    @Test
    void findLeadProjectTasks_shouldReturnAllTasksForProjectsTheLeadLeads() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("emp-lead-proj", Set.of(Role.EMPLOYEE));
        User lead = user("lead-proj", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User otherLead = user("other-lead-proj", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, lead, otherLead));

        Project ledProject = project(201, true);
        Project unledProject = project(202, true);
        projectRepositoryAdapter.saveAll(List.of(ledProject, unledProject));

        MonthEndTask etcTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                ledProject.id(), employee.id(), Set.of(employee.id())
        );
        MonthEndTask plrTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                ledProject.id(), employee.id(), Set.of(lead.id())
        );
        MonthEndTask abrechnungTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.ABRECHNUNG,
                ledProject.id(), null, Set.of(lead.id())
        );
        MonthEndTask unledTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                unledProject.id(), employee.id(), Set.of(otherLead.id())
        );
        monthEndTaskRepositoryAdapter.saveAll(List.of(etcTask, plrTask, abrechnungTask, unledTask));

        List<MonthEndTask> result = monthEndTaskRepositoryAdapter.findLeadProjectTasks(lead.id(), month);

        assertThat(result).containsExactlyInAnyOrder(etcTask, plrTask, abrechnungTask);
    }

    private User user(String username, Set<Role> roles) {
        return new User(
                UserId.generate(),
                Email.of(username + "@example.com"),
                FullName.of("Test", "User"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null)),
                roles
        );
    }

    private Project project(int zepId, boolean billable) {
        return Project.create(
                ProjectId.generate(),
                new ZepProjectProfile(zepId, "Project-" + zepId, LocalDate.of(2025, 1, 1), null, billable)
        );
    }
}

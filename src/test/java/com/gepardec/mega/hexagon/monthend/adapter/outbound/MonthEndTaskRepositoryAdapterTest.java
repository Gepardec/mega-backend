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
import static org.mockito.ArgumentMatchers.any;

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
    void findOpenSubjectTasks_shouldReturnAllOpenTasksWhereActorIsSubjectAndExcludeCompletedOnes() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("employee", Set.of(Role.EMPLOYEE));
        User lead = user("lead", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User otherEmployee = user("other-employee", Set.of(Role.EMPLOYEE));
        userRepositoryAdapter.saveAll(List.of(employee, lead, otherEmployee));

        Project project = project(42, true);
        projectRepositoryAdapter.saveAll(List.of(project));

        MonthEndTask openEtcTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.id(), employee.id(), Set.of(employee.id())
        );
        MonthEndTask doneEtcTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                project.id(), employee.id(), Set.of(lead.id())
        ).complete(lead.id());
        MonthEndTask openReviewTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                project.id(), employee.id(), Set.of(lead.id())
        );
        MonthEndTask doneReviewTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                project.id(), employee.id(), Set.of(lead.id())
        ).complete(lead.id());
        MonthEndTask otherEmployeeTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.id(), otherEmployee.id(), Set.of(otherEmployee.id())
        );
        monthEndTaskRepositoryAdapter.saveAll(List.of(openEtcTask, doneEtcTask, openReviewTask, doneReviewTask, otherEmployeeTask));

        List<MonthEndTask> tasks = monthEndTaskRepositoryAdapter.findOpenSubjectTasks(employee.id(), month);

        assertThat(tasks).containsExactlyInAnyOrder(openEtcTask, openReviewTask);
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
    void existsForSubjectEmployee_shouldReturnTrueOnlyForMatchingMonthProjectAndSubject() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("employee-exists", Set.of(Role.EMPLOYEE));
        User otherEmployee = user("other-employee-exists", Set.of(Role.EMPLOYEE));
        userRepositoryAdapter.saveAll(List.of(employee, otherEmployee));

        Project project = project(133, true);
        Project otherProject = project(134, true);
        projectRepositoryAdapter.saveAll(List.of(project, otherProject));

        MonthEndTask matchingTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.id(),
                employee.id(),
                Set.of(employee.id())
        );
        MonthEndTask otherProjectTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                otherProject.id(),
                employee.id(),
                Set.of(employee.id())
        );
        MonthEndTask otherSubjectTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.id(),
                otherEmployee.id(),
                Set.of(otherEmployee.id())
        );
        monthEndTaskRepositoryAdapter.saveAll(List.of(matchingTask, otherProjectTask, otherSubjectTask));

        assertThat(monthEndTaskRepositoryAdapter.existsForSubjectEmployee(month, project.id(), employee.id())).isTrue();
        assertThat(monthEndTaskRepositoryAdapter.existsForSubjectEmployee(month.plusMonths(1), project.id(), employee.id())).isFalse();
        assertThat(monthEndTaskRepositoryAdapter.existsForSubjectEmployee(month, otherProject.id(), otherEmployee.id())).isFalse();
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

    @Test
    void findOpenLeistungsnachweisTasks_shouldReturnOnlyOpenLeistungsnachweisTasksForProjectAndMonth() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("employee", Set.of(Role.EMPLOYEE));
        User lead = user("lead", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, lead));

        Project project = project(42, true);
        Project otherProject = project(43, true);
        projectRepositoryAdapter.saveAll(List.of(project, otherProject));

        MonthEndTask openLnTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                project.id(), employee.id(), Set.of(lead.id())
        );
        MonthEndTask doneLnTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                project.id(), employee.id(), Set.of(lead.id())
        ).complete(lead.id());
        MonthEndTask otherProjectLnTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                otherProject.id(), employee.id(), Set.of(lead.id())
        );
        MonthEndTask otherMonthLnTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month.plusMonths(1),
                MonthEndTaskType.LEISTUNGSNACHWEIS,
                project.id(), employee.id(), Set.of(lead.id())
        );
        MonthEndTask etcTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.id(), employee.id(), Set.of(employee.id())
        );
        monthEndTaskRepositoryAdapter.saveAll(
                List.of(openLnTask, doneLnTask, otherProjectLnTask, otherMonthLnTask, etcTask));

        List<MonthEndTask> result = monthEndTaskRepositoryAdapter.findOpenLeistungsnachweisTasks(month, project.id());

        assertThat(result).containsExactly(openLnTask);
    }

    @Test
    void findOpenEmployeeTimeCheckTasks_shouldReturnOnlyOpenEmployeeTimeCheckTasksForEmployeeMonthAndProject() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("employee-etc", Set.of(Role.EMPLOYEE));
        User otherEmployee = user("other-etc", Set.of(Role.EMPLOYEE));
        userRepositoryAdapter.saveAll(List.of(employee, otherEmployee));

        Project project = project(401, true);
        Project otherProject = project(402, true);
        projectRepositoryAdapter.saveAll(List.of(project, otherProject));

        MonthEndTask openEtcTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.id(), employee.id(), Set.of(employee.id())
        );
        MonthEndTask doneEtcTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.id(), employee.id(), Set.of(employee.id())
        ).complete(employee.id());
        MonthEndTask otherProjectEtcTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                otherProject.id(), employee.id(), Set.of(employee.id())
        );
        MonthEndTask otherMonthEtcTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month.plusMonths(1),
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.id(), employee.id(), Set.of(employee.id())
        );
        MonthEndTask otherSubjectEtcTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.id(), otherEmployee.id(), Set.of(otherEmployee.id())
        );
        MonthEndTask reviewTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                project.id(), employee.id(), Set.of(employee.id())
        );
        monthEndTaskRepositoryAdapter.saveAll(
                List.of(openEtcTask, doneEtcTask, otherProjectEtcTask, otherMonthEtcTask, otherSubjectEtcTask, reviewTask));

        List<MonthEndTask> result = monthEndTaskRepositoryAdapter.findOpenEmployeeTimeCheckTasks(employee.id(), month, project.id());

        assertThat(result).containsExactly(openEtcTask);
    }

    @Test
    void findOpenEmployeeTimeCheckTasks_shouldReturnOpenEmployeeTimeCheckTasksAcrossAllProjects_whenProjectIdIsNull() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("employee-etc-all", Set.of(Role.EMPLOYEE));
        userRepositoryAdapter.saveAll(List.of(employee));

        Project projectA = project(411, true);
        Project projectB = project(412, true);
        projectRepositoryAdapter.saveAll(List.of(projectA, projectB));

        MonthEndTask taskInProjectA = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectA.id(), employee.id(), Set.of(employee.id())
        );
        MonthEndTask taskInProjectB = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectB.id(), employee.id(), Set.of(employee.id())
        );
        MonthEndTask doneTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectA.id(), employee.id(), Set.of(employee.id())
        ).complete(employee.id());
        monthEndTaskRepositoryAdapter.saveAll(List.of(taskInProjectA, taskInProjectB, doneTask));

        List<MonthEndTask> result = monthEndTaskRepositoryAdapter.findOpenEmployeeTimeCheckTasks(employee.id(), month, null);

        assertThat(result).containsExactlyInAnyOrder(taskInProjectA, taskInProjectB);
    }

    @Test
    void findByProjectMonthAndType_shouldReturnProjectForMonthWithCorrectType() {
        YearMonth monthA = YearMonth.of(2026, 3);
        YearMonth monthB = YearMonth.of(2026, 4);
        User employee = user("emp-lead-proj", Set.of(Role.EMPLOYEE));
        User lead = user("lead-proj", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User otherLead = user("other-lead-proj", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, lead, otherLead));

        Project projectA = project(201, true);
        Project projectB = project(202, true);
        projectRepositoryAdapter.saveAll(List.of(projectA,projectB));

        MonthEndTask matchTask = MonthEndTask.create(
                MonthEndTaskId.generate(), monthA, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectA.id(), employee.id(), Set.of(employee.id())
        );

        MonthEndTask diffTypeTask = MonthEndTask.create(
                MonthEndTaskId.generate(), monthA, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectA.id(), employee.id(), Set.of(employee.id())
        );

        MonthEndTask diffProjectTask = MonthEndTask.create(
                MonthEndTaskId.generate(), monthA, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectB.id(), employee.id(), Set.of(employee.id())
        );

        MonthEndTask diffMonthTask = MonthEndTask.create(
                MonthEndTaskId.generate(), monthB, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectA.id(), employee.id(), Set.of(employee.id())
        );

        monthEndTaskRepositoryAdapter.saveAll(List.of(matchTask, diffTypeTask, diffProjectTask, diffMonthTask));

        List<MonthEndTask> result1 = monthEndTaskRepositoryAdapter.findByProjectMonthAndType(monthA, projectA.id(), MonthEndTaskType.EMPLOYEE_TIME_CHECK);
        List<MonthEndTask> result2 = monthEndTaskRepositoryAdapter.findByProjectMonthAndType(monthA, projectA.id(), MonthEndTaskType.PROJECT_LEAD_REVIEW);
        List<MonthEndTask> result3 = monthEndTaskRepositoryAdapter.findByProjectMonthAndType(monthA, projectB.id(), MonthEndTaskType.EMPLOYEE_TIME_CHECK);
        List<MonthEndTask> result4 = monthEndTaskRepositoryAdapter.findByProjectMonthAndType(monthB, projectA.id(), MonthEndTaskType.EMPLOYEE_TIME_CHECK);

        assertThat(result1).hasSize(1).containsExactly(matchTask);
        assertThat(result2).hasSize(1).containsExactly(diffTypeTask);
        assertThat(result3).hasSize(1).containsExactly(diffProjectTask);
        assertThat(result4).hasSize(1).containsExactly(diffMonthTask);
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

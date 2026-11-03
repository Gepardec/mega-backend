package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndScope;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.project.adapter.outbound.ProjectRepositoryAdapter;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.shared.domain.model.Capacity;
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
    void findTasks_subjectEmployeeScope_shouldReturnOnlyTasksWhereEmployeeIsSubject() {
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

        List<MonthEndTask> tasks = monthEndTaskRepositoryAdapter.findTasks(month, new MonthEndScope.SubjectEmployee(employee.id()));

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
    void findTasks_ledProjectsScope_shouldNotReturnTasksFromProjectsWhereLeadIsOnlyAnEmployee() {
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

        List<MonthEndTask> result = monthEndTaskRepositoryAdapter.findTasks(month, new MonthEndScope.LedProjects(lead.id()));

        assertThat(result).containsExactly(ledPlr);
    }

    @Test
    void findTasks_ledProjectsScope_shouldReturnAllTasksForProjectsTheLeadLeads() {
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

        List<MonthEndTask> result = monthEndTaskRepositoryAdapter.findTasks(month, new MonthEndScope.LedProjects(lead.id()));

        assertThat(result).containsExactlyInAnyOrder(etcTask, plrTask, abrechnungTask);
    }

    @Test
    void findTasks_ledProjectsScope_shouldIncludeProjectsWhereActorIsOnlyEligibleOnLeistungsnachweis() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("emp-coordinator-proj", Set.of(Role.EMPLOYEE));
        User lead = user("lead-coordinator-proj", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User coordinator = user("coordinator-proj", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD, Role.PROJECT_COORDINATOR));
        userRepositoryAdapter.saveAll(List.of(employee, lead, coordinator));

        Project foreignProject = project(251, true);
        Project unrelatedProject = project(252, true);
        projectRepositoryAdapter.saveAll(List.of(foreignProject, unrelatedProject));

        MonthEndTask leistungsnachweis = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.LEISTUNGSNACHWEIS,
                foreignProject.id(), employee.id(), Set.of(coordinator.id())
        );
        MonthEndTask leadReview = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                foreignProject.id(), employee.id(), Set.of(lead.id())
        );
        MonthEndTask unrelatedReview = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                unrelatedProject.id(), employee.id(), Set.of(lead.id())
        );
        monthEndTaskRepositoryAdapter.saveAll(List.of(leistungsnachweis, leadReview, unrelatedReview));

        List<MonthEndTask> result = monthEndTaskRepositoryAdapter.findTasks(month, new MonthEndScope.LedProjects(coordinator.id()));

        assertThat(result).containsExactlyInAnyOrder(leistungsnachweis, leadReview);
        assertThat(result.stream()
                .filter(task -> task.isCompletableIn(Capacity.CONTROLLING, coordinator.id()))
                .toList())
                .containsExactly(leistungsnachweis);
    }

    @Test
    void existsTasks_subjectEmployeeScope_shouldReturnTrueOnlyWhenEmployeeIsSubjectInMonth() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("exists-subject-emp", Set.of(Role.EMPLOYEE));
        User otherEmployee = user("exists-subject-other", Set.of(Role.EMPLOYEE));
        userRepositoryAdapter.saveAll(List.of(employee, otherEmployee));
        Project project = project(401, true);
        projectRepositoryAdapter.saveAll(List.of(project));
        monthEndTaskRepositoryAdapter.saveAll(List.of(MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.id(), employee.id(), Set.of(employee.id())
        )));

        assertThat(monthEndTaskRepositoryAdapter.existsTasks(month, new MonthEndScope.SubjectEmployee(employee.id()))).isTrue();
        assertThat(monthEndTaskRepositoryAdapter.existsTasks(month, new MonthEndScope.SubjectEmployee(otherEmployee.id()))).isFalse();
        assertThat(monthEndTaskRepositoryAdapter.existsTasks(month.plusMonths(1), new MonthEndScope.SubjectEmployee(employee.id()))).isFalse();
    }

    @Test
    void existsTasks_ledProjectsScope_shouldReturnTrueWhenLeadLeadsAProjectWithTasks() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("exists-led-emp", Set.of(Role.EMPLOYEE));
        User lead = user("exists-led-lead", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, lead));
        Project ledProject = project(411, true);
        projectRepositoryAdapter.saveAll(List.of(ledProject));
        monthEndTaskRepositoryAdapter.saveAll(List.of(
                MonthEndTask.create(
                        MonthEndTaskId.generate(), month, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        ledProject.id(), employee.id(), Set.of(employee.id())
                ),
                MonthEndTask.create(
                        MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        ledProject.id(), employee.id(), Set.of(lead.id())
                )
        ));

        assertThat(monthEndTaskRepositoryAdapter.existsTasks(month, new MonthEndScope.LedProjects(lead.id()))).isTrue();
    }

    @Test
    void existsTasks_ledProjectsScope_shouldReturnFalse_whenNoTasksExist() {
        User lead = user("exists-led-none", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(lead));

        assertThat(monthEndTaskRepositoryAdapter.existsTasks(YearMonth.of(2026, 3), new MonthEndScope.LedProjects(lead.id()))).isFalse();
    }

    @Test
    void existsTasks_ledProjectsScope_shouldReturnFalse_whenTasksExistOnlyOnProjectsTheLeadDoesNotLead() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("exists-unled-emp", Set.of(Role.EMPLOYEE));
        User lead = user("exists-unled-lead", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User otherLead = user("exists-unled-other", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, lead, otherLead));
        Project unledProject = project(421, true);
        projectRepositoryAdapter.saveAll(List.of(unledProject));
        monthEndTaskRepositoryAdapter.saveAll(List.of(
                MonthEndTask.create(
                        MonthEndTaskId.generate(), month, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        unledProject.id(), lead.id(), Set.of(lead.id())
                ),
                MonthEndTask.create(
                        MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        unledProject.id(), employee.id(), Set.of(otherLead.id())
                )
        ));

        assertThat(monthEndTaskRepositoryAdapter.existsTasks(month, new MonthEndScope.LedProjects(lead.id()))).isFalse();
    }

    @Test
    void findOpenProjectTasksOfType_shouldReturnTasksOfMonthProjectAndType() {
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

        List<MonthEndTask> result1 = monthEndTaskRepositoryAdapter.findOpenProjectTasksOfType(monthA, projectA.id(), MonthEndTaskType.EMPLOYEE_TIME_CHECK);
        List<MonthEndTask> result2 = monthEndTaskRepositoryAdapter.findOpenProjectTasksOfType(monthA, projectA.id(), MonthEndTaskType.PROJECT_LEAD_REVIEW);
        List<MonthEndTask> result3 = monthEndTaskRepositoryAdapter.findOpenProjectTasksOfType(monthA, projectB.id(), MonthEndTaskType.EMPLOYEE_TIME_CHECK);
        List<MonthEndTask> result4 = monthEndTaskRepositoryAdapter.findOpenProjectTasksOfType(monthB, projectA.id(), MonthEndTaskType.EMPLOYEE_TIME_CHECK);

        assertThat(result1).containsExactly(matchTask);
        assertThat(result2).containsExactly(diffTypeTask);
        assertThat(result3).containsExactly(diffProjectTask);
        assertThat(result4).containsExactly(diffMonthTask);
    }

    @Test
    void findOpenProjectTasksOfType_shouldExcludeDoneTasks() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("emp-open-type", Set.of(Role.EMPLOYEE));
        User lead = user("lead-open-type", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, lead));

        Project project = project(501, true);
        projectRepositoryAdapter.saveAll(List.of(project));

        MonthEndTask openTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                project.id(), employee.id(), Set.of(lead.id())
        );
        MonthEndTask doneTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                project.id(), lead.id(), Set.of(lead.id())
        ).complete(lead.id());
        monthEndTaskRepositoryAdapter.saveAll(List.of(openTask, doneTask));

        List<MonthEndTask> result = monthEndTaskRepositoryAdapter.findOpenProjectTasksOfType(
                month, project.id(), MonthEndTaskType.PROJECT_LEAD_REVIEW);

        assertThat(result).containsExactly(openTask);
    }

    @Test
    void existsEligibleTaskOfType_shouldReturnTrue_whenActorIsEligibleOnlyOnDoneTaskOfType() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("emp-lead-done", Set.of(Role.EMPLOYEE));
        User lead = user("lead-done", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, lead));

        Project project = project(511, true);
        projectRepositoryAdapter.saveAll(List.of(project));

        MonthEndTask doneTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                project.id(), employee.id(), Set.of(lead.id())
        ).complete(lead.id());
        monthEndTaskRepositoryAdapter.saveAll(List.of(doneTask));

        assertThat(monthEndTaskRepositoryAdapter.existsEligibleTaskOfType(
                month, project.id(), MonthEndTaskType.PROJECT_LEAD_REVIEW, lead.id())).isTrue();
    }

    @Test
    void existsEligibleTaskOfType_shouldReturnFalse_whenActorIsEligibleOnlyOnAnotherType() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("emp-other-type", Set.of(Role.EMPLOYEE));
        User lead = user("lead-other-type", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, lead));

        Project project = project(521, true);
        projectRepositoryAdapter.saveAll(List.of(project));

        MonthEndTask reviewTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                project.id(), employee.id(), Set.of(lead.id())
        );
        monthEndTaskRepositoryAdapter.saveAll(List.of(reviewTask));

        assertThat(monthEndTaskRepositoryAdapter.existsEligibleTaskOfType(
                month, project.id(), MonthEndTaskType.LEISTUNGSNACHWEIS, lead.id())).isFalse();
    }

    @Test
    void existsEligibleTaskOfType_shouldReturnFalse_whenProjectHasNoTasksOfType() {
        YearMonth month = YearMonth.of(2026, 3);
        User lead = user("lead-no-type", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(lead));

        Project project = project(531, false);
        projectRepositoryAdapter.saveAll(List.of(project));

        assertThat(monthEndTaskRepositoryAdapter.existsEligibleTaskOfType(
                month, project.id(), MonthEndTaskType.LEISTUNGSNACHWEIS, lead.id())).isFalse();
    }

    @Test
    void existsEligibleTaskOfType_shouldReturnFalse_forOtherProjectOrMonth() {
        YearMonth month = YearMonth.of(2026, 3);
        User employee = user("emp-other-scope", Set.of(Role.EMPLOYEE));
        User lead = user("lead-other-scope", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(employee, lead));

        Project ledProject = project(541, true);
        Project otherProject = project(542, true);
        projectRepositoryAdapter.saveAll(List.of(ledProject, otherProject));

        MonthEndTask reviewTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                ledProject.id(), employee.id(), Set.of(lead.id())
        );
        monthEndTaskRepositoryAdapter.saveAll(List.of(reviewTask));

        assertThat(monthEndTaskRepositoryAdapter.existsEligibleTaskOfType(
                month, otherProject.id(), MonthEndTaskType.PROJECT_LEAD_REVIEW, lead.id())).isFalse();
        assertThat(monthEndTaskRepositoryAdapter.existsEligibleTaskOfType(
                month.plusMonths(1), ledProject.id(), MonthEndTaskType.PROJECT_LEAD_REVIEW, lead.id())).isFalse();
    }

    @Test
    void existsEligibleTaskOfType_shouldReturnFalse_forUnknownProject() {
        YearMonth month = YearMonth.of(2026, 3);
        User lead = user("lead-unknown", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        userRepositoryAdapter.saveAll(List.of(lead));

        assertThat(monthEndTaskRepositoryAdapter.existsEligibleTaskOfType(
                month, ProjectId.generate(), MonthEndTaskType.PROJECT_LEAD_REVIEW, lead.id())).isFalse();
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

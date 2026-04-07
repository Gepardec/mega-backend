package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.adapter.outbound.MonthEndTaskRepositoryAdapter;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistItem;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.CompleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.CompleteMonthEndTaskUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GenerateMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetEmployeeMonthEndWorklistUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetProjectLeadMonthEndWorklistUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.PrematureMonthEndPreparationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.UpdateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
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
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.service.ProjectService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestTransaction
@Tag("IT")
class MonthEndIT {

    private static final YearMonth MONTH = YearMonth.of(2026, 3);

    @Inject
    GenerateMonthEndTasksUseCase generateMonthEndTasksUseCase;

    @Inject
    PrematureMonthEndPreparationUseCase prematureMonthEndPreparationUseCase;

    @Inject
    CompleteMonthEndTaskUseCase completeMonthEndTaskUseCase;

    @Inject
    CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase;

    @Inject
    UpdateMonthEndClarificationUseCase updateMonthEndClarificationUseCase;

    @Inject
    CompleteMonthEndClarificationUseCase completeMonthEndClarificationUseCase;

    @Inject
    GetEmployeeMonthEndWorklistUseCase getEmployeeMonthEndWorklistUseCase;

    @Inject
    GetProjectLeadMonthEndWorklistUseCase getProjectLeadMonthEndWorklistUseCase;

    @Inject
    GetMonthEndStatusOverviewUseCase getMonthEndStatusOverviewUseCase;

    @Inject
    UserRepositoryAdapter userRepositoryAdapter;

    @Inject
    ProjectRepositoryAdapter projectRepositoryAdapter;

    @Inject
    MonthEndTaskRepositoryAdapter monthEndTaskRepositoryAdapter;

    @Inject
    MonthEndClarificationRepository monthEndClarificationRepository;

    @InjectMock
    ProjectService projectService;

    @Test
    void monthEndFlow_shouldGenerateAndCompleteEmployeeChecklistItems() {
        User employee = user("employee", Set.of(Role.EMPLOYEE));
        User lead = user("lead", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(701, Set.of(lead.getId()));
        persistFixture(List.of(employee, lead), project, Set.of(employee.getZepProfile().username()));

        MonthEndTaskGenerationResult result = generateMonthEndTasksUseCase.generate(MONTH);

        assertThat(result.month()).isEqualTo(MONTH);
        assertThat(result.created()).isEqualTo(4);
        assertThat(result.skipped()).isZero();

        MonthEndWorklist employeeWorklist = getEmployeeMonthEndWorklistUseCase.getWorklist(employee.getId(), MONTH);
        assertThat(employeeWorklist.tasks())
                .extracting(MonthEndWorklistItem::type)
                .containsExactlyInAnyOrder(
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        MonthEndTaskType.LEISTUNGSNACHWEIS
                );
        assertThat(employeeWorklist.tasks())
                .allSatisfy(task -> {
                    assertThat(task.project().id()).isEqualTo(project.getId());
                    assertThat(task.subjectEmployee().id()).isEqualTo(employee.getId());
                });

        MonthEndWorklistItem employeeTimeCheck = employeeWorklist.tasks().stream()
                .filter(task -> task.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK)
                .findFirst()
                .orElseThrow();

        completeMonthEndTaskUseCase.complete(employeeTimeCheck.taskId(), employee.getId());

        MonthEndWorklist updatedEmployeeWorklist = getEmployeeMonthEndWorklistUseCase.getWorklist(employee.getId(), MONTH);
        assertThat(updatedEmployeeWorklist.tasks())
                .singleElement()
                .satisfies(task -> {
                    assertThat(task.type()).isEqualTo(MonthEndTaskType.LEISTUNGSNACHWEIS);
                    assertThat(task.project().id()).isEqualTo(project.getId());
                    assertThat(task.subjectEmployee().id()).isEqualTo(employee.getId());
                });
    }

    @Test
    void monthEndFlow_shouldRemoveSharedLeadChecklistItemForAllEligibleLeadsAfterCompletion() {
        User employee = user("employee", Set.of(Role.EMPLOYEE));
        User leadA = user("leadA", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("leadB", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(711, Set.of(leadA.getId(), leadB.getId()));
        persistFixture(List.of(employee, leadA, leadB), project, Set.of(employee.getZepProfile().username()));

        MonthEndTaskGenerationResult result = generateMonthEndTasksUseCase.generate(MONTH);

        assertThat(result.created()).isEqualTo(4);
        assertThat(result.skipped()).isZero();

        MonthEndWorklist leadAWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(leadA.getId(), MONTH);
        MonthEndWorklist leadBWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(leadB.getId(), MONTH);

        assertThat(leadAWorklist.tasks())
                .extracting(MonthEndWorklistItem::type)
                .containsExactlyInAnyOrder(
                        MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        MonthEndTaskType.ABRECHNUNG
                );
        assertThat(leadBWorklist.tasks())
                .extracting(MonthEndWorklistItem::type)
                .containsExactlyInAnyOrder(
                        MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        MonthEndTaskType.ABRECHNUNG
                );

        MonthEndWorklistItem leadReviewTask = leadAWorklist.tasks().stream()
                .filter(task -> task.type() == MonthEndTaskType.PROJECT_LEAD_REVIEW)
                .findFirst()
                .orElseThrow();

        completeMonthEndTaskUseCase.complete(leadReviewTask.taskId(), leadA.getId());

        MonthEndWorklist updatedLeadAWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(leadA.getId(), MONTH);
        MonthEndWorklist updatedLeadBWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(leadB.getId(), MONTH);

        assertThat(updatedLeadAWorklist.tasks())
                .singleElement()
                .satisfies(task -> {
                    assertThat(task.type()).isEqualTo(MonthEndTaskType.ABRECHNUNG);
                    assertThat(task.project().id()).isEqualTo(project.getId());
                    assertThat(task.subjectEmployee()).isNull();
                });
        assertThat(updatedLeadBWorklist.tasks())
                .singleElement()
                .satisfies(task -> {
                    assertThat(task.type()).isEqualTo(MonthEndTaskType.ABRECHNUNG);
                    assertThat(task.project().id()).isEqualTo(project.getId());
                    assertThat(task.subjectEmployee()).isNull();
                });
    }

    @Test
    void monthEndFlow_shouldPrepareOwnProjectBeforeScheduledGenerationWithoutDuplicates() {
        User employee = user("employee-self-service", Set.of(Role.EMPLOYEE));
        User lead = user("lead-self-service", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(714, Set.of(lead.getId()));
        persistFixture(List.of(employee, lead), project, Set.of(employee.getZepProfile().username()));

        MonthEndPreparationResult preparation = prematureMonthEndPreparationUseCase.prepare(
                MONTH,
                project.getId(),
                employee.getId(),
                null
        );

        assertThat(preparation.ensuredTasks())
                .extracting(MonthEndTask::type)
                .containsExactly(MonthEndTaskType.EMPLOYEE_TIME_CHECK, MonthEndTaskType.LEISTUNGSNACHWEIS);

        MonthEndTaskGenerationResult generationResult = generateMonthEndTasksUseCase.generate(MONTH);
        List<MonthEndTask> allTasks = monthEndTaskRepositoryAdapter.findByMonth(MONTH);

        assertThat(generationResult.created()).isEqualTo(2);
        assertThat(generationResult.skipped()).isEqualTo(2);
        assertThat(allTasks)
                .extracting(MonthEndTask::type)
                .containsExactlyInAnyOrder(
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        MonthEndTaskType.LEISTUNGSNACHWEIS,
                        MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        MonthEndTaskType.ABRECHNUNG
                );
        assertThat(allTasks.stream()
                .filter(task -> task.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK
                        || task.type() == MonthEndTaskType.LEISTUNGSNACHWEIS)
                .map(MonthEndTask::id)
                .toList())
                .containsExactlyInAnyOrderElementsOf(
                        preparation.ensuredTasks().stream().map(MonthEndTask::id).toList()
                );
    }

    @Test
    void monthEndFlow_shouldKeepCompletedEmployeeTaskVisibleInStatusOverviewWhileWorklistStaysOpenOnly() {
        User employee = user("employee-overview", Set.of(Role.EMPLOYEE));
        User lead = user("lead-overview", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(712, Set.of(lead.getId()));
        persistFixture(List.of(employee, lead), project, Set.of(employee.getZepProfile().username()));

        generateMonthEndTasksUseCase.generate(MONTH);

        MonthEndWorklist employeeWorklist = getEmployeeMonthEndWorklistUseCase.getWorklist(employee.getId(), MONTH);
        MonthEndWorklistItem employeeTimeCheck = employeeWorklist.tasks().stream()
                .filter(task -> task.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK)
                .findFirst()
                .orElseThrow();

        completeMonthEndTaskUseCase.complete(employeeTimeCheck.taskId(), employee.getId());

        MonthEndStatusOverview statusOverview = getMonthEndStatusOverviewUseCase.getOverview(employee.getId(), MONTH);
        MonthEndWorklist updatedWorklist = getEmployeeMonthEndWorklistUseCase.getWorklist(employee.getId(), MONTH);

        assertThat(statusOverview.entries())
                .extracting(MonthEndStatusOverviewItem::type, MonthEndStatusOverviewItem::status)
                .containsExactlyInAnyOrder(
                        tuple(MonthEndTaskType.PROJECT_LEAD_REVIEW, MonthEndTaskStatus.OPEN),
                        tuple(MonthEndTaskType.EMPLOYEE_TIME_CHECK, MonthEndTaskStatus.DONE),
                        tuple(MonthEndTaskType.LEISTUNGSNACHWEIS, MonthEndTaskStatus.OPEN)
                );
        assertThat(statusOverview.entries())
                .filteredOn(item -> item.taskId().equals(employeeTimeCheck.taskId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.project().name()).isEqualTo(project.getName());
                    assertThat(item.project().id()).isEqualTo(project.getId());
                    assertThat(item.subjectEmployee()).isNotNull();
                    assertThat(item.subjectEmployee().id()).isEqualTo(employee.getId());
                    assertThat(item.subjectEmployee().fullName())
                            .isEqualTo("%s %s".formatted(employee.getName().firstname(), employee.getName().lastname()));
                    assertThat(item.canComplete()).isTrue();
                    assertThat(item.completedBy()).isEqualTo(employee.getId());
                });
        assertThat(statusOverview.entries())
                .filteredOn(item -> item.type() == MonthEndTaskType.PROJECT_LEAD_REVIEW)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.subjectEmployee()).isNotNull();
                    assertThat(item.subjectEmployee().id()).isEqualTo(employee.getId());
                    assertThat(item.canComplete()).isFalse();
                    assertThat(item.completedBy()).isNull();
                });
        assertThat(updatedWorklist.tasks())
                .extracting(MonthEndWorklistItem::type)
                .containsExactly(MonthEndTaskType.LEISTUNGSNACHWEIS);
    }

    @Test
    void monthEndFlow_shouldKeepCompletedSharedLeadTaskVisibleInStatusOverviewForEligibleLeads() {
        User employee = user("employee-shared", Set.of(Role.EMPLOYEE));
        User leadA = user("lead-shared-a", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("lead-shared-b", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(713, Set.of(leadA.getId(), leadB.getId()));
        persistFixture(List.of(employee, leadA, leadB), project, Set.of(employee.getZepProfile().username()));

        generateMonthEndTasksUseCase.generate(MONTH);

        MonthEndWorklist leadAWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(leadA.getId(), MONTH);
        MonthEndWorklistItem leadReviewTask = leadAWorklist.tasks().stream()
                .filter(task -> task.type() == MonthEndTaskType.PROJECT_LEAD_REVIEW)
                .findFirst()
                .orElseThrow();

        completeMonthEndTaskUseCase.complete(leadReviewTask.taskId(), leadA.getId());

        MonthEndStatusOverview leadAOverview = getMonthEndStatusOverviewUseCase.getOverview(leadA.getId(), MONTH);
        MonthEndStatusOverview leadBOverview = getMonthEndStatusOverviewUseCase.getOverview(leadB.getId(), MONTH);
        MonthEndWorklist updatedLeadAWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(leadA.getId(), MONTH);
        MonthEndWorklist updatedLeadBWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(leadB.getId(), MONTH);

        assertThat(leadAOverview.entries())
                .filteredOn(item -> item.taskId().equals(leadReviewTask.taskId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.status()).isEqualTo(MonthEndTaskStatus.DONE);
                    assertThat(item.project().name()).isEqualTo(project.getName());
                    assertThat(item.project().id()).isEqualTo(project.getId());
                    assertThat(item.subjectEmployee()).isNotNull();
                    assertThat(item.subjectEmployee().id()).isEqualTo(employee.getId());
                    assertThat(item.subjectEmployee().fullName())
                            .isEqualTo("%s %s".formatted(employee.getName().firstname(), employee.getName().lastname()));
                    assertThat(item.canComplete()).isTrue();
                    assertThat(item.completedBy()).isEqualTo(leadA.getId());
                });
        assertThat(leadBOverview.entries())
                .filteredOn(item -> item.taskId().equals(leadReviewTask.taskId()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.status()).isEqualTo(MonthEndTaskStatus.DONE);
                    assertThat(item.project().name()).isEqualTo(project.getName());
                    assertThat(item.project().id()).isEqualTo(project.getId());
                    assertThat(item.subjectEmployee()).isNotNull();
                    assertThat(item.subjectEmployee().id()).isEqualTo(employee.getId());
                    assertThat(item.subjectEmployee().fullName())
                            .isEqualTo("%s %s".formatted(employee.getName().firstname(), employee.getName().lastname()));
                    assertThat(item.canComplete()).isTrue();
                    assertThat(item.completedBy()).isEqualTo(leadA.getId());
                });
        assertThat(leadAOverview.entries())
                .filteredOn(item -> item.type() == MonthEndTaskType.ABRECHNUNG)
                .singleElement()
                .satisfies(item -> assertThat(item.subjectEmployee()).isNull());
        assertThat(leadBOverview.entries())
                .filteredOn(item -> item.type() == MonthEndTaskType.ABRECHNUNG)
                .singleElement()
                .satisfies(item -> assertThat(item.subjectEmployee()).isNull());
        assertThat(updatedLeadAWorklist.tasks())
                .extracting(MonthEndWorklistItem::type)
                .containsExactly(MonthEndTaskType.ABRECHNUNG);
        assertThat(updatedLeadBWorklist.tasks())
                .extracting(MonthEndWorklistItem::type)
                .containsExactly(MonthEndTaskType.ABRECHNUNG);
    }

    @Test
    void monthEndFlow_shouldExposeClarificationAndAllowPreparedTasksToCompleteBeforeScheduledGeneration() {
        User employee = user("employee-prepared", Set.of(Role.EMPLOYEE));
        User leadA = user("lead-prepared-a", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("lead-prepared-b", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(715, Set.of(leadA.getId(), leadB.getId()));
        persistFixture(List.of(employee, leadA, leadB), project, Set.of(employee.getZepProfile().username()));

        MonthEndPreparationResult preparation = prematureMonthEndPreparationUseCase.prepare(
                MONTH,
                project.getId(),
                employee.getId(),
                "I am leaving before the scheduled run."
        );

        MonthEndWorklist employeeWorklist = getEmployeeMonthEndWorklistUseCase.getWorklist(employee.getId(), MONTH);
        MonthEndWorklist leadAWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(leadA.getId(), MONTH);
        MonthEndWorklist leadBWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(leadB.getId(), MONTH);

        assertThat(preparation.hasClarification()).isTrue();
        assertThat(employeeWorklist.tasks())
                .extracting(MonthEndWorklistItem::type)
                .containsExactlyInAnyOrder(
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        MonthEndTaskType.LEISTUNGSNACHWEIS
                );
        assertThat(employeeWorklist.clarifications()).singleElement()
                .satisfies(item -> {
                    assertThat(item.clarificationId()).isEqualTo(preparation.clarification().id());
                    assertThat(item.text()).isEqualTo("I am leaving before the scheduled run.");
                });
        assertThat(leadAWorklist.tasks()).isEmpty();
        assertThat(leadAWorklist.clarifications()).singleElement()
                .satisfies(item -> assertThat(item.clarificationId()).isEqualTo(preparation.clarification().id()));
        assertThat(leadBWorklist.tasks()).isEmpty();
        assertThat(leadBWorklist.clarifications()).singleElement()
                .satisfies(item -> assertThat(item.clarificationId()).isEqualTo(preparation.clarification().id()));

        MonthEndWorklistItem preparedEmployeeTimeCheck = employeeWorklist.tasks().stream()
                .filter(task -> task.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK)
                .findFirst()
                .orElseThrow();
        completeMonthEndTaskUseCase.complete(preparedEmployeeTimeCheck.taskId(), employee.getId());

        MonthEndWorklist updatedEmployeeWorklist = getEmployeeMonthEndWorklistUseCase.getWorklist(employee.getId(), MONTH);
        assertThat(updatedEmployeeWorklist.tasks())
                .singleElement()
                .satisfies(task -> assertThat(task.type()).isEqualTo(MonthEndTaskType.LEISTUNGSNACHWEIS));
        assertThat(updatedEmployeeWorklist.clarifications()).singleElement()
                .satisfies(item -> assertThat(item.clarificationId()).isEqualTo(preparation.clarification().id()));
    }

    @Test
    void monthEndFlow_shouldGenerateExpectedTaskCardinalityAcrossOverlappingAssignmentsAndLeadRoles() {
        User user1 = user("user1", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User user2 = user("user2", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project1 = project(721, Set.of(user1.getId()));
        Project project2 = project(722, Set.of(user1.getId(), user2.getId()));
        persistFixture(
                List.of(user1, user2),
                List.of(project1, project2),
                Map.of(
                        project1.getZepId(), Set.of(user1.getZepProfile().username(), user2.getZepProfile().username()),
                        project2.getZepId(), Set.of(user2.getZepProfile().username())
                )
        );

        MonthEndTaskGenerationResult result = generateMonthEndTasksUseCase.generate(MONTH);

        assertThat(result.month()).isEqualTo(MONTH);
        assertThat(result.created()).isEqualTo(11);
        assertThat(result.skipped()).isZero();

        List<MonthEndTask> tasks = monthEndTaskRepositoryAdapter.findByMonth(MONTH);
        List<MonthEndTask> project1Tasks = tasks.stream()
                .filter(task -> task.projectId().equals(project1.getId()))
                .toList();
        List<MonthEndTask> project2Tasks = tasks.stream()
                .filter(task -> task.projectId().equals(project2.getId()))
                .toList();
        List<MonthEndTask> employeeTimeChecks = tasks.stream()
                .filter(task -> task.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK)
                .toList();
        List<MonthEndTask> leistungsnachweise = tasks.stream()
                .filter(task -> task.type() == MonthEndTaskType.LEISTUNGSNACHWEIS)
                .toList();
        List<MonthEndTask> leadReviews = tasks.stream()
                .filter(task -> task.type() == MonthEndTaskType.PROJECT_LEAD_REVIEW)
                .toList();
        List<MonthEndTask> abrechnungen = tasks.stream()
                .filter(task -> task.type() == MonthEndTaskType.ABRECHNUNG)
                .toList();

        assertThat(tasks).hasSize(11);
        assertThat(project1Tasks).hasSize(7);
        assertThat(project2Tasks).hasSize(4);
        assertThat(employeeTimeChecks).hasSize(3);
        assertThat(leistungsnachweise).hasSize(3);
        assertThat(leadReviews).hasSize(3);
        assertThat(abrechnungen).hasSize(2);

        assertThat(employeeTimeChecks)
                .extracting(MonthEndTask::subjectEmployeeId)
                .containsExactlyInAnyOrder(user1.getId(), user2.getId(), user2.getId());
        assertThat(leistungsnachweise)
                .extracting(MonthEndTask::subjectEmployeeId)
                .containsExactlyInAnyOrder(user1.getId(), user2.getId(), user2.getId());
        assertThat(leadReviews)
                .extracting(MonthEndTask::subjectEmployeeId)
                .containsExactlyInAnyOrder(user1.getId(), user2.getId(), user2.getId());
        assertThat(abrechnungen)
                .extracting(MonthEndTask::subjectEmployeeId)
                .containsOnlyNulls();

        assertThat(leadReviews.stream()
                .filter(task -> task.projectId().equals(project1.getId()))
                .toList())
                .allSatisfy(task -> assertThat(task.eligibleActorIds()).containsExactly(user1.getId()));
        assertThat(leadReviews.stream()
                .filter(task -> task.projectId().equals(project2.getId()))
                .toList())
                .singleElement()
                .satisfies(task -> assertThat(task.eligibleActorIds()).containsExactlyInAnyOrder(
                        user1.getId(),
                        user2.getId()
                ));
        assertThat(abrechnungen.stream()
                .filter(task -> task.projectId().equals(project1.getId()))
                .toList())
                .singleElement()
                .satisfies(task -> assertThat(task.eligibleActorIds()).containsExactly(user1.getId()));
        assertThat(abrechnungen.stream()
                .filter(task -> task.projectId().equals(project2.getId()))
                .toList())
                .singleElement()
                .satisfies(task -> assertThat(task.eligibleActorIds()).containsExactlyInAnyOrder(
                        user1.getId(),
                        user2.getId()
                ));

        MonthEndWorklist user1EmployeeWorklist = getEmployeeMonthEndWorklistUseCase.getWorklist(user1.getId(), MONTH);
        MonthEndWorklist user2EmployeeWorklist = getEmployeeMonthEndWorklistUseCase.getWorklist(user2.getId(), MONTH);
        MonthEndWorklist user1LeadWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(user1.getId(), MONTH);
        MonthEndWorklist user2LeadWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(user2.getId(), MONTH);

        assertThat(user1EmployeeWorklist.tasks()).hasSize(2);
        assertThat(user1EmployeeWorklist.tasks())
                .allSatisfy(task -> {
                    assertThat(task.project().id()).isEqualTo(project1.getId());
                    assertThat(task.subjectEmployee().id()).isEqualTo(user1.getId());
                });

        assertThat(user2EmployeeWorklist.tasks()).hasSize(4);
        assertThat(user2EmployeeWorklist.tasks())
                .extracting(task -> task.project().id())
                .containsExactlyInAnyOrder(project1.getId(), project1.getId(), project2.getId(), project2.getId());
        assertThat(user2EmployeeWorklist.tasks())
                .allSatisfy(task -> assertThat(task.subjectEmployee().id()).isEqualTo(user2.getId()));

        assertThat(user1LeadWorklist.tasks()).hasSize(5);
        assertThat(user2LeadWorklist.tasks()).hasSize(2);
        assertThat(user1EmployeeWorklist.clarifications()).isEmpty();
        assertThat(user1LeadWorklist.clarifications()).isEmpty();
    }

    @Test
    void monthEndFlow_shouldShowEmployeeCreatedClarificationUntilResolvedByLead() {
        User employee = user("employee-clarification", Set.of(Role.EMPLOYEE));
        User leadA = user("lead-clarification-a", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("lead-clarification-b", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(731, Set.of(leadA.getId(), leadB.getId()));
        persistFixture(List.of(employee, leadA, leadB), project, Set.of(employee.getZepProfile().username()));

        generateMonthEndTasksUseCase.generate(MONTH);

        MonthEndClarification clarification = createMonthEndClarificationUseCase.create(
                MONTH,
                project.getId(),
                employee.getId(),
                employee.getId(),
                MonthEndClarificationSide.EMPLOYEE,
                "Please verify the remaining booking."
        );

        MonthEndWorklist employeeWorklist = getEmployeeMonthEndWorklistUseCase.getWorklist(employee.getId(), MONTH);
        MonthEndWorklist leadAWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(leadA.getId(), MONTH);
        MonthEndWorklist leadBWorklist = getProjectLeadMonthEndWorklistUseCase.getWorklist(leadB.getId(), MONTH);

        assertThat(employeeWorklist.clarifications()).singleElement()
                .satisfies(item -> assertThat(item.clarificationId()).isEqualTo(clarification.id()));
        assertThat(leadAWorklist.clarifications()).singleElement()
                .satisfies(item -> assertThat(item.clarificationId()).isEqualTo(clarification.id()));
        assertThat(leadBWorklist.clarifications()).singleElement()
                .satisfies(item -> assertThat(item.clarificationId()).isEqualTo(clarification.id()));

        completeMonthEndClarificationUseCase.complete(clarification.id(), leadA.getId(), "Handled by lead.");

        assertThat(getEmployeeMonthEndWorklistUseCase.getWorklist(employee.getId(), MONTH).clarifications()).isEmpty();
        assertThat(getProjectLeadMonthEndWorklistUseCase.getWorklist(leadA.getId(), MONTH).clarifications()).isEmpty();
        assertThat(getProjectLeadMonthEndWorklistUseCase.getWorklist(leadB.getId(), MONTH).clarifications()).isEmpty();
        assertThat(monthEndClarificationRepository.findById(clarification.id()))
                .hasValueSatisfying(saved -> {
                    assertThat(saved.resolvedBy()).isEqualTo(leadA.getId());
                    assertThat(saved.resolutionNote()).isEqualTo("Handled by lead.");
                });
    }

    @Test
    void monthEndFlow_shouldAllowLeadSideEditAndEmployeeResolutionWithoutBlockingTaskCompletion() {
        User employee = user("employee-resolution", Set.of(Role.EMPLOYEE));
        User leadA = user("lead-resolution-a", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("lead-resolution-b", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(732, Set.of(leadA.getId(), leadB.getId()));
        persistFixture(List.of(employee, leadA, leadB), project, Set.of(employee.getZepProfile().username()));

        generateMonthEndTasksUseCase.generate(MONTH);

        MonthEndClarification clarification = createMonthEndClarificationUseCase.create(
                MONTH,
                project.getId(),
                employee.getId(),
                leadA.getId(),
                MonthEndClarificationSide.PROJECT_LEAD,
                "Please update the supporting note."
        );

        MonthEndClarification updatedClarification = updateMonthEndClarificationUseCase.updateText(
                clarification.id(),
                leadB.getId(),
                "Please update the supporting note before close."
        );

        MonthEndWorklist employeeWorklist = getEmployeeMonthEndWorklistUseCase.getWorklist(employee.getId(), MONTH);
        MonthEndWorklistItem employeeTimeCheck = employeeWorklist.tasks().stream()
                .filter(task -> task.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK)
                .findFirst()
                .orElseThrow();

        completeMonthEndTaskUseCase.complete(employeeTimeCheck.taskId(), employee.getId());

        MonthEndWorklist stillOpenClarificationWorklist = getEmployeeMonthEndWorklistUseCase.getWorklist(employee.getId(), MONTH);
        assertThat(stillOpenClarificationWorklist.clarifications()).singleElement()
                .satisfies(item -> {
                    assertThat(item.clarificationId()).isEqualTo(clarification.id());
                    assertThat(item.text()).isEqualTo("Please update the supporting note before close.");
                });

        completeMonthEndClarificationUseCase.complete(updatedClarification.id(), employee.getId(), "Updated and resolved.");

        assertThat(getEmployeeMonthEndWorklistUseCase.getWorklist(employee.getId(), MONTH).clarifications()).isEmpty();
        assertThat(getProjectLeadMonthEndWorklistUseCase.getWorklist(leadA.getId(), MONTH).clarifications()).isEmpty();
        assertThat(getProjectLeadMonthEndWorklistUseCase.getWorklist(leadB.getId(), MONTH).clarifications()).isEmpty();
    }

    private void persistFixture(List<User> users, Project project, Set<String> assignedUsernames) {
        persistFixture(users, List.of(project), Map.of(project.getZepId(), assignedUsernames));
    }

    private void persistFixture(List<User> users, List<Project> projects, Map<Integer, Set<String>> assignedUsernamesByProject) {
        userRepositoryAdapter.saveAll(users);
        projectRepositoryAdapter.saveAll(projects);
        for (Project project : projects) {
            when(projectService.getProjectEmployeesForId(project.getZepId(), MONTH))
                    .thenReturn(projectEmployees(assignedUsernamesByProject.getOrDefault(project.getZepId(), Set.of())));
        }
    }

    private User user(String username, Set<Role> roles) {
        return User.create(UserId.of(Instancio.create(UUID.class)), zepProfile(username), roles);
    }

    private ZepProfile zepProfile(String username) {
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

    private Project project(int zepId, Set<UserId> leadIds) {
        Project project = Project.create(
                ProjectId.of(Instancio.create(UUID.class)),
                new ZepProjectProfile(zepId, "Project-" + zepId, LocalDate.of(2025, 1, 1), null, true)
        );
        project.setLeads(leadIds.stream().map(UserId::value).collect(java.util.stream.Collectors.toSet()));
        return project;
    }

    private List<ZepProjectEmployee> projectEmployees(Set<String> assignedUsernames) {
        return assignedUsernames.stream()
                .map(username -> ZepProjectEmployee.builder().username(username).build())
                .toList();
    }
}

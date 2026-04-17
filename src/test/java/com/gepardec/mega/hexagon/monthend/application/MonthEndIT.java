package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.adapter.outbound.MonthEndTaskRepositoryAdapter;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteMonthEndTaskUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GenerateMonthEndTasksUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetEmployeeMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetProjectLeadMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.PrematureMonthEndPreparationUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.UpdateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
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
    GetEmployeeMonthEndStatusOverviewUseCase getEmployeeMonthEndStatusOverviewUseCase;

    @Inject
    GetProjectLeadMonthEndStatusOverviewUseCase getProjectLeadMonthEndStatusOverviewUseCase;

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
        Project project = project(701, Set.of(lead.id()));
        persistFixture(List.of(employee, lead), project, Set.of(employee.zepUsername().value()));

        MonthEndTaskGenerationResult result = generateMonthEndTasksUseCase.generate(MONTH);

        assertThat(result.month()).isEqualTo(MONTH);
        assertThat(result.created()).isEqualTo(4);
        assertThat(result.skipped()).isZero();

        MonthEndStatusOverview employeeOverview = getEmployeeMonthEndStatusOverviewUseCase.getOverview(employee.id(), MONTH);
        List<MonthEndTask> eligibleOpenItems = employeeOverview.tasks().stream()
                .filter(item -> item.status() == MonthEndTaskStatus.OPEN && item.canBeCompletedBy(employee.id()))
                .toList();
        assertThat(eligibleOpenItems)
                .extracting(MonthEndTask::type)
                .containsExactlyInAnyOrder(
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        MonthEndTaskType.LEISTUNGSNACHWEIS
                );
        assertThat(eligibleOpenItems)
                .allSatisfy(item -> {
                    assertThat(item.projectId()).isEqualTo(project.id());
                    assertThat(item.subjectEmployeeId()).isEqualTo(employee.id());
                });

        MonthEndTask employeeTimeCheck = eligibleOpenItems.stream()
                .filter(item -> item.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK)
                .findFirst()
                .orElseThrow();

        completeMonthEndTaskUseCase.complete(employeeTimeCheck.id(), employee.id());

        MonthEndStatusOverview updatedOverview = getEmployeeMonthEndStatusOverviewUseCase.getOverview(employee.id(), MONTH);
        List<MonthEndTask> updatedEligibleOpenItems = updatedOverview.tasks().stream()
                .filter(item -> item.status() == MonthEndTaskStatus.OPEN && item.canBeCompletedBy(employee.id()))
                .toList();
        assertThat(updatedEligibleOpenItems)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.type()).isEqualTo(MonthEndTaskType.LEISTUNGSNACHWEIS);
                    assertThat(item.projectId()).isEqualTo(project.id());
                    assertThat(item.subjectEmployeeId()).isEqualTo(employee.id());
                });
    }

    @Test
    void monthEndFlow_shouldRemoveSharedLeadChecklistItemForAllEligibleLeadsAfterCompletion() {
        User employee = user("employee", Set.of(Role.EMPLOYEE));
        User leadA = user("leadA", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("leadB", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(711, Set.of(leadA.id(), leadB.id()));
        persistFixture(List.of(employee, leadA, leadB), project, Set.of(employee.zepUsername().value()));

        MonthEndTaskGenerationResult result = generateMonthEndTasksUseCase.generate(MONTH);

        assertThat(result.created()).isEqualTo(4);
        assertThat(result.skipped()).isZero();

        MonthEndStatusOverview leadAOverview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(leadA.id(), MONTH);
        MonthEndStatusOverview leadBOverview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(leadB.id(), MONTH);
        List<MonthEndTask> leadAEligibleOpen = leadAOverview.tasks().stream()
                .filter(item -> item.status() == MonthEndTaskStatus.OPEN && item.canBeCompletedBy(leadA.id()))
                .toList();
        List<MonthEndTask> leadBEligibleOpen = leadBOverview.tasks().stream()
                .filter(item -> item.status() == MonthEndTaskStatus.OPEN && item.canBeCompletedBy(leadB.id()))
                .toList();

        assertThat(leadAEligibleOpen)
                .extracting(MonthEndTask::type)
                .containsExactlyInAnyOrder(
                        MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        MonthEndTaskType.ABRECHNUNG
                );
        assertThat(leadBEligibleOpen)
                .extracting(MonthEndTask::type)
                .containsExactlyInAnyOrder(
                        MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        MonthEndTaskType.ABRECHNUNG
                );

        MonthEndTask leadReviewItem = leadAEligibleOpen.stream()
                .filter(item -> item.type() == MonthEndTaskType.PROJECT_LEAD_REVIEW)
                .findFirst()
                .orElseThrow();

        completeMonthEndTaskUseCase.complete(leadReviewItem.id(), leadA.id());

        MonthEndStatusOverview updatedLeadAOverview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(leadA.id(), MONTH);
        MonthEndStatusOverview updatedLeadBOverview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(leadB.id(), MONTH);
        List<MonthEndTask> updatedLeadAEligibleOpen = updatedLeadAOverview.tasks().stream()
                .filter(item -> item.status() == MonthEndTaskStatus.OPEN && item.canBeCompletedBy(leadA.id()))
                .toList();
        List<MonthEndTask> updatedLeadBEligibleOpen = updatedLeadBOverview.tasks().stream()
                .filter(item -> item.status() == MonthEndTaskStatus.OPEN && item.canBeCompletedBy(leadB.id()))
                .toList();

        assertThat(updatedLeadAEligibleOpen)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.type()).isEqualTo(MonthEndTaskType.ABRECHNUNG);
                    assertThat(item.projectId()).isEqualTo(project.id());
                    assertThat(item.subjectEmployeeId()).isNull();
                });
        assertThat(updatedLeadBEligibleOpen)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.type()).isEqualTo(MonthEndTaskType.ABRECHNUNG);
                    assertThat(item.projectId()).isEqualTo(project.id());
                    assertThat(item.subjectEmployeeId()).isNull();
                });
    }

    @Test
    void monthEndFlow_shouldPrepareOwnProjectBeforeScheduledGenerationWithoutDuplicates() {
        User employee = user("employee-self-service", Set.of(Role.EMPLOYEE));
        User lead = user("lead-self-service", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(714, Set.of(lead.id()));
        persistFixture(List.of(employee, lead), project, Set.of(employee.zepUsername().value()));

        MonthEndPreparationResult preparation = prematureMonthEndPreparationUseCase.prepare(
                MONTH,
                project.id(),
                employee.id(),
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
    void monthEndFlow_shouldKeepCompletedEmployeeTaskVisibleInStatusOverview() {
        User employee = user("employee-overview", Set.of(Role.EMPLOYEE));
        User lead = user("lead-overview", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(712, Set.of(lead.id()));
        persistFixture(List.of(employee, lead), project, Set.of(employee.zepUsername().value()));

        generateMonthEndTasksUseCase.generate(MONTH);

        MonthEndStatusOverview employeeOverview = getEmployeeMonthEndStatusOverviewUseCase.getOverview(employee.id(), MONTH);
        MonthEndTask employeeTimeCheck = employeeOverview.tasks().stream()
                .filter(item -> item.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK)
                .findFirst()
                .orElseThrow();

        completeMonthEndTaskUseCase.complete(employeeTimeCheck.id(), employee.id());

        MonthEndStatusOverview statusOverview = getEmployeeMonthEndStatusOverviewUseCase.getOverview(employee.id(), MONTH);

        assertThat(statusOverview.tasks())
                .extracting(MonthEndTask::type, MonthEndTask::status)
                .containsExactlyInAnyOrder(
                        tuple(MonthEndTaskType.PROJECT_LEAD_REVIEW, MonthEndTaskStatus.OPEN),
                        tuple(MonthEndTaskType.EMPLOYEE_TIME_CHECK, MonthEndTaskStatus.DONE),
                        tuple(MonthEndTaskType.LEISTUNGSNACHWEIS, MonthEndTaskStatus.OPEN)
                );
        assertThat(statusOverview.tasks())
                .filteredOn(item -> item.id().equals(employeeTimeCheck.id()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.projectId()).isEqualTo(project.id());
                    assertThat(item.subjectEmployeeId()).isNotNull();
                    assertThat(item.subjectEmployeeId()).isEqualTo(employee.id());
                    assertThat(item.canBeCompletedBy(employee.id())).isTrue();
                    assertThat(item.completedBy()).isEqualTo(employee.id());
                });
        assertThat(statusOverview.tasks())
                .filteredOn(item -> item.type() == MonthEndTaskType.PROJECT_LEAD_REVIEW)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.subjectEmployeeId()).isNotNull();
                    assertThat(item.subjectEmployeeId()).isEqualTo(employee.id());
                    assertThat(item.canBeCompletedBy(employee.id())).isFalse();
                    assertThat(item.completedBy()).isNull();
                });
    }

    @Test
    void monthEndFlow_shouldKeepCompletedSharedLeadTaskVisibleInStatusOverviewForEligibleLeads() {
        User employee = user("employee-shared", Set.of(Role.EMPLOYEE));
        User leadA = user("lead-shared-a", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("lead-shared-b", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(713, Set.of(leadA.id(), leadB.id()));
        persistFixture(List.of(employee, leadA, leadB), project, Set.of(employee.zepUsername().value()));

        generateMonthEndTasksUseCase.generate(MONTH);

        MonthEndStatusOverview leadAInitialOverview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(leadA.id(), MONTH);
        MonthEndTask leadReviewTask = leadAInitialOverview.tasks().stream()
                .filter(item -> item.type() == MonthEndTaskType.PROJECT_LEAD_REVIEW)
                .findFirst()
                .orElseThrow();

        completeMonthEndTaskUseCase.complete(leadReviewTask.id(), leadA.id());

        MonthEndStatusOverview leadAOverview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(leadA.id(), MONTH);
        MonthEndStatusOverview leadBOverview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(leadB.id(), MONTH);

        assertThat(leadAOverview.tasks())
                .filteredOn(item -> item.id().equals(leadReviewTask.id()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.status()).isEqualTo(MonthEndTaskStatus.DONE);
                    assertThat(item.projectId()).isEqualTo(project.id());
                    assertThat(item.subjectEmployeeId()).isNotNull();
                    assertThat(item.subjectEmployeeId()).isEqualTo(employee.id());
                    assertThat(item.canBeCompletedBy(leadA.id())).isTrue();
                    assertThat(item.completedBy()).isEqualTo(leadA.id());
                });
        assertThat(leadBOverview.tasks())
                .filteredOn(item -> item.id().equals(leadReviewTask.id()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.status()).isEqualTo(MonthEndTaskStatus.DONE);
                    assertThat(item.projectId()).isEqualTo(project.id());
                    assertThat(item.subjectEmployeeId()).isNotNull();
                    assertThat(item.subjectEmployeeId()).isEqualTo(employee.id());
                    assertThat(item.canBeCompletedBy(leadB.id())).isTrue();
                    assertThat(item.completedBy()).isEqualTo(leadA.id());
                });
        assertThat(leadAOverview.tasks())
                .filteredOn(item -> item.type() == MonthEndTaskType.ABRECHNUNG)
                .singleElement()
                .satisfies(item -> assertThat(item.subjectEmployeeId()).isNull());
        assertThat(leadBOverview.tasks())
                .filteredOn(item -> item.type() == MonthEndTaskType.ABRECHNUNG)
                .singleElement()
                .satisfies(item -> assertThat(item.subjectEmployeeId()).isNull());
    }

    @Test
    void monthEndFlow_shouldExposeClarificationAndAllowPreparedTasksToCompleteBeforeScheduledGeneration() {
        User employee = user("employee-prepared", Set.of(Role.EMPLOYEE));
        User leadA = user("lead-prepared-a", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("lead-prepared-b", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(715, Set.of(leadA.id(), leadB.id()));
        persistFixture(List.of(employee, leadA, leadB), project, Set.of(employee.zepUsername().value()));

        MonthEndPreparationResult preparation = prematureMonthEndPreparationUseCase.prepare(
                MONTH,
                project.id(),
                employee.id(),
                "I am leaving before the scheduled run."
        );

        MonthEndStatusOverview employeeOverview = getEmployeeMonthEndStatusOverviewUseCase.getOverview(employee.id(), MONTH);
        MonthEndStatusOverview leadAOverview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(leadA.id(), MONTH);
        MonthEndStatusOverview leadBOverview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(leadB.id(), MONTH);

        assertThat(preparation.hasClarification()).isTrue();
        List<MonthEndTask> employeeEligibleOpen = employeeOverview.tasks().stream()
                .filter(item -> item.status() == MonthEndTaskStatus.OPEN && item.canBeCompletedBy(employee.id()))
                .toList();
        assertThat(employeeEligibleOpen)
                .extracting(MonthEndTask::type)
                .containsExactlyInAnyOrder(
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        MonthEndTaskType.LEISTUNGSNACHWEIS
                );
        assertThat(employeeOverview.clarifications()).singleElement()
                .satisfies(c -> {
                    assertThat(c.id()).isEqualTo(preparation.clarification().id());
                    assertThat(c.text()).isEqualTo("I am leaving before the scheduled run.");
                });
        assertThat(leadAOverview.tasks().stream()
                .filter(item -> item.status() == MonthEndTaskStatus.OPEN && item.canBeCompletedBy(leadA.id()))
                .toList()).isEmpty();
        assertThat(leadAOverview.clarifications()).singleElement()
                .satisfies(c -> assertThat(c.id()).isEqualTo(preparation.clarification().id()));
        assertThat(leadBOverview.tasks().stream()
                .filter(item -> item.status() == MonthEndTaskStatus.OPEN && item.canBeCompletedBy(leadB.id()))
                .toList()).isEmpty();
        assertThat(leadBOverview.clarifications()).singleElement()
                .satisfies(c -> assertThat(c.id()).isEqualTo(preparation.clarification().id()));

        MonthEndTask preparedEmployeeTimeCheck = employeeEligibleOpen.stream()
                .filter(item -> item.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK)
                .findFirst()
                .orElseThrow();
        completeMonthEndTaskUseCase.complete(preparedEmployeeTimeCheck.id(), employee.id());

        MonthEndStatusOverview updatedEmployeeOverview = getEmployeeMonthEndStatusOverviewUseCase.getOverview(employee.id(), MONTH);
        List<MonthEndTask> updatedEligibleOpen = updatedEmployeeOverview.tasks().stream()
                .filter(item -> item.status() == MonthEndTaskStatus.OPEN && item.canBeCompletedBy(employee.id()))
                .toList();
        assertThat(updatedEligibleOpen)
                .singleElement()
                .satisfies(item -> assertThat(item.type()).isEqualTo(MonthEndTaskType.LEISTUNGSNACHWEIS));
        assertThat(updatedEmployeeOverview.clarifications()).singleElement()
                .satisfies(c -> assertThat(c.id()).isEqualTo(preparation.clarification().id()));
    }

    @Test
    void monthEndFlow_shouldGenerateExpectedTaskCardinalityAcrossOverlappingAssignmentsAndLeadRoles() {
        User user1 = user("user1", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User user2 = user("user2", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project1 = project(721, Set.of(user1.id()));
        Project project2 = project(722, Set.of(user1.id(), user2.id()));
        persistFixture(
                List.of(user1, user2),
                List.of(project1, project2),
                Map.of(
                        project1.zepId(), Set.of(user1.zepUsername().value(), user2.zepUsername().value()),
                        project2.zepId(), Set.of(user2.zepUsername().value())
                )
        );

        MonthEndTaskGenerationResult result = generateMonthEndTasksUseCase.generate(MONTH);

        assertThat(result.month()).isEqualTo(MONTH);
        assertThat(result.created()).isEqualTo(11);
        assertThat(result.skipped()).isZero();

        List<MonthEndTask> tasks = monthEndTaskRepositoryAdapter.findByMonth(MONTH);
        List<MonthEndTask> project1Tasks = tasks.stream()
                .filter(task -> task.projectId().equals(project1.id()))
                .toList();
        List<MonthEndTask> project2Tasks = tasks.stream()
                .filter(task -> task.projectId().equals(project2.id()))
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
                .containsExactlyInAnyOrder(user1.id(), user2.id(), user2.id());
        assertThat(leistungsnachweise)
                .extracting(MonthEndTask::subjectEmployeeId)
                .containsExactlyInAnyOrder(user1.id(), user2.id(), user2.id());
        assertThat(leadReviews)
                .extracting(MonthEndTask::subjectEmployeeId)
                .containsExactlyInAnyOrder(user1.id(), user2.id(), user2.id());
        assertThat(abrechnungen)
                .extracting(MonthEndTask::subjectEmployeeId)
                .containsOnlyNulls();

        assertThat(leadReviews.stream()
                .filter(task -> task.projectId().equals(project1.id()))
                .toList())
                .allSatisfy(task -> assertThat(task.eligibleActorIds()).containsExactly(user1.id()));
        assertThat(leadReviews.stream()
                .filter(task -> task.projectId().equals(project2.id()))
                .toList())
                .singleElement()
                .satisfies(task -> assertThat(task.eligibleActorIds()).containsExactlyInAnyOrder(
                        user1.id(),
                        user2.id()
                ));
        assertThat(abrechnungen.stream()
                .filter(task -> task.projectId().equals(project1.id()))
                .toList())
                .singleElement()
                .satisfies(task -> assertThat(task.eligibleActorIds()).containsExactly(user1.id()));
        assertThat(abrechnungen.stream()
                .filter(task -> task.projectId().equals(project2.id()))
                .toList())
                .singleElement()
                .satisfies(task -> assertThat(task.eligibleActorIds()).containsExactlyInAnyOrder(
                        user1.id(),
                        user2.id()
                ));
    }

    @Test
    void monthEndFlow_shouldShowEmployeeCreatedClarificationUntilResolvedByLead() {
        User employee = user("employee-clarification", Set.of(Role.EMPLOYEE));
        User leadA = user("lead-clarification-a", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("lead-clarification-b", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(731, Set.of(leadA.id(), leadB.id()));
        persistFixture(List.of(employee, leadA, leadB), project, Set.of(employee.zepUsername().value()));

        generateMonthEndTasksUseCase.generate(MONTH);

        MonthEndClarification clarification = createMonthEndClarificationUseCase.create(
                MONTH,
                project.id(),
                employee.id(),
                employee.id(),
                "Please verify the remaining booking."
        );

        MonthEndStatusOverview employeeOverview = getEmployeeMonthEndStatusOverviewUseCase.getOverview(employee.id(), MONTH);
        MonthEndStatusOverview leadAOverview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(leadA.id(), MONTH);
        MonthEndStatusOverview leadBOverview = getProjectLeadMonthEndStatusOverviewUseCase.getOverview(leadB.id(), MONTH);

        assertThat(employeeOverview.clarifications()).singleElement()
                .satisfies(c -> assertThat(c.id()).isEqualTo(clarification.id()));
        assertThat(leadAOverview.clarifications()).singleElement()
                .satisfies(c -> assertThat(c.id()).isEqualTo(clarification.id()));
        assertThat(leadBOverview.clarifications()).singleElement()
                .satisfies(c -> assertThat(c.id()).isEqualTo(clarification.id()));

        completeMonthEndClarificationUseCase.complete(clarification.id(), leadA.id(), "Handled by lead.");

        assertThat(monthEndClarificationRepository.findById(clarification.id()))
                .hasValueSatisfying(saved -> {
                    assertThat(saved.resolvedBy()).isEqualTo(leadA.id());
                    assertThat(saved.resolutionNote()).isEqualTo("Handled by lead.");
                });
    }

    @Test
    void monthEndFlow_shouldAllowLeadSideEditAndEmployeeResolutionWithoutBlockingTaskCompletion() {
        User employee = user("employee-resolution", Set.of(Role.EMPLOYEE));
        User leadA = user("lead-resolution-a", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        User leadB = user("lead-resolution-b", Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD));
        Project project = project(732, Set.of(leadA.id(), leadB.id()));
        persistFixture(List.of(employee, leadA, leadB), project, Set.of(employee.zepUsername().value()));

        generateMonthEndTasksUseCase.generate(MONTH);

        MonthEndClarification clarification = createMonthEndClarificationUseCase.create(
                MONTH,
                project.id(),
                employee.id(),
                leadA.id(),
                "Please update the supporting note."
        );

        MonthEndClarification updatedClarification = updateMonthEndClarificationUseCase.updateText(
                clarification.id(),
                leadA.id(),
                "Please update the supporting note before close."
        );

        MonthEndStatusOverview employeeOverview = getEmployeeMonthEndStatusOverviewUseCase.getOverview(employee.id(), MONTH);
        MonthEndTask employeeTimeCheck = employeeOverview.tasks().stream()
                .filter(item -> item.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK)
                .findFirst()
                .orElseThrow();

        completeMonthEndTaskUseCase.complete(employeeTimeCheck.id(), employee.id());

        MonthEndStatusOverview updatedEmployeeOverview = getEmployeeMonthEndStatusOverviewUseCase.getOverview(employee.id(), MONTH);
        assertThat(updatedEmployeeOverview.clarifications()).singleElement()
                .satisfies(c -> {
                    assertThat(c.id()).isEqualTo(clarification.id());
                    assertThat(c.text()).isEqualTo("Please update the supporting note before close.");
                });

        completeMonthEndClarificationUseCase.complete(updatedClarification.id(), employee.id(), "Updated and resolved.");

        assertThat(monthEndClarificationRepository.findById(updatedClarification.id()))
                .hasValueSatisfying(saved -> assertThat(saved.resolvedBy()).isEqualTo(employee.id()));
    }

    private void persistFixture(List<User> users, Project project, Set<String> assignedUsernames) {
        persistFixture(users, List.of(project), Map.of(project.zepId(), assignedUsernames));
    }

    private void persistFixture(List<User> users, List<Project> projects, Map<Integer, Set<String>> assignedUsernamesByProject) {
        userRepositoryAdapter.saveAll(users);
        projectRepositoryAdapter.saveAll(projects);
        for (Project project : projects) {
            when(projectService.getProjectEmployeesForId(project.zepId(), MONTH))
                    .thenReturn(projectEmployees(assignedUsernamesByProject.getOrDefault(project.zepId(), Set.of())));
        }
    }

    private User user(String username, Set<Role> roles) {
        return new User(
                UserId.of(Instancio.create(UUID.class)),
                Email.of(username + "@example.com"),
                FullName.of("Test", "User"),
                ZepUsername.of(username),
                null,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null)),
                roles
        );
    }

    private Project project(int zepId, Set<UserId> leadIds) {
        return Project.create(
                ProjectId.of(Instancio.create(UUID.class)),
                new ZepProjectProfile(zepId, "Project-" + zepId, LocalDate.of(2025, 1, 1), null, true)
        ).withLeads(leadIds);
    }

    private List<ZepProjectEmployee> projectEmployees(Set<String> assignedUsernames) {
        return assignedUsernames.stream()
                .map(username -> ZepProjectEmployee.builder().username(username).build())
                .toList();
    }
}

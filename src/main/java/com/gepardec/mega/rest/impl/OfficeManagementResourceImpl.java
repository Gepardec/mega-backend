package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.MegaRolesAllowed;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.entity.project.ProjectEntry;
import com.gepardec.mega.db.entity.project.ProjectStep;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.FinishedAndTotalComments;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectEmployees;
import com.gepardec.mega.domain.model.ProjectFilter;
import com.gepardec.mega.domain.model.ProjectState;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.State;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.rest.api.OfficeManagementResource;
import com.gepardec.mega.rest.mapper.EmployeeMapper;
import com.gepardec.mega.rest.model.CustomerProjectWithoutLeadsDto;
import com.gepardec.mega.rest.model.OfficeManagementEntryDto;
import com.gepardec.mega.rest.model.PmProgressDto;
import com.gepardec.mega.rest.model.ProjectOverviewDto;
import com.gepardec.mega.rest.provider.PayrollContext;
import com.gepardec.mega.rest.provider.PayrollMonthProvider;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.ProjectCommentService;
import com.gepardec.mega.service.api.ProjectEntryService;
import com.gepardec.mega.service.api.ProjectService;
import com.gepardec.mega.service.api.StepEntryService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestScoped
@Authenticated
@MegaRolesAllowed(value = {Role.OFFICE_MANAGEMENT})
public class OfficeManagementResourceImpl implements OfficeManagementResource {

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";

    @Inject
    @PayrollContext(PayrollContext.PayrollContextType.MANAGEMENT)
    PayrollMonthProvider payrollMonthProvider;

    @Inject
    StepEntryService stepEntryService;

    @Inject
    EmployeeService employeeService;

    @Inject
    UserContext userContext;

    @Inject
    ProjectEntryService projectEntryService;

    @Inject
    ProjectCommentService projectCommentService;

    @Inject
    EmployeeMapper employeeMapper;

    @Inject
    CommentService commentService;

    @Inject
    ProjectService projectService;

    @Inject
    Logger logger;

    @Override
    public Response getAllOfficeManagementEntries() {
        return getAllOfficeManagementEntries(payrollMonthProvider.getPayrollMonth());
    }

    @Override
    public Response getAllOfficeManagementEntries(YearMonth payrollMonth) {
        List<OfficeManagementEntryDto> officeManagementEntries = new ArrayList<>();
        List<Employee> employees = employeeService.getAllEmployeesConsideringExitDate(payrollMonth);

        for (Employee employee : employees) {
            List<StepEntry> stepEntries = stepEntryService.findAllStepEntriesForEmployee(employee, payrollMonth);

            List<StepEntry> allOwnedStepEntriesForPMProgress = stepEntryService.findAllOwnedAndUnassignedStepEntriesForPMProgress(employee.getEmail(), payrollMonth);
            List<PmProgressDto> pmProgressDtos = new ArrayList<>();

            allOwnedStepEntriesForPMProgress
                    .forEach(stepEntry -> pmProgressDtos.add(
                            PmProgressDto.builder()
                                    .project(stepEntry.getProject())
                                    .assigneeEmail(stepEntry.getAssignee().getEmail())
                                    .state(stepEntry.getState())
                                    .stepId(stepEntry.getStep().getId())
                                    .firstname(stepEntry.getAssignee().getFirstname())
                                    .lastname(stepEntry.getAssignee().getLastname())
                                    .build()
                    ));

            OfficeManagementEntryDto officeManagementEntryDto = createManagementEntryForEmployee(
                    employee,
                    stepEntries,
                    payrollMonth,
                    pmProgressDtos
            );

            if (officeManagementEntryDto != null) {
                officeManagementEntries.add(officeManagementEntryDto);
            }

        }

        return Response.ok(officeManagementEntries).build();
    }

    @Override
    public Response getProjectOverview(YearMonth payrollMonth) {
        List<ProjectEmployees> projectEmployees = stepEntryService.getAllProjectEmployeesForPM(payrollMonth);
        Map<String, Employee> employees = createEmployeeCache(payrollMonth);

        List<ProjectOverviewDto> projectOverviewDtos = projectEmployees.stream()
                .map(currentProject -> projectOverviewDto(currentProject, employees, payrollMonth))
                .toList();


        return Response.ok(projectOverviewDtos).build();
    }

    @Override
    public Response getProjectsWithoutLeads() {
        YearMonth fetchDate = payrollMonthProvider.getPayrollMonth().plusMonths(1);
        List<Project> customerProjectsWithoutLeads = projectService.getProjectsForMonthYear(
                fetchDate,
                List.of(ProjectFilter.IS_CUSTOMER_PROJECT, ProjectFilter.WITHOUT_LEADS)
        );

        List<CustomerProjectWithoutLeadsDto> customerProjectsWithoutLeadsDto = customerProjectsWithoutLeads.stream()
                .map(project -> CustomerProjectWithoutLeadsDto.builder()
                        .projectName(project.getProjectId())
                        .fetchDate(fetchDate.atDay(1))
                        .comment("Dieses Projekt hat keinen Projektleiter zugewiesen. Bitte in ZEP hinzufügen!")
                        .zepId(project.getZepId())
                        .build())
                .toList();

        return Response.ok(customerProjectsWithoutLeadsDto).build();
    }

    private Map<String, Employee> createEmployeeCache(YearMonth selectedYearMonth) {
        return employeeService.getAllEmployeesConsideringExitDate(selectedYearMonth).stream()
                .collect(Collectors.toMap(Employee::getUserId, employee -> employee));
    }

    private ProjectOverviewDto projectOverviewDto(ProjectEmployees projectEmployees, Map<String, Employee> employees, YearMonth payrollMonth) {
        List<ProjectEntry> projectEntries = projectEntryService.findByNameAndDate(projectEmployees.getProjectId(), payrollMonth);
        return new ProjectOverviewDto(
                Optional.ofNullable(projectEntries.getFirst())
                        .map(ProjectEntry::getProject)
                        .map(com.gepardec.mega.db.entity.project.Project::getZepId)
                        .orElse(null),
                projectEmployees.getProjectId(),
                extractStateForProject(
                        projectEmployees.getEmployees().stream()
                                .filter(employees::containsKey)
                                .map(employees::get)
                                .map(employee ->
                                        stepEntryService.findAllStepEntriesForEmployeeAndProject(
                                                employee,
                                                projectEmployees.getProjectId(),
                                                Objects.requireNonNull(userContext.getUser()).getEmail(),
                                                payrollMonth
                                        )
                                )
                                .flatMap(List::stream)
                                .toList()
                ),
                ProjectState.byName(
                        getProjectEntryForProjectStep(projectEntries, ProjectStep.CONTROL_PROJECT).getState().name()
                ),
                ProjectState.byName(
                        getProjectEntryForProjectStep(projectEntries, ProjectStep.CONTROL_BILLING).getState().name()
                ),
                projectCommentService.findForProjectNameWithCurrentYearMonth(projectEmployees.getProjectId(), payrollMonth.atDay(1).toString())
        );
    }

    private ProjectEntry getProjectEntryForProjectStep(List<ProjectEntry> projectEntries, ProjectStep projectStep) {
        return projectEntries.stream()
                .filter(p -> p.getStep() == projectStep)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No project entry found for project step '%s'".formatted(projectStep)));
    }

    private OfficeManagementEntryDto createManagementEntryForEmployee(Employee employee, List<StepEntry> stepEntries, YearMonth payrollMonth, List<PmProgressDto> pmProgressDtos) {
        FinishedAndTotalComments finishedAndTotalComments = commentService.countFinishedAndTotalComments(employee.getEmail(), payrollMonth);

        if (!stepEntries.isEmpty()) {
            Pair<State, String> employeeCheckStatePair = extractEmployeeCheckState(stepEntries);

            State employeeCheckState;
            String employeeCheckStateReason = null;

            if (employeeCheckStatePair == null) {
                // Wenn aus irgendeinem Grund kein CONTROL_TIMES Step gefunden wurde, ist der Mitarbeiter auf OPEN
                employeeCheckState = State.OPEN;
                Long userId = stepEntries.stream().findFirst().map(StepEntry::getOwner).map(User::getId).orElse(null);

                logger.error("Für Mitarbeiter [ID: {}] wurde kein CONTROL_TIMES step gefunden.", userId);
            } else {
                employeeCheckState = employeeCheckStatePair.getLeft();
                employeeCheckStateReason = employeeCheckStatePair.getRight();
            }

            return new OfficeManagementEntryDto(
                    employeeMapper.mapToDto(employee),
                    employeeCheckState,
                    employeeCheckStateReason,
                    extractInternalCheckState(stepEntries),
                    extractStateForProject(stepEntries),
                    pmProgressDtos,
                    finishedAndTotalComments.getTotalComments(),
                    finishedAndTotalComments.getFinishedComments(),
                    stepEntries.getFirst().getDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN))
            );
        }

        return null;
    }

    private State extractInternalCheckState(List<StepEntry> stepEntries) {
        boolean internalCheckStateOpen = stepEntries.stream()
                .filter(stepEntry ->
                        StepName.CONTROL_INTERNAL_TIMES.name().equalsIgnoreCase(stepEntry.getStep().getName())
                                && Strings.CS.equals(
                                Objects.requireNonNull(userContext.getUser()).getEmail(),
                                stepEntry.getAssignee().getEmail()
                        )
                ).anyMatch(stepEntry -> EmployeeState.OPEN.equals(stepEntry.getState()));

        return internalCheckStateOpen ? State.OPEN : State.DONE;
    }

    private State extractStateForProject(List<StepEntry> stepEntries) {
        List<EmployeeState> collectedStates = stepEntries
                .stream()
                .filter(se -> StepName.CONTROL_TIME_EVIDENCES.name().equalsIgnoreCase(se.getStep().getName()))
                .map(StepEntry::getState)
                .toList();

        if (collectedStates.isEmpty()) {
            return State.DONE;
        }

        return collectedStates.stream()
                .allMatch(state -> state.equals(EmployeeState.DONE)) ? State.DONE : State.OPEN;
    }

    /**
     * @return Pair.left: state, pair.right: stateReason
     */
    private Pair<State, String> extractEmployeeCheckState(List<StepEntry> stepEntries) {

        return stepEntries.stream()
                .filter(stepEntry -> StepName.CONTROL_TIMES.name().equalsIgnoreCase(stepEntry.getStep().getName()))
                .findFirst()
                .map(entry -> Pair.of(mapEmployeeStateToManagementState(entry.getState()), entry.getStateReason()))
                .orElse(null);
    }

    private State mapEmployeeStateToManagementState(EmployeeState employeeState) {
        return switch (employeeState) {
            case DONE -> State.DONE;
            case OPEN -> State.OPEN;
            case IN_PROGRESS -> State.IN_PROGRESS;
            default -> null;
        };
    }
}

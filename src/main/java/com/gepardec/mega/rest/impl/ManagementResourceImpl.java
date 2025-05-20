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
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.State;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.rest.api.ManagementResource;
import com.gepardec.mega.rest.mapper.EmployeeMapper;
import com.gepardec.mega.rest.model.CustomerProjectWithoutLeadsDto;
import com.gepardec.mega.rest.model.ManagementEntryDto;
import com.gepardec.mega.rest.model.PmProgressDto;
import com.gepardec.mega.rest.model.ProjectManagementEntryDto;
import com.gepardec.mega.rest.provider.PayrollContext;
import com.gepardec.mega.rest.provider.PayrollMonthProvider;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.ProjectEntryService;
import com.gepardec.mega.service.api.ProjectService;
import com.gepardec.mega.service.api.StepEntryService;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.impl.Rest;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gepardec.mega.rest.provider.PayrollContext.PayrollContextType.MANAGEMENT;

@RequestScoped
@Authenticated
@MegaRolesAllowed(value = {Role.PROJECT_LEAD, Role.OFFICE_MANAGEMENT})
public class ManagementResourceImpl implements ManagementResource {

    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";

    @Inject
    EmployeeService employeeService;

    @Inject
    StepEntryService stepEntryService;

    @Inject
    CommentService commentService;

    @Inject
    UserContext userContext;

    @Inject
    ProjectEntryService projectEntryService;

    @Inject
    ZepService zepService;

    @Inject
    ProjectService projectService;

    @Inject
    WorkingTimeUtil workingTimeUtil;

    @Inject
    EmployeeMapper employeeMapper;

    @Inject
    @Rest
    ZepService zepRestService;

    @Inject
    @PayrollContext(MANAGEMENT)
    PayrollMonthProvider payrollMonthProvider;

    @Inject
    Logger logger;

    @Override
    public Response getAllOfficeManagementEntries(boolean projectStateLogicSingle) {
        return getAllOfficeManagementEntries(payrollMonthProvider.getPayrollMonth(), projectStateLogicSingle);
    }

    @Override
    public Response getAllOfficeManagementEntries(YearMonth payrollMonth, boolean projectStateLogicSingle) {
        List<ManagementEntryDto> officeManagementEntries = new ArrayList<>();
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

            ManagementEntryDto newManagementEntryDto = createManagementEntryForEmployee(
                    employee,
                    stepEntries,
                    payrollMonth,
                    pmProgressDtos,
                    projectStateLogicSingle
            );

            if (newManagementEntryDto != null) {
                officeManagementEntries.add(newManagementEntryDto);
            }

        }

        return Response.ok(officeManagementEntries).build();
    }

    @Override
    public Response getAllProjectManagementEntries(boolean allProjects, boolean projectStateLogicSingle) {
        return getAllProjectManagementEntries(
                payrollMonthProvider.getPayrollMonth(),
                allProjects,
                projectStateLogicSingle
        );
    }

    @Override
    public Response getAllProjectManagementEntries(YearMonth payrollMonth, boolean allProjects, boolean projectStateLogicSingle) {
        validateUserContext();

        List<ProjectEmployees> projectEmployees;

        if (allProjects) {
            projectEmployees = stepEntryService.getAllProjectEmployeesForPM(payrollMonth);
        } else {
            projectEmployees = stepEntryService.getProjectEmployeesForPM(
                    payrollMonth,
                    Objects.requireNonNull(userContext.getUser()).getEmail()
            );
        }

        List<ProjectManagementEntryDto> projectManagementEntries = new ArrayList<>();

        Map<String, Employee> employees = createEmployeeCache(payrollMonth);

        for (ProjectEmployees currentProject : projectEmployees) {
            ProjectManagementEntryDto projectManagementEntryDto = loadProjectManagementEntryDto(
                    currentProject,
                    employees,
                    payrollMonth,
                    projectStateLogicSingle
            );

            if (projectManagementEntryDto != null) {
                projectManagementEntries.add(projectManagementEntryDto);
            }
        }

        return Response.ok(projectManagementEntries).build();
    }

    private ProjectManagementEntryDto loadProjectManagementEntryDto(ProjectEmployees currentProject, Map<String,
            Employee> employees, YearMonth payrollMonth, boolean projectStateLogicSingle) {

        List<ManagementEntryDto> entries = createManagementEntriesForProject(currentProject, employees, payrollMonth, projectStateLogicSingle);
        List<ProjectEntry> projectEntries = projectEntryService.findByNameAndDate(currentProject.getProjectId(), payrollMonth);

        if (!entries.isEmpty() && !projectEntries.isEmpty()) {

            Duration billable = calculateProjectDuration(entries.stream()
                    .map(ManagementEntryDto::getBillableTime)
                    .toList());

            Duration nonBillable = calculateProjectDuration(entries.stream()
                    .map(ManagementEntryDto::getNonBillableTime)
                    .toList());

            // it is guaranteed that the same Project instance is obtained for every ProjectEntry
            Integer zepId = Optional.ofNullable(projectEntries.get(0))
                    .map(ProjectEntry::getProject)
                    .map(com.gepardec.mega.db.entity.project.Project::getZepId)
                    .orElse(null);

            return ProjectManagementEntryDto.builder()
                    .zepId(zepId)
                    .projectName(currentProject.getProjectId())
                    .controlProjectState(
                            ProjectState.byName(
                                    getProjectEntryForProjectStep(
                                            projectEntries,
                                            ProjectStep.CONTROL_PROJECT).getState().name()
                            )
                    )
                    .controlBillingState(
                            ProjectState.byName(
                                    getProjectEntryForProjectStep(
                                            projectEntries,
                                            ProjectStep.CONTROL_BILLING).getState().name()
                            )
                    )
                    .presetControlProjectState(
                            getProjectEntryForProjectStep(projectEntries, ProjectStep.CONTROL_PROJECT).isPreset()
                    )
                    .presetControlBillingState(
                            getProjectEntryForProjectStep(projectEntries, ProjectStep.CONTROL_BILLING).isPreset()
                    )
                    .entries(entries)
                    .aggregatedBillableWorkTimeInSeconds(billable)
                    .aggregatedNonBillableWorkTimeInSeconds(nonBillable)
                    .build();
        } else {
            return null;
        }
    }

    private void validateUserContext() {
        if (userContext == null || userContext.getUser() == null) {
            throw new IllegalStateException("User context does not exist or user is null.");
        }
    }

    @Override
    public Response getProjectsWithoutLeads() {
        validateUserContext();

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

    private Duration calculateProjectDuration(List<String> entries) {
        return Duration.ofMinutes(
                Optional.ofNullable(entries)
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .filter(Objects::nonNull)
                        .map(billableTime -> {
                            long hours = Long.parseLong(billableTime.split(":")[0]);
                            long minutes = Long.parseLong(billableTime.split(":")[1]);
                            return hours * 60 + minutes;
                        })
                        .reduce(0L, Long::sum)
        );
    }

    private ProjectEntry getProjectEntryForProjectStep(List<ProjectEntry> projectEntries, ProjectStep projectStep) {
        return projectEntries.stream()
                .filter(p -> p.getStep() == projectStep)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("No project entry found for project step '%s'", projectStep)));
    }

    private Map<String, Employee> createEmployeeCache(YearMonth selectedYearMonth) {
        return employeeService.getAllEmployeesConsideringExitDate(selectedYearMonth).stream()
                .collect(Collectors.toMap(Employee::getUserId, employee -> employee));
    }

    private List<ManagementEntryDto> createManagementEntriesForProject(ProjectEmployees projectEmployees, Map<String, Employee> employees, YearMonth payrollMonth, boolean projectStateLogicSingle) {
        validateUserContext();

        List<ManagementEntryDto> entries = new ArrayList<>();

        for (String userId : projectEmployees.getEmployees()) {
            if (employees.containsKey(userId)) {
                Employee employee = employees.get(userId);
                List<StepEntry> stepEntries = stepEntryService.findAllStepEntriesForEmployeeAndProject(
                        employee,
                        projectEmployees.getProjectId(),
                        Objects.requireNonNull(userContext.getUser()).getEmail(),
                        payrollMonth
                );

                ManagementEntryDto entry = createManagementEntryForEmployee(employee, projectEmployees.getProjectId(), stepEntries, payrollMonth, null, projectStateLogicSingle);

                if (entry != null) {
                    entries.add(entry);
                }
            }
        }

        return entries;
    }

    private ManagementEntryDto createManagementEntryForEmployee(Employee employee, List<StepEntry> stepEntries, YearMonth payrollMonth, List<PmProgressDto> pmProgressDtos, boolean projectStateLogicSingle) {
        return createManagementEntryForEmployee(employee, null, stepEntries, payrollMonth, pmProgressDtos, projectStateLogicSingle);
    }

    private ManagementEntryDto createManagementEntryForEmployee(Employee employee, String projectId, List<StepEntry> stepEntries, YearMonth payrollMonth, List<PmProgressDto> pmProgressDtos, boolean projectStateLogicSingle) {
        FinishedAndTotalComments finishedAndTotalComments = commentService.countFinishedAndTotalComments(employee.getEmail(), payrollMonth);

        List<ProjectTime> projectTime = zepService.getProjectTimesForEmployeePerProject(projectId, payrollMonth);

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

            // used later on to compute the percentage of hours which were spent in this project (both billable and non-billable)
            List<com.gepardec.mega.domain.model.monthlyreport.ProjectEntry> projectEntriesForEmployee = zepRestService.getProjectTimes(employee, payrollMonth);
            long totalWorkingHoursInMinutesForMonthAndEmployee = workingTimeUtil.getDurationFromTimeString(workingTimeUtil.getTotalWorkingTimeForEmployee(projectEntriesForEmployee, employee)).toMinutes();

            String billableTimeString = workingTimeUtil.getBillableTimesForEmployee(projectTime, employee);
            String nonBillableTimeString = workingTimeUtil.getInternalTimesForEmployee(projectTime, employee);
            long billableTimeInMinutes = workingTimeUtil.getDurationFromTimeString(billableTimeString).toMinutes();
            long nonBillableTimeInMinutes = workingTimeUtil.getDurationFromTimeString(nonBillableTimeString).toMinutes();

            double percentageOfHoursSpentInThisProject = 0.0;
            if (!(Double.compare(totalWorkingHoursInMinutesForMonthAndEmployee, 0.0d) == 0)) {
                percentageOfHoursSpentInThisProject = (double) (billableTimeInMinutes + nonBillableTimeInMinutes) / totalWorkingHoursInMinutesForMonthAndEmployee;
                percentageOfHoursSpentInThisProject =
                        BigDecimal.valueOf(percentageOfHoursSpentInThisProject)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue() * 100;
            }

            return ManagementEntryDto.builder()
                    .employee(employeeMapper.mapToDto(employee))
                    .employeeCheckState(employeeCheckState)
                    .employeeCheckStateReason(employeeCheckStateReason)
                    .internalCheckState(extractInternalCheckState(stepEntries))
                    .projectCheckState(extractStateForProject(stepEntries, projectId, projectStateLogicSingle))
                    .employeeProgresses(pmProgressDtos)
                    .finishedComments(finishedAndTotalComments.getFinishedComments())
                    .totalComments(finishedAndTotalComments.getTotalComments())
                    .entryDate(stepEntries.get(0).getDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)))
                    .billableTime(billableTimeString)
                    .nonBillableTime(nonBillableTimeString)
                    .percentageOfHoursSpentInThisProject(percentageOfHoursSpentInThisProject)
                    .build();
        }

        return null;
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
        switch (employeeState) {
            case DONE:
                return State.DONE;
            case OPEN:
                return State.OPEN;
            case IN_PROGRESS:
                return State.IN_PROGRESS;
            default:
                return null;
        }
    }

    private com.gepardec.mega.domain.model.State extractInternalCheckState(List<StepEntry> stepEntries) {
        if (userContext == null || userContext.getUser() == null) {
            throw new IllegalStateException("User context does not exist.");
        }

        boolean internalCheckStateOpen = stepEntries.stream()
                .filter(stepEntry ->
                        StepName.CONTROL_INTERNAL_TIMES.name().equalsIgnoreCase(stepEntry.getStep().getName())
                                && StringUtils.equalsIgnoreCase(
                                Objects.requireNonNull(userContext.getUser()).getEmail(),
                                stepEntry.getAssignee().getEmail()
                        )
                ).anyMatch(stepEntry -> EmployeeState.OPEN.equals(stepEntry.getState()));

        return internalCheckStateOpen ? com.gepardec.mega.domain.model.State.OPEN : com.gepardec.mega.domain.model.State.DONE;
    }

    private com.gepardec.mega.domain.model.State extractStateForProject(List<StepEntry> stepEntries, String projectId, boolean projectStateLogicSingle) {
        validateUserContext();

        List<EmployeeState> collectedStates = stepEntries
                .stream()
                .filter(se -> {
                    if (StringUtils.isBlank(projectId)) {
                        return StepName.CONTROL_TIME_EVIDENCES.name().equalsIgnoreCase(se.getStep().getName());
                    } else {
                        return StepName.CONTROL_TIME_EVIDENCES.name().equalsIgnoreCase(se.getStep().getName()) &&
                                StringUtils.equalsIgnoreCase(se.getProject(), projectId);
                    }
                })
                .map(StepEntry::getState)
                .toList();

        if (projectStateLogicSingle) {
            return collectedStates.stream()
                    .anyMatch(state -> state.equals(EmployeeState.OPEN)) ? com.gepardec.mega.domain.model.State.OPEN : com.gepardec.mega.domain.model.State.DONE;
        } else {
            if (collectedStates.isEmpty()) {
                return State.DONE;
            }
            return collectedStates.stream()
                    .anyMatch(state -> state.equals(EmployeeState.DONE)) ? com.gepardec.mega.domain.model.State.DONE : com.gepardec.mega.domain.model.State.OPEN;
        }

    }
}

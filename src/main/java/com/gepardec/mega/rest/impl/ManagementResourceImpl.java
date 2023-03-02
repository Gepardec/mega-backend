package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.RolesAllowed;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.db.entity.project.ProjectEntry;
import com.gepardec.mega.db.entity.project.ProjectStep;
import com.gepardec.mega.domain.model.*;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.rest.api.ManagementResource;
import com.gepardec.mega.rest.model.*;
import com.gepardec.mega.service.api.*;
import com.gepardec.mega.zep.ZepService;
import de.provantis.zep.ProjektzeitType;
import io.quarkus.security.Authenticated;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestScoped
@Authenticated
@RolesAllowed(value = {Role.PROJECT_LEAD, Role.OFFICE_MANAGEMENT})
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

    @Override
    public Response getAllOfficeManagementEntries(Integer year, Integer month, boolean projectStateLogicSingle) {
        LocalDate from = DateUtils.getFirstDayOfMonth(year, month);
        LocalDate to = DateUtils.getLastDayOfMonth(year, month);

        List<ManagementEntryDto> officeManagementEntries = new ArrayList<>();
        List<Employee> activeEmployees = employeeService.getAllActiveEmployees();

        for (Employee employee : activeEmployees) {
            List<StepEntry> stepEntries = stepEntryService.findAllStepEntriesForEmployee(employee, from, to);

            String entryDate = DateUtils.getFirstDayOfMonth(year, month).minusMonths(1).format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN));

            List<StepEntry> allOwnedStepEntriesForPMProgress = stepEntryService.findAllOwnedAndUnassignedStepEntriesForPMProgress(employee.getEmail(), entryDate);
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

            ManagementEntryDto newManagementEntryDto = createManagementEntryForEmployee(employee, stepEntries, from, to, pmProgressDtos, projectStateLogicSingle);

            if (newManagementEntryDto != null) {
                officeManagementEntries.add(newManagementEntryDto);
            }

        }

        return Response.ok(officeManagementEntries).build();
    }

    @Override
    public Response getAllProjectManagementEntries(Integer year, Integer month, boolean allProjects, boolean projectStateLogicSingle) {
        validateUserContext();

        LocalDate from = DateUtils.getFirstDayOfMonth(year, month);
        LocalDate to = DateUtils.getLastDayOfMonth(year, month);

        List<ProjectEmployees> projectEmployees;

        if (allProjects) {
            projectEmployees = stepEntryService.getAllProjectEmployeesForPM(from, to);
        } else {
            projectEmployees = stepEntryService.getProjectEmployeesForPM(from, to, Objects.requireNonNull(userContext.getUser()).getEmail());
        }

        List<ProjectManagementEntryDto> projectManagementEntries = new ArrayList<>();

        Map<String, Employee> employees = createEmployeeCache();

        for (ProjectEmployees currentProject : projectEmployees) {
            ProjectManagementEntryDto projectManagementEntryDto = loadProjectManagementEntryDto(currentProject, employees,
                    from, to, projectStateLogicSingle);

            if(projectManagementEntryDto != null) {
                projectManagementEntries.add(projectManagementEntryDto);
            }
        }

        return Response.ok(projectManagementEntries).build();
    }

    private ProjectManagementEntryDto loadProjectManagementEntryDto(ProjectEmployees currentProject, Map<String,
            Employee> employees, LocalDate from, LocalDate to, boolean projectStateLogicSingle) {

        List<ManagementEntryDto> entries = createManagementEntriesForProject(currentProject, employees, from, to, projectStateLogicSingle);
        List<ProjectEntry> projectEntries = projectEntryService.findByNameAndDate(currentProject.getProjectId(), from, to);

        if (!entries.isEmpty() && !projectEntries.isEmpty()) {

            Duration billable = calculateProjectDuration(entries.stream()
                    .map(ManagementEntryDto::billableTime)
                    .collect(Collectors.toList()));

            Duration nonBillable = calculateProjectDuration(entries.stream()
                    .map(ManagementEntryDto::nonBillableTime)
                    .collect(Collectors.toList()));

            return ProjectManagementEntryDto.builder()
                    .projectName(currentProject.getProjectId())
                    .controlProjectState(ProjectState.byName(getProjectEntryForProjectStep(projectEntries, ProjectStep.CONTROL_PROJECT).getState().name()))
                    .controlBillingState(ProjectState.byName((getProjectEntryForProjectStep(projectEntries, ProjectStep.CONTROL_BILLING).getState().name())))
                    .presetControlProjectState(getProjectEntryForProjectStep(projectEntries, ProjectStep.CONTROL_PROJECT).isPreset())
                    .presetControlBillingState(getProjectEntryForProjectStep(projectEntries, ProjectStep.CONTROL_BILLING).isPreset())
                    .entries(entries)
                    .aggregatedBillableWorkTimeInSeconds(billable)
                    .aggregatedNonBillableWorkTimeInSeconds(nonBillable)
                    .build();
        }
        else {
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

        LocalDate firstDayOfMonth = DateUtils.getFirstDayOfCurrentMonth();
        List<Project> customerProjectsWithoutLeads = projectService.getProjectsForMonthYear(firstDayOfMonth,
                List.of(ProjectFilter.IS_CUSTOMER_PROJECT, ProjectFilter.WITHOUT_LEADS));

        List<CustomerProjectWithoutLeadsDto> customerProjectsWithoutLeadsDto = customerProjectsWithoutLeads.stream()
                .map(project -> CustomerProjectWithoutLeadsDto.builder()
                        .projectName(project.getProjectId())
                        .fetchDate(firstDayOfMonth)
                        .comment("Dies Projekt hat keinen Projektleiter zugewiesen. Bitte hinzufügen!")
                        .build())
                .collect(Collectors.toList());

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

    private Map<String, Employee> createEmployeeCache() {
        return employeeService.getAllActiveEmployees().stream()
                .collect(Collectors.toMap(Employee::getUserId, employee -> employee));
    }

    private List<ManagementEntryDto> createManagementEntriesForProject(ProjectEmployees projectEmployees, Map<String, Employee> employees, LocalDate from, LocalDate to, boolean projectStateLogicSingle) {
        validateUserContext();

        List<ManagementEntryDto> entries = new ArrayList<>();

        for (String userId : projectEmployees.getEmployees()) {
            if (employees.containsKey(userId)) {
                Employee employee = employees.get(userId);
                List<StepEntry> stepEntries = stepEntryService.findAllStepEntriesForEmployeeAndProject(
                        employee, projectEmployees.getProjectId(), Objects.requireNonNull(userContext.getUser()).getEmail(), from, to
                );

                ManagementEntryDto entry = createManagementEntryForEmployee(employee, projectEmployees.getProjectId(), stepEntries, from, to, null, projectStateLogicSingle);

                if (entry != null) {
                    entries.add(entry);
                }
            }
        }

        return entries;
    }

    private ManagementEntryDto createManagementEntryForEmployee(Employee employee, List<StepEntry> stepEntries, LocalDate from, LocalDate to, List<PmProgressDto> pmProgressDtos, boolean projectStateLogicSingle) {
        return createManagementEntryForEmployee(employee, null, stepEntries, from, to, pmProgressDtos, projectStateLogicSingle);
    }

    private ManagementEntryDto createManagementEntryForEmployee(Employee employee, String projectId, List<StepEntry> stepEntries, LocalDate from, LocalDate to, List<PmProgressDto> pmProgressDtos, boolean projectStateLogicSingle) {
        FinishedAndTotalComments finishedAndTotalComments = commentService.cntFinishedAndTotalCommentsForEmployee(employee, from, to);

        List<ProjektzeitType> projektzeitTypes = zepService.getProjectTimesForEmployeePerProject(projectId, from);

        if (!stepEntries.isEmpty()) {
            return ManagementEntryDto.builder()
                    .employee(employee)
                    .employeeCheckState(extractEmployeeCheckState(stepEntries))
                    .internalCheckState(extractInternalCheckState(stepEntries))
                    .projectCheckState(extractStateForProject(stepEntries, projectId, projectStateLogicSingle))
                    .employeeProgresses(pmProgressDtos)
                    .finishedComments(finishedAndTotalComments.getFinishedComments())
                    .totalComments(finishedAndTotalComments.getTotalComments())
                    .entryDate(stepEntries.get(0).getDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)))
                    .billableTime(zepService.getBillableTimesForEmployee(projektzeitTypes, employee))
                    .nonBillableTime(zepService.getInternalTimesForEmployee(projektzeitTypes, employee))
                    .build();
        }

        return null;
    }

    private com.gepardec.mega.domain.model.State extractEmployeeCheckState(List<StepEntry> stepEntries) {
        boolean employeeCheckStateOpen = stepEntries.stream()
                .filter(stepEntry -> StepName.CONTROL_TIMES.name().equalsIgnoreCase(stepEntry.getStep().getName()))
                .anyMatch(stepEntry -> EmployeeState.OPEN.equals(stepEntry.getState()));

        return employeeCheckStateOpen ? com.gepardec.mega.domain.model.State.OPEN : com.gepardec.mega.domain.model.State.DONE;
    }

    private com.gepardec.mega.domain.model.State extractInternalCheckState(List<StepEntry> stepEntries) {
        if (userContext == null || userContext.getUser() == null) {
            throw new IllegalStateException("User context does not exist.");
        }

        boolean internalCheckStateOpen = stepEntries.stream()
                .filter(stepEntry ->
                        StepName.CONTROL_INTERNAL_TIMES.name().equalsIgnoreCase(stepEntry.getStep().getName())
                                && StringUtils.equalsIgnoreCase(Objects.requireNonNull(userContext.getUser()).getEmail(), stepEntry.getAssignee().getEmail())
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
                .collect(Collectors.toList());

        if (projectStateLogicSingle) {
            return collectedStates.stream()
                    .anyMatch(state -> state.equals(EmployeeState.OPEN)) ? com.gepardec.mega.domain.model.State.OPEN : com.gepardec.mega.domain.model.State.DONE;
        } else {
            if(collectedStates.isEmpty()) {
                return State.DONE;
            }
            return collectedStates.stream()
                    .anyMatch(state -> state.equals(EmployeeState.DONE)) ? com.gepardec.mega.domain.model.State.DONE : com.gepardec.mega.domain.model.State.OPEN;
        }

    }
}

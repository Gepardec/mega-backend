package com.gepardec.mega.service.impl.init;

import com.gepardec.mega.application.configuration.NotificationConfig;
import com.gepardec.mega.db.entity.project.ProjectEntry;
import com.gepardec.mega.db.entity.project.ProjectState;
import com.gepardec.mega.db.entity.project.ProjectStep;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectFilter;
import com.gepardec.mega.domain.model.State;
import com.gepardec.mega.domain.model.Step;
import com.gepardec.mega.domain.model.StepEntry;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.service.api.init.StepEntrySyncService;
import com.gepardec.mega.service.api.project.ProjectService;
import com.gepardec.mega.service.api.projectentry.ProjectEntryService;
import com.gepardec.mega.service.api.step.StepService;
import com.gepardec.mega.service.api.stepentry.StepEntryService;
import com.gepardec.mega.service.api.user.UserService;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Dependent
@Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
public class StepEntrySyncServiceImpl implements StepEntrySyncService {

    @Inject
    Logger logger;

    @Inject
    UserService userService;

    @Inject
    ProjectService projectService;

    @Inject
    StepService stepService;

    @Inject
    StepEntryService stepEntryService;

    @Inject
    NotificationConfig notificationConfig;

    @Inject
    ProjectEntryService projectEntryService;

    @Override
    public boolean generateProjects() {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        logger.info("Started project generation: {}", Instant.ofEpochMilli(stopWatch.getStartTime()));

        LocalDate date = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        logger.info("Processing date: {}", date);

        List<User> activeUsers = userService.findActiveUsers();
        List<Project> projectsForMonthYear = projectService.getProjectsForMonthYear(date,
                List.of(ProjectFilter.IS_LEADS_AVAILABLE,
                        ProjectFilter.IS_CUSTOMER_PROJECT));

        logger.info("Loaded projects: {}", projectsForMonthYear.size());
        logger.debug("projects are {}", projectsForMonthYear);
        logger.info("Loaded users: {}", activeUsers.size());
        logger.debug("Users are: {}", activeUsers);

        createProjects(activeUsers, projectsForMonthYear, date)
                .forEach(projectService::addProject);

        List<Project> projects = projectService.getProjectsForMonthYear(date);

        stopWatch.stop();

        logger.debug("projects in db are {}", projects);

        logger.info("Project generation took: {}ms", stopWatch.getTime());
        logger.info("Finished project generation: {}", Instant.ofEpochMilli(stopWatch.getStartTime() + stopWatch.getTime()));

        return !projects.isEmpty();
    }

    private List<com.gepardec.mega.db.entity.project.Project> createProjects(List<User> activeUsers, List<Project> projects, LocalDate date) {
        return projects.stream()
                .map(project -> createProjectEntityFromProject(activeUsers, project, date))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<com.gepardec.mega.db.entity.project.Project> createProjectEntityFromProject(List<User> activeUsers, Project project, LocalDate date) {
        com.gepardec.mega.db.entity.project.Project projectEntity = new com.gepardec.mega.db.entity.project.Project();

        List<User> leads = project.leads()
                .stream()
                .filter(Objects::nonNull)
                .filter(userid -> !userid.isBlank())
                .map(userid -> findUserByUserId(activeUsers, userid))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if(leads.isEmpty()){
            return Optional.empty();
        }

        Set<com.gepardec.mega.db.entity.User> mappedLeads = leads.stream()
                .map(this::mapDomainUserToEntity)
                .collect(Collectors.toSet());

        projectEntity.setProjectLeads(mappedLeads);
        projectEntity.setName(project.projectId());
        projectEntity.setStartDate(project.startDate());
        projectEntity.setEndDate(project.endDate());

        projectEntity.setItems(new HashSet<>());

        Arrays.stream(ProjectStep.values()).forEach(projectStep ->
                projectEntity.getItems().add(createProjectEntry(projectEntity, mappedLeads, date, projectStep))
        );

        return Optional.of(projectEntity);
    }

    @Override
    public void genereteStepEntries() {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        logger.info("Started step entry generation: {}", Instant.ofEpochMilli(stopWatch.getStartTime()));

        final LocalDate date = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        logger.info("Processing date: {}", date);

        final List<User> activeUsers = userService.findActiveUsers();
        final List<Project> projectsForMonthYear = projectService.getProjectsForMonthYear(date,
                List.of(ProjectFilter.IS_LEADS_AVAILABLE,
                        ProjectFilter.IS_CUSTOMER_PROJECT));
        final List<Step> steps = stepService.getSteps();

        final List<User> omUsers = notificationConfig.getOmMailAddresses()
                .stream()
                .map(email -> findUserByEmail(activeUsers, email))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        logger.info("Loaded projects: {}", projectsForMonthYear.size());
        logger.debug("projects are {}", projectsForMonthYear);
        logger.info("Loaded users: {}", activeUsers.size());
        logger.debug("Users are: {}", activeUsers);
        logger.info("Loaded steps: {}", steps.size());
        logger.debug("Steps are: {}", steps);
        logger.info("Loaded omUsers: {}", omUsers.size());
        logger.debug("omUsers are: {}", omUsers);

        // TODO: process newly fetched data (projectsForMonth contains data for project table)
        // TODO: generate projectEntries
        final List<StepEntry> toBeCreatedStepEntries = new ArrayList<>();
        final List<ProjectEntry> toBeCreatedProjectEntries = new ArrayList<>();

        for (Step step : steps) {
            switch (step.role()) {
                case EMPLOYEE:
                    toBeCreatedStepEntries.addAll(createStepEntriesForUsers(date, step, activeUsers));
                    break;
                case OFFICE_MANAGEMENT:
                    toBeCreatedStepEntries.addAll(createStepEntriesOmForUsers(date, step, omUsers, activeUsers));
                    break;
                case PROJECT_LEAD:
                    toBeCreatedStepEntries.addAll(createStepEntriesProjectLeadForUsers(date, step, projectsForMonthYear, activeUsers));
                    // TODO: here
                    toBeCreatedProjectEntries.addAll(createProjectStepEntriesForProjects(date, step, projectsForMonthYear));
                    break;
                default:
                    throw new IllegalArgumentException("no logic implemented for provided role");
            }
        }

        toBeCreatedStepEntries.forEach(stepEntryService::addStepEntry);

        stopWatch.stop();

        logger.info("Processed step entries: {}", toBeCreatedStepEntries.size());
        logger.info("Step entry generation took: {}ms", stopWatch.getTime());
        logger.info("Finished step entry generation: {}", Instant.ofEpochMilli(stopWatch.getStartTime() + stopWatch.getTime()));
    }

    private List<StepEntry> createStepEntriesProjectLeadForUsers(final LocalDate date, final Step step, final List<Project> projects, final List<User> users) {
        return users.stream()
                .map(owner -> createStepEntriesForOwnerProjects(date, step, projects, users, owner))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<StepEntry> createStepEntriesForOwnerProjects(final LocalDate date, final Step step, final List<Project> projects, final List<User> users, final User ownerUser) {
        return projects.stream()
                .filter(project -> project.employees().contains(ownerUser.userId()))
                .map(project -> createStepEntriesForOwnerProject(date, step, project, users, ownerUser))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<StepEntry> createStepEntriesForOwnerProject(final LocalDate date, final Step step, final Project project, final List<User> users, final User ownerUser) {
        return project.leads()
                .stream()
                .map(lead -> findUserByUserId(users, lead))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(leadUser -> StepEntry.builder()
                        .date(date)
                        .project(project)
                        .state(State.OPEN)
                        .owner(ownerUser)
                        .assignee(leadUser)
                        .step(step)
                        .build())
                .collect(Collectors.toList());
    }

    private List<ProjectEntry> createProjectStepEntriesForProjects(final LocalDate date, final Step step, final List<Project> projects) {
        // TODO: logic for generating step entries goes here
        // Therefore a AutoValue class is required in order to construct the default ProjectEntries
        return null;
//        projects.stream()
//                .map(project -> com.gepardec.mega.domain.model.ProjectEntry.builder())
//                .collect(Collectors.toList());
    }

    private List<StepEntry> createStepEntriesOmForUsers(final LocalDate date, final Step step, final List<User> omUsers, final List<User> users) {
        return users.stream()
                .map(ownerUser -> createStepEntriesForOwnerOmUsers(date, step, omUsers, ownerUser))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<StepEntry> createStepEntriesForOwnerOmUsers(final LocalDate date, final Step step, final List<User> omUsers, final User ownerUser) {
        return omUsers.stream()
                .map(omUser -> StepEntry.builder()
                        .date(date)
                        .project(null)
                        .state(State.OPEN)
                        .owner(ownerUser)
                        .assignee(omUser)
                        .step(step)
                        .build())
                .collect(Collectors.toList());
    }

    private List<StepEntry> createStepEntriesForUsers(final LocalDate date, final Step step, final List<User> users) {
        return users.stream()
                .map(ownerUser -> StepEntry.builder()
                        .date(date)
                        .project(null)
                        .state(State.OPEN)
                        .owner(ownerUser)
                        .assignee(ownerUser)
                        .step(step)
                        .build())
                .collect(Collectors.toList());
    }

    private Optional<User> findUserByUserId(final List<User> users, final String userId) {
        return users.stream().filter(user -> user.userId().equals(userId)).findFirst();
    }

    private Optional<User> findUserByEmail(final List<User> users, final String email) {
        return users.stream().filter(user -> user.email().equals(email)).findFirst();
    }

    private com.gepardec.mega.db.entity.User mapDomainUserToEntity(User user) {
        com.gepardec.mega.db.entity.User u = new com.gepardec.mega.db.entity.User();
        u.setId(user.dbId());
        return u;
    }

    private ProjectEntry createProjectEntry(com.gepardec.mega.db.entity.project.Project project,
                                            Set<com.gepardec.mega.db.entity.User> leads,
                                            LocalDate date, ProjectStep step) {
        ProjectEntry projectEntry = new ProjectEntry();
        projectEntry.setProject(project);
        projectEntry.setName(project.getName());
        projectEntry.setOwner(
                leads.stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Project without project lead found."))
        );
        projectEntry.setAssignee(
                leads.stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Project without project lead found."))
        );
        projectEntry.setDate(date);
        projectEntry.setCreationDate(LocalDateTime.now());
        projectEntry.setUpdatedDate(LocalDateTime.now());
        projectEntry.setState(ProjectState.OPEN);
        projectEntry.setStep(step);

        LocalDate from = date.minusMonths(1).withDayOfMonth(1);
        LocalDate to = date.minusMonths(1).withDayOfMonth(date.minusMonths(1).lengthOfMonth());
        Optional<ProjectEntry> projectEntryValue = projectEntryService.findByNameAndDate(project.getName(), from, to)
                .stream()
                .filter(Objects::nonNull)
                .filter(pe -> pe.getStep().getId() == step.getId())
                .findFirst();

        projectEntryValue.ifPresentOrElse(
                p -> projectEntry.setPreset(p.isPreset()),
                () -> projectEntry.setPreset(false)
        );

        return projectEntry;
    }
}

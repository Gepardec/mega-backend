package com.gepardec.mega.service.impl;

import com.gepardec.mega.application.configuration.NotificationConfig;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectFilter;
import com.gepardec.mega.domain.model.State;
import com.gepardec.mega.domain.model.Step;
import com.gepardec.mega.domain.model.StepEntry;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.service.api.ProjectService;
import com.gepardec.mega.service.api.StepEntryService;
import com.gepardec.mega.service.api.StepEntrySyncService;
import com.gepardec.mega.service.api.StepService;
import com.gepardec.mega.service.api.UserService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    @Override
    public boolean generateStepEntries(LocalDate date) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        logger.info("Started step entry generation: {}", Instant.ofEpochMilli(stopWatch.getStartTime()));

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
                .toList();

        logger.info("Loaded projects: {}", projectsForMonthYear.size());
        logger.debug("projects are {}", projectsForMonthYear);
        logger.info("Loaded users: {}", activeUsers.size());
        logger.debug("Users are: {}", activeUsers);
        logger.info("Loaded steps: {}", steps.size());
        logger.debug("Steps are: {}", steps);
        logger.info("Loaded omUsers: {}", omUsers.size());
        logger.debug("omUsers are: {}", omUsers);

        final List<StepEntry> toBeCreatedStepEntries = new ArrayList<>();

        for (Step step : steps) {
            switch (step.getRole()) {
                case EMPLOYEE:
                    toBeCreatedStepEntries.addAll(createStepEntriesForUsers(date, step, activeUsers));
                    break;
                case OFFICE_MANAGEMENT:
                    toBeCreatedStepEntries.addAll(createStepEntriesOmForUsers(date, step, omUsers, activeUsers));
                    break;
                case PROJECT_LEAD:
                    toBeCreatedStepEntries.addAll(createStepEntriesProjectLeadForUsers(date, step, projectsForMonthYear, activeUsers));
                    break;
                default:
                    throw new IllegalArgumentException("no logic implemented for provided role");
            }
        }

        final List<com.gepardec.mega.db.entity.employee.StepEntry> allEntityStepEntries = stepEntryService.findAll();
        List<StepEntry> toBeCreatedFilteredStepEntries = toBeCreatedStepEntries;

        if (!allEntityStepEntries.isEmpty()) {
            toBeCreatedFilteredStepEntries = toBeCreatedStepEntries.stream()
                    .filter(stepEntry -> allEntityStepEntries.stream()
                            .noneMatch(stepEntry1 -> modelEqualsEntityStepEntry(stepEntry, stepEntry1)))
                    .toList();
        }


        toBeCreatedFilteredStepEntries.forEach(stepEntryService::addStepEntry);

        stopWatch.stop();

        logger.info("Processed step entries: {}", toBeCreatedStepEntries.size());
        logger.info("Step entry generation took: {}ms", stopWatch.getTime());
        logger.info("Finished step entry generation: {}", Instant.ofEpochMilli(stopWatch.getStartTime() + stopWatch.getTime()));

        return true;
    }

    private boolean modelEqualsEntityStepEntry(StepEntry model, com.gepardec.mega.db.entity.employee.StepEntry entity) {
        if (entity.getDate().equals(model.getDate()) &&
                entity.getAssignee().getEmail().equals(model.getAssignee().getEmail()) &&
                entity.getOwner().getEmail().equals(model.getOwner().getEmail())) {
            return true;
        }
        return false;
    }

    private List<StepEntry> createStepEntriesProjectLeadForUsers(final LocalDate date, final Step step, final List<Project> projects, final List<User> users) {
        return users.stream()
                .map(owner -> createStepEntriesForOwnerProjects(date, step, projects, users, owner))
                .flatMap(Collection::stream)
                .toList();
    }

    private List<StepEntry> createStepEntriesForOwnerProjects(final LocalDate date, final Step step, final List<Project> projects, final List<User> users, final User ownerUser) {
        return projects.stream()
                .filter(project -> project.getEmployees().contains(ownerUser.getUserId()))
                .map(project -> createStepEntriesForOwnerProject(date, step, project, users, ownerUser))
                .flatMap(Collection::stream)
                .toList();
    }

    private List<StepEntry> createStepEntriesForOwnerProject(final LocalDate date, final Step step, final Project project, final List<User> users, final User ownerUser) {
        return project.getLeads()
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
                .toList();
    }

    private List<StepEntry> createStepEntriesOmForUsers(final LocalDate date, final Step step, final List<User> omUsers, final List<User> users) {
        return users.stream()
                .map(ownerUser -> createStepEntriesForOwnerOmUsers(date, step, omUsers, ownerUser))
                .flatMap(Collection::stream)
                .toList();
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
                .toList();
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
                .toList();
    }

    private Optional<User> findUserByUserId(final List<User> users, final String userId) {
        return users.stream().filter(user -> user.getUserId().equals(userId)).findFirst();
    }

    private Optional<User> findUserByEmail(final List<User> users, final String email) {
        return users.stream().filter(user -> user.getEmail().equals(email)).findFirst();
    }
}

package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.common.State;
import com.gepardec.mega.db.entity.employee.UserEntity;
import com.gepardec.mega.db.entity.project.ProjectEntity;
import com.gepardec.mega.db.entity.project.ProjectEntryEntity;
import com.gepardec.mega.db.entity.project.ProjectStep;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectFilter;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.service.api.ProjectEntryService;
import com.gepardec.mega.service.api.ProjectService;
import com.gepardec.mega.service.api.ProjectSyncService;
import com.gepardec.mega.service.api.UserService;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Dependent
@Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
public class ProjectSyncServiceImpl implements ProjectSyncService {

    @Inject
    Logger logger;

    @Inject
    UserService userService;

    @Inject
    ProjectService projectService;

    @Inject
    ProjectEntryService projectEntryService;

    @Override
    public boolean generateProjects(YearMonth payrollMonth) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        logger.info("Started project generation: {}", stopWatch.getStartInstant());
        logger.info("Processing date: {}", payrollMonth);

        List<User> activeUsers = userService.findActiveUsers();
        List<Project> projectsForMonthYear = projectService.getProjectsForMonthYear(payrollMonth, List.of(ProjectFilter.IS_LEADS_AVAILABLE));

        logger.info("Loaded projects: {}", projectsForMonthYear.size());
        logger.debug("projects are {}", projectsForMonthYear);
        logger.info("Loaded users: {}", activeUsers.size());
        logger.debug("Users are: {}", activeUsers);

        createProjects(activeUsers, projectsForMonthYear, payrollMonth)
                .forEach(project -> projectService.addProject(project, payrollMonth));

        List<Project> projects = projectService.getProjectsForMonthYear(payrollMonth);

        stopWatch.stop();

        logger.debug("projects in db are {}", projects);

        logger.info("Project generation took: {}ms", stopWatch.getTime());
        logger.info("Finished project generation: {}", stopWatch.getStopInstant());

        return !projects.isEmpty();
    }

    private List<ProjectEntity> createProjects(List<User> activeUsers, List<Project> projects, YearMonth payrollMonth) {
        return projects.stream()
                .map(project -> createProjectEntityFromProject(activeUsers, project, payrollMonth))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ProjectEntity> createProjectEntityFromProject(List<User> activeUsers, Project project, YearMonth payrollMonth) {
        ProjectEntity projectEntity = new ProjectEntity();

        List<User> leads = project.getLeads()
                .stream()
                .filter(Objects::nonNull)
                .filter(userid -> !userid.isBlank())
                .map(userid -> findUserByUserId(activeUsers, userid))
                .flatMap(Optional::stream)
                .toList();

        if (leads.isEmpty()) {
            return Optional.empty();
        }

        Set<UserEntity> mappedLeads = leads.stream()
                .map(this::mapDomainUserToEntity)
                .collect(Collectors.toSet());

        projectEntity.setProjectLeads(mappedLeads);
        projectEntity.setZepId(project.getZepId());
        projectEntity.setName(project.getProjectId());
        projectEntity.setStartDate(project.getStartDate());
        projectEntity.setEndDate(project.getEndDate());

        Arrays.stream(ProjectStep.values()).forEach(projectStep ->
                projectEntity.addProjectEntry(createProjectEntry(projectEntity, mappedLeads, payrollMonth, projectStep))
        );

        return Optional.of(projectEntity);
    }

    private Optional<User> findUserByUserId(final List<User> users, final String userId) {
        return users.stream().filter(user -> user.getUserId().equals(userId)).findFirst();
    }

    private UserEntity mapDomainUserToEntity(User user) {
        UserEntity u = new UserEntity();
        u.setId(user.getDbId());
        return u;
    }

    private ProjectEntryEntity createProjectEntry(ProjectEntity project,
                                                  Set<UserEntity> leads,
                                                  YearMonth payrollMonth, ProjectStep step) {
        ProjectEntryEntity projectEntry = new ProjectEntryEntity();
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
        projectEntry.setDate(payrollMonth.atDay(1));
        // TODO: Make sure that creation- and updateDate and state are not set in code, but only in the entity class
        projectEntry.setCreationDate(LocalDateTime.now());
        projectEntry.setUpdatedDate(LocalDateTime.now());
        projectEntry.setState(State.OPEN);
        projectEntry.setStep(step);

        Optional<ProjectEntryEntity> projectEntryValue = projectEntryService.findByNameAndDate(project.getName(), payrollMonth.minusMonths(1))
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

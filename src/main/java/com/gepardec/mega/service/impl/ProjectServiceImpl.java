package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.entity.project.ProjectEntry;
import com.gepardec.mega.db.repository.ProjectRepository;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectFilter;
import com.gepardec.mega.service.api.ProjectService;
import com.gepardec.mega.zep.ZepService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectServiceImpl implements ProjectService {

    private static final String INTERN_PROJECT_CATEGORY = "INT";

    @Inject
    ZepService zepService;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    UserRepository userRepository;

    @Override
    public List<Project> getProjectsForMonthYear(YearMonth payrollMonth) {
        return this.getProjectsForMonthYear(payrollMonth, List.of());
    }

    @Override
    public List<Project> getProjectsForMonthYear(final YearMonth payrollMonth, final List<ProjectFilter> projectFilters) {
        return zepService.getProjectsForMonthYear(payrollMonth)
                .stream()
                .filter(project -> filterProject(project, Optional.ofNullable(projectFilters).orElse(List.of())))
                .toList();
    }

    @Override
    public Optional<Project> getProjectByName(final String projectName, final YearMonth payrollMonth) {
        return zepService.getProjectByName(projectName, payrollMonth);
    }

    @Override
    public void addProject(com.gepardec.mega.db.entity.project.Project project, YearMonth payrollMonth) {

        com.gepardec.mega.db.entity.project.Project projectEntity = projectRepository.findByName(project.getName());

        if (projectEntity == null) {
            projectEntity = new com.gepardec.mega.db.entity.project.Project();
        }

        com.gepardec.mega.db.entity.project.Project finalProjectEntity = projectEntity;
        project.getProjectLeads().forEach(lead -> {
            User user = userRepository.findById(lead.getId());
            if (finalProjectEntity.getProjectLeads() == null) {
                finalProjectEntity.setProjectLeads(new HashSet<>());
            }
            finalProjectEntity.getProjectLeads().add(user);
        });

        com.gepardec.mega.db.entity.project.Project finalProjectEntity1 = projectEntity;

        boolean noProjectEntriesExist = true;
        if (projectEntity.getProjectEntries() != null) {
            noProjectEntriesExist = projectEntity.getProjectEntries()
                    .stream()
                    .noneMatch(pe -> pe.getDate().equals(payrollMonth));
        }

        if (noProjectEntriesExist) {
            if (project.getProjectEntries() != null) {
                project.getProjectEntries().forEach(projectEntry -> {

                    User owner = userRepository.findById(projectEntry.getOwner().getId());
                    User assignee = userRepository.findById(projectEntry.getAssignee().getId());

                    ProjectEntry pe = new ProjectEntry();
                    pe.setPreset(projectEntry.isPreset());
                    pe.setProject(projectEntry.getProject());
                    pe.setStep(projectEntry.getStep());
                    pe.setState(projectEntry.getState());
                    pe.setUpdatedDate(projectEntry.getUpdatedDate());
                    pe.setCreationDate(projectEntry.getCreationDate());
                    pe.setDate(projectEntry.getDate());
                    pe.setName(projectEntry.getName());
                    pe.setOwner(owner);
                    pe.setAssignee(assignee);

                    finalProjectEntity1.addProjectEntry(pe);
                });
            }
        }

        projectEntity.setName(project.getName());
        projectEntity.setZepId(project.getZepId());
        projectEntity.setStartDate(project.getStartDate());
        projectEntity.setEndDate(project.getEndDate());

        projectRepository.merge(projectEntity);
    }

    private boolean filterProject(final Project project, final List<ProjectFilter> projectFilters) {
        validateProjectFilter(projectFilters);

        return projectFilters.stream()
                .allMatch(projectFilter -> filterProject(project, projectFilter));
    }

    private void validateProjectFilter(List<ProjectFilter> projectFilters) {
        if (projectFilters == null || projectFilters.size() < 2) {
            return;
        }

        List<ProjectFilter> conflictingFilter = List.of(ProjectFilter.IS_LEADS_AVAILABLE, ProjectFilter.WITHOUT_LEADS);

        if (new HashSet<>(projectFilters).containsAll(conflictingFilter)) {
            String conflictingStr = conflictingFilter.stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(" & ", "[", "]"));

            throw new IllegalStateException("Conflicting ProjectFilter: " + conflictingStr);
        }
    }

    private boolean filterProject(final Project project, final ProjectFilter projectFilter) {
        switch (projectFilter) {
            case IS_LEADS_AVAILABLE:
                return !project.getLeads().isEmpty();
            case WITHOUT_LEADS:
                return project.getLeads().isEmpty();
            case IS_CUSTOMER_PROJECT:
                return !project.getCategories().contains(INTERN_PROJECT_CATEGORY);
            default:
                throw new IllegalStateException(String.format("projectFilter %s not implemented", projectFilter));
        }
    }
}

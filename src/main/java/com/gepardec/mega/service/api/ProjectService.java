package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectFilter;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface ProjectService {

    List<Project> getProjectsForMonthYear(final YearMonth payrollMonth);

    List<Project> getProjectsForMonthYear(final YearMonth payrollMonth, final List<ProjectFilter> projectFilters);

    Optional<Project> getProjectByName(final String projectName, final YearMonth payrollMonth);

    void addProject(final com.gepardec.mega.db.entity.project.Project project, YearMonth payrollMonth);
}

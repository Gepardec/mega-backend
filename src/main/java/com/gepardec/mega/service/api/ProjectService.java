package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProjectService {

    List<Project> getProjectsForMonthYear(final LocalDate monthYear);

    List<Project> getProjectsForMonthYear(final LocalDate monthYear, final List<ProjectFilter> projectFilters);

    Optional<Project> getProjectByName(final String projectName, final LocalDate monthYear);

    void addProject(final com.gepardec.mega.db.entity.project.Project project, LocalDate selectedDate);
}

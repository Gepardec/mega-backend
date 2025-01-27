package com.gepardec.mega.service.api;

import com.gepardec.mega.db.entity.project.ProjectEntry;
import com.gepardec.mega.rest.model.ProjectEntryDto;

import java.time.YearMonth;
import java.util.List;

public interface ProjectEntryService {
    List<ProjectEntry> findByNameAndDate(final String projectName, final YearMonth payrollMonth);

    boolean update(ProjectEntryDto projectEntry);
}

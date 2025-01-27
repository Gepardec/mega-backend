package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.common.State;
import com.gepardec.mega.db.entity.project.ProjectEntry;
import com.gepardec.mega.db.repository.ProjectEntryRepository;
import com.gepardec.mega.domain.model.ProjectState;
import com.gepardec.mega.domain.model.ProjectStep;
import com.gepardec.mega.rest.model.ProjectEntryDto;
import com.gepardec.mega.service.api.ProjectEntryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class ProjectEntryServiceImpl implements ProjectEntryService {

    @Inject
    ProjectEntryRepository projectEntryRepository;

    @Override
    public List<ProjectEntry> findByNameAndDate(String projectName, YearMonth payrollMonth) {
        return projectEntryRepository.findByNameAndDate(projectName, payrollMonth.atDay(1), payrollMonth.atEndOfMonth());
    }

    @Override
    public boolean update(ProjectEntryDto projectEntry) {
        var projectEntity = findByNameAndEntryDateAndStep(
                projectEntry.getProjectName(),
                LocalDate.parse(projectEntry.getCurrentMonthYear()),
                projectEntry.getStep()
        );
        projectEntity.setState(mapProjectStateFromModelToDatabase(projectEntry.getState()));
        projectEntity.setPreset(projectEntry.isPreset());

        return projectEntryRepository.updateProjectEntry(projectEntity);
    }

    private ProjectEntry findByNameAndEntryDateAndStep(String projectName, LocalDate entryDate, ProjectStep projectStep) {
        return projectEntryRepository.findByNameAndEntryDateAndStep(projectName, entryDate, mapProjectStepFromModelToDatabase(projectStep));
    }

    private State mapProjectStateFromModelToDatabase(ProjectState state) {
        return Arrays.stream(State.values())
                .filter(ps -> ps.name().equals(state.name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("ProjectState not supported!"));
    }

    private com.gepardec.mega.db.entity.project.ProjectStep mapProjectStepFromModelToDatabase(ProjectStep step) {
        return Arrays.stream(com.gepardec.mega.db.entity.project.ProjectStep.values())
                .filter(ps -> ps.name().equals(step.name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("ProjectStep not supported!"));
    }
}

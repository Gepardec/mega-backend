package com.gepardec.mega.service.impl.projectentry;

import com.gepardec.mega.db.entity.common.State;
import com.gepardec.mega.db.entity.project.ProjectEntry;
import com.gepardec.mega.db.entity.project.ProjectStep;
import com.gepardec.mega.db.repository.ProjectEntryRepository;
import com.gepardec.mega.domain.model.ProjectState;
import com.gepardec.mega.rest.model.ProjectEntryDto;
import com.gepardec.mega.service.api.ProjectEntryService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProjectEntryServiceImplTest {

    @Inject
    ProjectEntryService projectEntryService;

    @InjectMock
    ProjectEntryRepository projectEntryRepository;

    @Test
    void findByNameAndDate_returnsProjectEntryList() {
        when(projectEntryRepository.findByNameAndDate(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(createProjectEntryList());

        List<ProjectEntry> actual = projectEntryService.findByNameAndDate("ABC", YearMonth.of(2024, 6));

        assertThat(actual.size()).isOne();
        assertThat(actual.get(0).getName()).isEqualTo("ABC");
    }

    @Test
    void updateProjectEntry_returnsTrue() {
        when(projectEntryRepository.findByNameAndEntryDateAndStep(anyString(), any(LocalDate.class), any(ProjectStep.class)))
                .thenReturn(createProjectEntry());

        when(projectEntryRepository.updateProjectEntry(any(ProjectEntry.class)))
                .thenReturn(true);

        boolean actual = projectEntryService.update(createProjectEntryDto());

        assertThat(actual).isTrue();
    }

    private List<ProjectEntry> createProjectEntryList() {
        List<ProjectEntry> projectEntryList = new ArrayList<>();
        ProjectEntry projectEntry = new ProjectEntry();
        projectEntry.setName("ABC");
        projectEntry.setDate(LocalDate.now());
        projectEntryList.add(projectEntry);
        return projectEntryList;
    }

    private ProjectEntry createProjectEntry() {
        ProjectEntry projectEntry = new ProjectEntry();
        projectEntry.setName("ABC");
        projectEntry.setDate(LocalDate.now());
        projectEntry.setState(State.OPEN);
        projectEntry.setPreset(true);
        return projectEntry;
    }

    private ProjectEntryDto createProjectEntryDto() {
        ProjectEntryDto projectEntryDto = ProjectEntryDto.builder()
                .projectName("ABC")
                .state(ProjectState.OPEN)
                .step(com.gepardec.mega.domain.model.ProjectStep.CONTROL_PROJECT)
                .currentMonthYear("2024-06-01")
                .preset(true)
                .build();

        return projectEntryDto;
    }
}

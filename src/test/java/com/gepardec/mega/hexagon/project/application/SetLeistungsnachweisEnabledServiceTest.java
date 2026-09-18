package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.domain.error.LeistungsnachweisNotApplicableException;
import com.gepardec.mega.hexagon.project.domain.error.ProjectNotFoundException;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.shared.application.security.ForbiddenException;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SetLeistungsnachweisEnabledServiceTest {

    private static final UserId LEAD_ID = UserId.of(UUID.randomUUID());
    private static final UserId OTHER_USER_ID = UserId.of(UUID.randomUUID());

    private ProjectRepository projectRepository;
    private SetLeistungsnachweisEnabledService service;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        service = new SetLeistungsnachweisEnabledService(projectRepository);
    }

    @Test
    void setLeistungsnachweisEnabled_unknownProject_throwsNotFound() {
        ProjectId projectId = ProjectId.generate();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setLeistungsnachweisEnabled(projectId, LEAD_ID, true))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository, never()).saveAll(anyList());
    }

    @Test
    void setLeistungsnachweisEnabled_nonLead_throwsForbiddenWithoutSaving() {
        Project project = new Project(ProjectId.generate(), 1, "X", LocalDate.now(), null, true, true, Set.of(LEAD_ID));
        when(projectRepository.findById(project.id())).thenReturn(Optional.of(project));

        ThrowableAssert.ThrowingCallable throwingCallable = () -> service.setLeistungsnachweisEnabled(project.id(), OTHER_USER_ID, false);

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(ForbiddenException.class);

        verify(projectRepository, never()).saveAll(anyList());
    }

    @Test
    void setLeistungsnachweisEnabled_enableOnBillableProject_saves() {
        Project project = new Project(ProjectId.generate(), 1, "X", LocalDate.now(), null, true, false, Set.of(LEAD_ID));
        when(projectRepository.findById(project.id())).thenReturn(Optional.of(project));

        service.setLeistungsnachweisEnabled(project.id(), LEAD_ID, true);

        verify(projectRepository).saveAll(argThat(projects -> {
            assertThat(projects.getFirst().leistungsnachweisEnabled()).isTrue();
            return true;
        }));
    }

    @Test
    void setLeistungsnachweisEnabled_enableOnNonBillableProject_throwsWithoutSaving() {
        Project project = new Project(ProjectId.generate(), 1, "X", LocalDate.now(), null, false, false, Set.of(LEAD_ID));
        when(projectRepository.findById(project.id())).thenReturn(Optional.of(project));

        ThrowableAssert.ThrowingCallable throwingCallable = () -> service.setLeistungsnachweisEnabled(project.id(), LEAD_ID, true);

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(LeistungsnachweisNotApplicableException.class);

        verify(projectRepository, never()).saveAll(anyList());
    }

    @Test
    void setLeistungsnachweisEnabled_disableOnNonBillableProject_succeeds() {
        Project project = new Project(ProjectId.generate(), 1, "X", LocalDate.now(), null, false, false, Set.of(LEAD_ID));
        when(projectRepository.findById(project.id())).thenReturn(Optional.of(project));

        service.setLeistungsnachweisEnabled(project.id(), LEAD_ID, false);

        verify(projectRepository).saveAll(List.of(project));
    }
}

package com.gepardec.mega.service.impl.project;


import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.service.api.ProjectService;
import com.gepardec.mega.service.api.ProjectSyncService;
import com.gepardec.mega.service.api.UserService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProjectSyncServiceImplTest {

    @Inject
    ProjectSyncService projectSyncService;

    @InjectMock
    Logger logger;

    @InjectMock
    UserService userService;

    @InjectMock
    ProjectService projectService;

    @Test
    void generateProjectsWithDate() {
        LocalDate date = LocalDate.of(2024, 5, 1);
        List<User> activeUsers = List.of(mock(User.class), mock(User.class));
        List<Project> projectsForMonthYear = List.of(mock(Project.class), mock(Project.class));

        when(userService.findActiveUsers())
                .thenReturn(activeUsers);
        when(projectService.getProjectsForMonthYear(any(LocalDate.class), anyList()))
                .thenReturn(projectsForMonthYear);
        when(projectService.getProjectsForMonthYear(any(LocalDate.class)))
                .thenReturn(projectsForMonthYear);


        boolean result = projectSyncService.generateProjects(date);

        assertThat(result).isTrue();
        verify(userService).findActiveUsers();
        verify(projectService).getProjectsForMonthYear(eq(date), anyList());
        verify(projectService).getProjectsForMonthYear(eq(date));
        verify(logger, atLeastOnce()).info(anyString(), any(Instant.class));
    }

    @Test
    void generateProjects() {
        LocalDate expectedDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        List<User> activeUsers = List.of(mock(User.class), mock(User.class));
        List<Project> projectsForMonthYear = List.of(mock(Project.class), mock(Project.class));

        when(userService.findActiveUsers())
                .thenReturn(activeUsers);
        when(projectService.getProjectsForMonthYear(any(LocalDate.class), anyList()))
                .thenReturn(projectsForMonthYear);
        when(projectService.getProjectsForMonthYear(any(LocalDate.class)))
                .thenReturn(projectsForMonthYear);

        boolean result = projectSyncService.generateProjects(expectedDate);

        assertThat(result).isTrue();
        verify(userService).findActiveUsers();
        verify(projectService).getProjectsForMonthYear(eq(expectedDate), anyList());
        verify(projectService).getProjectsForMonthYear(eq(expectedDate));
        verify(logger, atLeastOnce()).info(anyString(), any(Instant.class));
    }
}

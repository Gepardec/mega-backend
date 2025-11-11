package com.gepardec.mega.service.impl.project;

import com.gepardec.mega.db.entity.common.State;
import com.gepardec.mega.db.entity.project.ProjectEntry;
import com.gepardec.mega.db.entity.project.ProjectStep;
import com.gepardec.mega.db.repository.ProjectRepository;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectFilter;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.service.impl.ProjectServiceImpl;
import com.gepardec.mega.zep.ZepService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProjectServiceImplTest {

    @InjectMock
    ZepService zepService;

    @Inject
    ProjectServiceImpl projectService;

    @InjectMock
    ProjectRepository projectRepository;

    @InjectMock
    UserRepository userRepository;

    private Project.Builder projectFor(final String id) {
        return Project.builder()
                .projectId(id)
                .description("Description of Project %s".formatted(id));
    }

    @Test
    void whenResultIsEmpty_thenReturnEmptyProjectList() {
        // Given
        when(zepService.getProjectsForMonthYear(any())).thenReturn(List.of());

        // When
        final List<Project> projectsForMonthYear = projectService.getProjectsForMonthYear(YearMonth.now());

        // Then
        assertThat(projectsForMonthYear).isEmpty();
    }

    @Test
    void whenResult_thenReturnProjectList() {
        // Given
        when(zepService.getProjectsForMonthYear(any())).thenReturn(List.of(projectFor("1")
                .leads(List.of())
                .employees(List.of())
                .categories(List.of())
                .startDate(LocalDate.now())
                .build()));

        // When
        final List<Project> projectsForMonthYear = projectService.getProjectsForMonthYear(YearMonth.now());

        // Then
        assertThat(projectsForMonthYear).isNotEmpty();
    }

    @Test
    void whenFilterCustomer_thenReturnProjectListWithCustomerProjects() {
        // Given
        when(zepService.getProjectsForMonthYear(any())).thenReturn(List.of(
                projectFor("Intern")
                        .leads(List.of())
                        .employees(List.of())
                        .categories(List.of("INT"))
                        .startDate(LocalDate.now())
                        .build(),
                projectFor("Kunde")
                        .leads(List.of())
                        .employees(List.of())
                        .categories(List.of())
                        .startDate(LocalDate.now())
                        .build()));

        // When
        final List<Project> projectsForMonthYear = projectService.getProjectsForMonthYear(YearMonth.now(), List.of(ProjectFilter.IS_CUSTOMER_PROJECT));

        // Then
        assertThat(projectsForMonthYear).hasSize(1);
        assertThat(projectsForMonthYear.getFirst().getProjectId()).isEqualTo("Kunde");
    }

    @Test
    void whenFilterLeads_thenReturnProjectListWithLeads() {
        // Given
        when(zepService.getProjectsForMonthYear(any())).thenReturn(List.of(
                projectFor("1")
                        .leads(List.of(User.builder()
                                        .dbId(1)
                                        .userId("userId")
                                        .firstname("Gepard")
                                        .lastname("Gepardec")
                                        .email("%s%s.%s@gepardec.com".formatted("Gepard", 1, "Gepardec"))
                                        .roles(Set.of(Role.EMPLOYEE)).build())
                                .stream().map(User::getUserId).toList())
                        .employees(List.of())
                        .categories(List.of())
                        .startDate(LocalDate.now())
                        .build(),
                projectFor("2")
                        .leads(List.of())
                        .employees(List.of())
                        .categories(List.of())
                        .startDate(LocalDate.now())
                        .build()));

        // When
        final List<Project> projectsForMonthYear = projectService.getProjectsForMonthYear(YearMonth.now(), List.of(ProjectFilter.IS_LEADS_AVAILABLE));

        // Then
        assertThat(projectsForMonthYear).hasSize(1);
        assertThat(projectsForMonthYear.getFirst().getProjectId()).isEqualTo("1");
    }

    @Test
    void getProjectByName_whenProjectExists_thenReturnProject() {
        com.gepardec.mega.domain.model.Project project = Project.builder().projectId("testId").build();
        when(zepService.getProjectByName(anyString(), any(YearMonth.class)))
                .thenReturn(Optional.of(project));

        Optional<Project> result = projectService.getProjectByName("Testprojectname", YearMonth.now());

        assertThat(result).isPresent();
        assertThat(result.get().getProjectId()).isEqualTo("testId");
    }

    @Test
    void addProject_whenProjectDoesNotExistHasLeadsAndNoProjectEntries_createsNewProject() {
        String projectName = "ProjectA";
        com.gepardec.mega.db.entity.project.Project project = new com.gepardec.mega.db.entity.project.Project();
        project.setName(projectName);
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusMonths(1));
        project.setProjectLeads(Set.of(createUser(1L, "TestLead", "1", "testlead1@gmail.com", Set.of(Role.PROJECT_LEAD)),
                createUser(2L, "TestLead", "2", "testlead2@gmail.com", Set.of(Role.PROJECT_LEAD, Role.EMPLOYEE))));

        when(projectRepository.findByName(anyString()))
                .thenReturn(null);
        when(projectRepository.merge(any(com.gepardec.mega.db.entity.project.Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userRepository.findById(anyLong()))
                .thenReturn(createUser(1L, "TestLead", "1", "testlead1@gmail.com", Set.of(Role.PROJECT_LEAD)))
                .thenReturn(createUser(2L, "TestLead", "2", "testlead2@gmail.com", Set.of(Role.PROJECT_LEAD, Role.EMPLOYEE)));


        projectService.addProject(project, YearMonth.now());

        ArgumentCaptor<com.gepardec.mega.db.entity.project.Project> projectEntityCaptor = ArgumentCaptor.forClass(com.gepardec.mega.db.entity.project.Project.class);
        verify(projectRepository, times(1)).merge(projectEntityCaptor.capture());

        com.gepardec.mega.db.entity.project.Project capturedProjectEntity = projectEntityCaptor.getValue();
        assertThat(capturedProjectEntity).isNotNull();
        assertThat(capturedProjectEntity.getName()).isEqualTo(project.getName());
        assertThat(capturedProjectEntity.getStartDate()).isEqualTo(project.getStartDate());
        assertThat(capturedProjectEntity.getEndDate()).isEqualTo(project.getEndDate());
    }

    @Test
    void addProject_whenProjectExistsHasLeadsAndProjectEntries_addsExistingProject() {
        String projectName = "ProjectA";
        HashSet<ProjectEntry> projectEntries = new HashSet<>();
        HashSet<com.gepardec.mega.db.entity.employee.User> projectLeads = new HashSet<>();
        projectLeads.add(createUser(1L, "TestLead", "1", "testlead1@gmail.com", Set.of(Role.PROJECT_LEAD)));
        projectLeads.add(createUser(2L, "TestLead", "2", "testlead2@gmail.com", Set.of(Role.PROJECT_LEAD, Role.EMPLOYEE)));
        com.gepardec.mega.db.entity.project.Project project = new com.gepardec.mega.db.entity.project.Project();
        project.setName(projectName);
        project.setId(1L);
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusMonths(1));
        projectEntries.add(createProjectEntry(project, LocalDate.of(2024, 6, 1)));
        project.setProjectLeads(projectLeads);
        project.setProjectEntries(projectEntries);


        when(projectRepository.findByName(anyString()))
                .thenReturn(project);
        when(projectRepository.merge(any(com.gepardec.mega.db.entity.project.Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userRepository.findById(anyLong()))
                .thenReturn(createUser(1L, "TestLead", "1", "testlead1@gmail.com", Set.of(Role.PROJECT_LEAD)))
                .thenReturn(createUser(2L, "TestLead", "2", "testlead2@gmail.com", Set.of(Role.PROJECT_LEAD, Role.EMPLOYEE)));

        when(userRepository.findById(3L))
                .thenReturn(createUser(3L, "TestOwner", "1", "testowner1@gmail.com", Set.of(Role.PROJECT_LEAD)));


        projectService.addProject(project, YearMonth.now());

        ArgumentCaptor<com.gepardec.mega.db.entity.project.Project> projectEntityCaptor =
                ArgumentCaptor.forClass(com.gepardec.mega.db.entity.project.Project.class);
        verify(projectRepository, times(1)).merge(projectEntityCaptor.capture());

        com.gepardec.mega.db.entity.project.Project capturedProjectEntity = projectEntityCaptor.getValue();
        assertThat(capturedProjectEntity).isNotNull();
        assertThat(capturedProjectEntity.getName()).isEqualTo(project.getName());
        assertThat(capturedProjectEntity.getStartDate()).isEqualTo(project.getStartDate());
        assertThat(capturedProjectEntity.getEndDate()).isEqualTo(project.getEndDate());

        // Assert project leads are correctly set
        assertThat(capturedProjectEntity.getProjectLeads()).isNotNull();
        assertThat(capturedProjectEntity.getProjectLeads()).hasSize(2);
        assertThat(capturedProjectEntity.getProjectLeads()).extracting("id")
                .containsExactlyInAnyOrder(1L, 2L);

        // Assert project entries are correctly set
        assertThat(capturedProjectEntity.getProjectEntries()).isNotNull();
        assertThat(capturedProjectEntity.getProjectEntries()).hasSize(2);
        assertThat(capturedProjectEntity.getProjectEntries()).extracting("name")
                .containsExactlyInAnyOrder("ProjectA", "ProjectA");


    }

    private com.gepardec.mega.db.entity.employee.User createUser(Long userId, String firstname, String lastname, String email, Set<Role> roles) {
        com.gepardec.mega.db.entity.employee.User user = new com.gepardec.mega.db.entity.employee.User();
        user.setLastname(lastname);
        user.setFirstname(firstname);
        user.setId(userId);
        user.setEmail(email);
        user.setRoles(roles);
        return user;
    }

    private ProjectEntry createProjectEntry(com.gepardec.mega.db.entity.project.Project project, LocalDate projectDate) {
        ProjectEntry projectEntry = new ProjectEntry();
        projectEntry.setPreset(true);
        projectEntry.setProject(project);
        projectEntry.setStep(ProjectStep.CONTROL_PROJECT);
        projectEntry.setState(State.OPEN);
        projectEntry.setUpdatedDate(LocalDateTime.now());
        projectEntry.setCreationDate(LocalDateTime.now());
        projectEntry.setDate(projectDate);
        projectEntry.setName(project.getName());
        projectEntry.setOwner(createUser(3L, "TestOwner", "1", "testowner1@gmail.com", Set.of(Role.PROJECT_LEAD)));
        projectEntry.setAssignee(createUser(3L, "TestOwner", "1", "testowner1@gmail.com", Set.of(Role.PROJECT_LEAD)));
        projectEntry.setId(project.getId());

        return projectEntry;
    }
}

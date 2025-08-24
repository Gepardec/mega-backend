package com.gepardec.mega.service.impl.sync;

import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.personio.employees.PersonioEmployeesService;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.ProjectService;
import com.gepardec.mega.service.api.SyncService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class SyncServiceImplTest {

    @InjectMock
    EmployeeService employeeService;

    @InjectMock
    ProjectService projectService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    PersonioEmployeesService personioEmployeesService;

    @Inject
    SyncService syncService;

    @Test
    void syncEmployees() {
        // Mock projects
        Project project = Project.builder()
                .projectId("xyz")
                .leads(new ArrayList<>()) // Ensure leads is initialized to a non-null list
                .build();
        List<Project> projects = Collections.singletonList(project);

        // Mock service responses
        when(projectService.getProjectsForMonthYear(any()))
                .thenReturn(projects);

        // Mock other necessary responses
        List<Employee> employees = new ArrayList<>();
        employees.add(Employee.builder().firstname("TEST").build());
        when(employeeService.getAllActiveEmployees())
                .thenReturn(employees);

        List<User> existingUsers = new ArrayList<>();
        existingUsers.add(User.of("test@gmail.com"));
        when(userRepository.listAll())
                .thenReturn(existingUsers);

        when(personioEmployeesService.getPersonioEmployeeByEmail(null)).thenReturn(Optional.empty());

        // Execute the method under test
        syncService.syncEmployees();

        // Verify interactions
        verify(projectService, times(1)).getProjectsForMonthYear(any());
        verify(employeeService, times(1)).getAllActiveEmployees();
        verify(userRepository, times(1)).listAll();
    }
}

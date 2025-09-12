package com.gepardec.mega.service.impl.stepentry;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.StepEntryRepository;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectEmployees;
import com.gepardec.mega.domain.model.Step;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.service.api.StepEntryService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class StepEntryServiceImplTest {

    @Inject
    StepEntryService stepEntryService;

    @InjectMock
    StepEntryRepository stepEntryRepository;

    @InjectMock
    Logger logger;

    @Test
    void findEmployeeCheckState_whenValidStepEntries_thenValidState() {
        StepEntry stepEntry = createStepEntry(1L);

        Optional<StepEntry> stepEntries = Optional.of(stepEntry);
        when(stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.anyString())).thenReturn(stepEntries);

        Optional<EmployeeState> states = stepEntryService.findEmployeeCheckState(createEmployee());

        assertAll(
                () -> assertThat(states).isPresent(),
                () -> assertThat(states).get().isEqualTo(EmployeeState.IN_PROGRESS)
        );
    }

    @Test
    void findEmployeeCheckState_whenNoStepEntries_thenEmpty() {
        when(stepEntryRepository.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.anyString())).thenReturn(List.of());

        Optional<EmployeeState> states = stepEntryService.findEmployeeCheckState(createEmployee());
        assertThat(states).isEmpty();
    }

    @Test
    void findEmployeeCheckState_whenNoEmployee_thenEmpty() {
        Optional<EmployeeState> states = stepEntryService.findEmployeeCheckState(null);
        assertThat(states).isEmpty();
    }

    @Test
    void findEmployeeInternalCheckState_whenNoEmployee_thenEmpty() {
        Optional<EmployeeState> states = stepEntryService.findEmployeeInternalCheckState(null, YearMonth.now());
        assertThat(states).isEmpty();
    }

    @Test
    void findEmployeeInternalCheckState_whenEmployee_thenStep() {
        when(stepEntryRepository.findAllOwnedAndAssignedStepEntriesForEmployeeForControlInternalTimes(any(LocalDate.class), anyString())).thenReturn(Optional.of(createStepEntry(0L)));

        Optional<EmployeeState> states = stepEntryService.findEmployeeInternalCheckState(createEmployee(), YearMonth.now());
        assertThat(states).isPresent();
    }

    @Test
    void areOtherChecksDone_whenAllInProgress_thenFalse() {
        StepEntry stepEntry1 = createStepEntry(1L);
        StepEntry stepEntry2 = createStepEntry(2L);

        List<StepEntry> stepEntries = List.of(stepEntry1, stepEntry2);
        when(stepEntryRepository.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.anyString())).thenReturn(stepEntries);

        boolean areOtherChecksDone = stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(createEmployee(), YearMonth.now())
                .stream().allMatch(stepEntry -> stepEntry.getState() == EmployeeState.DONE);
        assertThat(areOtherChecksDone).isFalse();
    }

    @Test
    void areOtherChecksDone_whenNoStepEntries_thenTrue() {
        when(stepEntryRepository.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.anyString())).thenReturn(List.of());

        boolean areOtherChecksDone = stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(createEmployee(), YearMonth.now())
                .stream().allMatch(stepEntry -> stepEntry.getState() == EmployeeState.DONE);

        assertThat(areOtherChecksDone).isTrue();
    }

    @Test
    void areOtherChecksDone_whenAllDone_thenTrue() {
        StepEntry stepEntry1 = createStepEntry(1L);
        StepEntry stepEntry2 = createStepEntry(2L);

        stepEntry1.setState(EmployeeState.DONE);
        stepEntry2.setState(EmployeeState.DONE);

        List<StepEntry> stepEntries = List.of(stepEntry1, stepEntry2);
        when(stepEntryRepository.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.anyString())).thenReturn(stepEntries);

        boolean areOtherChecksDone = stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(createEmployee(), YearMonth.now())
                .stream().allMatch(stepEntry -> stepEntry.getState() == EmployeeState.DONE);

        assertThat(areOtherChecksDone).isTrue();
    }

    @Test
    void setOpenAndAssignedStepEntriesDone_when0_thenFalse() {
        when(stepEntryRepository.updateStateAssigned(ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.any(LocalDate.class), ArgumentMatchers.anyString(), anyLong(),
                argThat(EmployeeState.DONE::equals)))
                .thenReturn(0);

        Employee employee = createEmployee();
        boolean updated = stepEntryService.setOpenAndAssignedStepEntriesDone(employee, 0L, YearMonth.now().plusMonths(1));
        assertThat(updated).isFalse();
    }

    @Test
    void setOpenAndAssignedStepEntriesDone_when1_thenTrue() {
        when(stepEntryRepository.updateStateAssigned(ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.any(LocalDate.class), ArgumentMatchers.anyString(), anyLong(),
                argThat(EmployeeState.DONE::equals)))
                .thenReturn(1);

        Employee employee = createEmployee();
        LocalDate from = DateUtils.getFirstDayOfFollowingMonth(employee.getReleaseDate());
        LocalDate to = DateUtils.getLastDayOfFollowingMonth(employee.getReleaseDate());

        boolean updated = stepEntryService.setOpenAndAssignedStepEntriesDone(employee, 1L, YearMonth.now().plusMonths(1));

        assertThat(updated).isTrue();
    }

    @Test
    void updateStepEntryReasonForStepWithStateDone_whenUpdateSuccessful_thenReturnTrue() {
        Employee employee = createEmployee();
        Long stepId = 123L;
        YearMonth payrollMonth = YearMonth.now();
        String reason = "Test reason";

        when(stepEntryRepository.updateReasonForStepEntryWithStateDone(any(LocalDate.class), any(LocalDate.class), anyString(), any(Long.class), anyString()))
                .thenReturn(1);

        boolean result = stepEntryService.updateStepEntryReasonForStepWithStateDone(employee, stepId, payrollMonth, reason);

        assertThat(result).isTrue();
        verify(stepEntryRepository).updateReasonForStepEntryWithStateDone(eq(payrollMonth.atDay(1)), eq(payrollMonth.atEndOfMonth()), eq(employee.getEmail()), eq(stepId), eq(reason));
    }

    @Test
    void updateStepEntryReasonForStepWithStateDone_whenUpdateFails_thenReturnFalse() {
        Employee employee = createEmployee();
        Long stepId = 123L;
        YearMonth payrollMonth = YearMonth.now();
        String reason = "Test reason";

        when(stepEntryRepository.updateReasonForStepEntryWithStateDone(any(LocalDate.class), any(LocalDate.class), anyString(), any(Long.class), anyString()))
                .thenReturn(0);

        boolean result = stepEntryService.updateStepEntryReasonForStepWithStateDone(employee, stepId, payrollMonth, reason);

        assertThat(result).isFalse();
        verify(stepEntryRepository).updateReasonForStepEntryWithStateDone(eq(payrollMonth.atDay(1)), eq(payrollMonth.atEndOfMonth()), eq(employee.getEmail()), eq(stepId), eq(reason));
    }

    @Test
    void findAllStepEntriesForEmployee_whenEmployeeIsNull_thenThrowsException() {
        assertThatThrownBy(() -> stepEntryService.findAllStepEntriesForEmployee(null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Employee must not be null!");
    }

    @Test
    void findAllStepEntriesForEmployee_whenValidStepEntries_thenListOfEntries() {
        when(stepEntryRepository.findAllOwnedStepEntriesInRange(
                ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.anyString()
        )).thenReturn(List.of(createStepEntry(1L)));

        Employee empl = createEmployee();
        YearMonth payrollMonth = YearMonth.from(DateUtils.getFirstDayOfFollowingMonth(empl.getReleaseDate()));
        List<StepEntry> result = stepEntryService.findAllStepEntriesForEmployee(empl, payrollMonth);
        verify(stepEntryRepository, times(1)).findAllOwnedStepEntriesInRange(
                DateUtils.getFirstDayOfFollowingMonth(empl.getReleaseDate()),
                DateUtils.getLastDayOfFollowingMonth(empl.getReleaseDate()),
                empl.getEmail()
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void findStepEntryForEmployeeAtStep_whenEmployeeIsNull_thenThrowsException() {
        assertThatThrownBy(() -> stepEntryService.findStepEntryForEmployeeAtStep(2L, null, "", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("'employeeEmail' must not be null!");
    }

    @Test
    void findStepEntryForEmployeeAtStep_whenNoStepIsFound_thenThrowsException() {
        when(stepEntryRepository.findStepEntryForEmployeeAtStepInRange(
                ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.anyString(),
                anyLong(),
                ArgumentMatchers.anyString()
        )).thenReturn(Optional.empty());

        Employee employee = createEmployee();

        assertThatThrownBy(() -> stepEntryService.findStepEntryForEmployeeAtStep(2L, employee.getEmail(), "", YearMonth.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No StepEntries found for Employee ");
    }

    @Test
    void findStepEntryForEmployeeAtStep_whenValid_thenReturnStepEntry() {
        when(stepEntryRepository.findStepEntryForEmployeeAtStepInRange(
                ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.any(LocalDate.class),
                ArgumentMatchers.anyString(),
                anyLong(),
                ArgumentMatchers.anyString()
        )).thenReturn(Optional.of(createStepEntry(1L)));

        Employee employee = createEmployee();
        StepEntry stepEntry = stepEntryService.findStepEntryForEmployeeAtStep(2L, employee.getEmail(), "", YearMonth.now());
        assertThat(stepEntry).isNotNull();
        assertThat(stepEntry.getId()).isEqualTo(1L);
        assertThat(stepEntry.getProject()).isEqualTo("Liwest-EMS");
        assertThat(stepEntry.getState()).isEqualTo(EmployeeState.IN_PROGRESS);
    }

    @Test
    void getProjectEmployeesForPM_whenValid_thenReturnListOfEntries() {
        when(stepEntryRepository.findAllStepEntriesForPMInRange(
                ArgumentMatchers.any(LocalDate.class), ArgumentMatchers.any(LocalDate.class), ArgumentMatchers.anyString())
        ).thenReturn(createStepEntriesForPM());

        List<ProjectEmployees> projectEmployees = stepEntryService.getProjectEmployeesForPM(YearMonth.now(), "no-reply@gepardec.com");

        assertAll(
                () -> assertThat(projectEmployees).isNotNull(),
                () -> assertThat(projectEmployees).hasSize(1),
                () -> assertThat(projectEmployees.getFirst().getProjectId()).isEqualTo("Liwest-EMS"),
                () -> assertThat(projectEmployees.getFirst().getEmployees()).containsExactlyInAnyOrder("008", "010", "012", "020")
        );
    }

    @Test
    void addStepEntry_whenProjectIsNotNull_thenCreateStepEntry() {
        com.gepardec.mega.domain.model.StepEntry stepEntry = com.gepardec.mega.domain.model.StepEntry.builder()
                .owner(com.gepardec.mega.domain.model.User.builder().dbId(1L).build())
                .step(Step.builder().dbId(1L).build())
                .assignee(com.gepardec.mega.domain.model.User.builder().dbId(1L).build())
                .date(LocalDate.of(2024, 5, 12))
                .project(Project.builder().projectId("ABC").build())
                .build();

        User ownerDb = new User();
        ownerDb.setId(stepEntry.getOwner().getDbId());

        User assigneeDb = new User();
        assigneeDb.setId(stepEntry.getAssignee().getDbId());

        com.gepardec.mega.db.entity.employee.Step step = new com.gepardec.mega.db.entity.employee.Step();
        step.setId(stepEntry.getStep().getDbId());


        StepEntry expectedStepEntry = new StepEntry();
        expectedStepEntry.setDate(stepEntry.getDate());
        expectedStepEntry.setProject(stepEntry.getProject().getProjectId());
        expectedStepEntry.setState(EmployeeState.OPEN);
        expectedStepEntry.setOwner(ownerDb);
        expectedStepEntry.setAssignee(assigneeDb);
        expectedStepEntry.setStep(step);

        stepEntryService.addStepEntry(stepEntry);

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        verify(logger).debug(eq("inserting step entry {}"), argumentCaptor.capture());

        StepEntry capturedStepEntry = (StepEntry) argumentCaptor.getValue();
        assertThat(capturedStepEntry.getProject()).isEqualTo(expectedStepEntry.getProject());
    }

    @Test
    void addStepEntry_whenProjectIsNull_thenCreateStepEntry() {
        com.gepardec.mega.domain.model.StepEntry stepEntry = com.gepardec.mega.domain.model.StepEntry.builder()
                .owner(com.gepardec.mega.domain.model.User.builder().dbId(1L).build())
                .step(Step.builder().dbId(1L).build())
                .assignee(com.gepardec.mega.domain.model.User.builder().dbId(1L).build())
                .date(LocalDate.of(2024, 5, 12))
                .project(null)
                .build();

        User ownerDb = new User();
        ownerDb.setId(stepEntry.getOwner().getDbId());

        User assigneeDb = new User();
        assigneeDb.setId(stepEntry.getAssignee().getDbId());

        com.gepardec.mega.db.entity.employee.Step step = new com.gepardec.mega.db.entity.employee.Step();
        step.setId(stepEntry.getStep().getDbId());


        StepEntry expectedStepEntry = new StepEntry();
        expectedStepEntry.setDate(stepEntry.getDate());
        expectedStepEntry.setProject(null);
        expectedStepEntry.setState(EmployeeState.OPEN);
        expectedStepEntry.setOwner(ownerDb);
        expectedStepEntry.setAssignee(assigneeDb);
        expectedStepEntry.setStep(step);

        stepEntryService.addStepEntry(stepEntry);

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        verify(logger).debug(eq("inserting step entry {}"), argumentCaptor.capture());

        StepEntry capturedStepEntry = (StepEntry) argumentCaptor.getValue();
        assertThat(capturedStepEntry.getProject()).isEqualTo(expectedStepEntry.getProject());
    }

    @Test
    void findEmployeeCheckState_whenEmployeeIsNull_thenReturnOptionalEmpty() {
        assertThat(stepEntryService.findEmployeeCheckState(null)).isEmpty();
    }

    @Test
    void findStepEntryForEmployeeAndProjectAtStep_whenNoEntriesFound_thenThrowsException() {
        when(stepEntryRepository.findStepEntryForEmployeeAndProjectAtStepInRange(any(LocalDate.class),
                        any(LocalDate.class),
                        anyString(),
                        anyLong(),
                        anyString(),
                        anyString()
                )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stepEntryService.findStepEntryForEmployeeAndProjectAtStep(1L, "max.mustermann@gmail.com", "max.mustermann@gmail.com", "ABC", YearMonth.of(2025, 5)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateStepEntryStateForEmployeeInProject() {
        Employee employee = Employee.builder().userId("007-JBond")
                .firstname("James")
                .lastname("Bond")
                .email("james.bond@gmail.com")
                .build();

        try (MockedStatic<DateUtils> dateUtilsMockedStatic = mockStatic(DateUtils.class)) {
            dateUtilsMockedStatic.when(() -> DateUtils.getFirstDayOfCurrentMonth("2024-05"))
                    .thenReturn(LocalDate.of(2024, 5, 1));
            dateUtilsMockedStatic.when(() -> DateUtils.getLastDayOfCurrentMonth("2024-05"))
                    .thenReturn(LocalDate.of(2024, 5, 30));

            when(stepEntryRepository.updateStateAssigned(
                    any(LocalDate.class),
                    any(LocalDate.class),
                    anyString(),
                    anyLong(),
                    anyString(),
                    any(EmployeeState.class))
            )
                    .thenReturn(1);


            boolean result = stepEntryService.updateStepEntryStateForEmployeeInProject(employee, 123L, "PROJECT123", "2024-05", EmployeeState.OPEN);

            verify(stepEntryRepository, times(1))
                    .updateStateAssigned(LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 30),
                            "james.bond@gmail.com", 123L, "PROJECT123", EmployeeState.OPEN);

            assertThat(result).isTrue();
        }
    }

    @Test
    void getAllProjectEmployeesForPM_whenValidRange_thenReturnProjectEmployees() {
        YearMonth payrollMonth = YearMonth.of(2024, 1);

        List<StepEntry> stepEntries = List.of(
                createStepEntryWithProjectAndOwner("Project1", "employee1"),
                createStepEntryWithProjectAndOwner("Project1", "employee2"),
                createStepEntryWithProjectAndOwner("Project2", "employee3")
        );

        when(stepEntryRepository.findAllStepEntriesForAllPMInRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(stepEntries);

        List<ProjectEmployees> result = stepEntryService.getAllProjectEmployeesForPM(payrollMonth);

        assertThat(result).hasSize(2);

        ProjectEmployees project1 = result.stream()
                .filter(pe -> "Project1".equals(pe.getProjectId()))
                .findFirst()
                .orElse(null);
        assertThat(project1).isNotNull();
        assertThat(project1.getEmployees()).containsExactlyInAnyOrder("employee1", "employee2");

        ProjectEmployees project2 = result.stream()
                .filter(pe -> "Project2".equals(pe.getProjectId()))
                .findFirst()
                .orElse(null);
        assertThat(project2).isNotNull();
        assertThat(project2.getEmployees()).containsExactly("employee3");

        verify(stepEntryRepository, times(1)).findAllStepEntriesForAllPMInRange(payrollMonth.atDay(1), payrollMonth.atEndOfMonth());
    }

    @Test
    void findAllStepEntriesForEmployeeAndProject_whenValidInputs_thenReturnStepEntries() {
        Employee employee = createEmployee();
        String projectId = "Project1";
        String assigneeEmail = "assignee@example.com";
        YearMonth payrollMonth = YearMonth.of(2024, 1);

        List<StepEntry> stepEntriesProjectSpecific = List.of(
                createStepEntryWithDetails(1L, projectId, employee.getEmail(), assigneeEmail),
                createStepEntryWithDetails(2L, projectId, employee.getEmail(), assigneeEmail)
        );

        List<StepEntry> stepEntriesGeneral = List.of(
                createStepEntryWithDetails(1L, "Project2", employee.getEmail(), assigneeEmail),
                createStepEntryWithDetails(2L, "Project3", employee.getEmail(), assigneeEmail)
        );

        when(stepEntryRepository.findAllOwnedStepEntriesInRange(any(LocalDate.class), any(LocalDate.class), anyString(), anyString(), anyString()))
                .thenReturn(stepEntriesProjectSpecific);
        when(stepEntryRepository.findAllOwnedStepEntriesInRange(payrollMonth.atDay(1), payrollMonth.atEndOfMonth(), employee.getEmail()))
                .thenReturn(stepEntriesGeneral);

        List<StepEntry> result = stepEntryService.findAllStepEntriesForEmployeeAndProject(employee, projectId, assigneeEmail, payrollMonth);

        assertThat(result).hasSize(4);
        assertThat(result).containsAll(stepEntriesProjectSpecific);
        assertThat(result).containsAll(stepEntriesGeneral);

        verify(stepEntryRepository, times(1))
                .findAllOwnedStepEntriesInRange(payrollMonth.atDay(1), payrollMonth.atEndOfMonth(), employee.getEmail(), projectId, assigneeEmail);
        verify(stepEntryRepository, times(1))
                .findAllOwnedStepEntriesInRange(payrollMonth.atDay(1), payrollMonth.atEndOfMonth(), employee.getEmail());
    }

    @Test
    void findStepEntryForEmployeeAndProjectAtStep_whenValidInputs_thenReturnStepEntry() {
        Long stepId = 1L;
        String employeeEmail = "employee@example.com";
        String assigneeEmail = "assignee@example.com";
        String project = "Project1";
        YearMonth payrollMonth = YearMonth.of(2024, 1);

        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 1, 31);

        StepEntry expectedStepEntry = createStepEntryWithDetails(stepId, project, employeeEmail, assigneeEmail);

        try (MockedStatic<DateUtils> dateUtilsMockedStatic = mockStatic(DateUtils.class)) {
            dateUtilsMockedStatic.when(() -> DateUtils.getFirstDayOfCurrentMonth(anyString()))
                    .thenReturn(fromDate);
            dateUtilsMockedStatic.when(() -> DateUtils.getLastDayOfCurrentMonth(anyString()))
                    .thenReturn(toDate);

            when(stepEntryRepository.findStepEntryForEmployeeAndProjectAtStepInRange(
                    any(LocalDate.class), any(LocalDate.class), anyString(), anyLong(), anyString(), anyString()))
                    .thenReturn(Optional.of(expectedStepEntry));

            StepEntry result = stepEntryService.findStepEntryForEmployeeAndProjectAtStep(stepId, employeeEmail, assigneeEmail, project, payrollMonth);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedStepEntry);
            verify(stepEntryRepository, times(1)).findStepEntryForEmployeeAndProjectAtStepInRange(
                    fromDate, toDate, employeeEmail, stepId, assigneeEmail, project);
        }
    }

    @Test
    void findStepEntryForEmployeeAndProjectAtStep_whenNoEntryFound_thenThrowsException() {
        Long stepId = 1L;
        String employeeEmail = "employee@example.com";
        String assigneeEmail = "assignee@example.com";
        String project = "Project1";
        YearMonth payrollMonth = YearMonth.of(2024, 1);

        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 1, 31);

        try (MockedStatic<DateUtils> dateUtilsMockedStatic = mockStatic(DateUtils.class)) {
            dateUtilsMockedStatic.when(() -> DateUtils.getFirstDayOfCurrentMonth(anyString()))
                    .thenReturn(fromDate);
            dateUtilsMockedStatic.when(() -> DateUtils.getLastDayOfCurrentMonth(anyString()))
                    .thenReturn(toDate);

            when(stepEntryRepository.findStepEntryForEmployeeAndProjectAtStepInRange(
                    any(LocalDate.class), any(LocalDate.class), anyString(), anyLong(), anyString(), anyString()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> stepEntryService.findStepEntryForEmployeeAndProjectAtStep(stepId, employeeEmail, assigneeEmail, project, payrollMonth))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No StepEntries found for Employee %s".formatted(employeeEmail));
            verify(stepEntryRepository, times(1)).findStepEntryForEmployeeAndProjectAtStepInRange(
                    fromDate, toDate, employeeEmail, stepId, assigneeEmail, project);
        }
    }


    @Test
    void updateStepEntryStateForEmployee_whenUpdateSuccessful_thenReturnTrue() {
        Employee employee = createEmployee();
        Long stepId = 1L;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 31);
        EmployeeState newState = EmployeeState.DONE;
        String reason = "Task completed";

        when(stepEntryRepository.updateStateAssignedWithReason(any(LocalDate.class), any(LocalDate.class), anyString(), anyLong(), any(EmployeeState.class), anyString()))
                .thenReturn(1);

        boolean result = stepEntryService.updateStepEntryStateForEmployee(employee, stepId, from, to, newState, reason);

        assertThat(result).isTrue();
        verify(stepEntryRepository, times(1)).updateStateAssignedWithReason(from, to, employee.getEmail(), stepId, newState, reason);
    }

    private StepEntry createStepEntryWithDetails(Long id, String project, String ownerEmail, String assigneeEmail) {
        StepEntry stepEntry = new StepEntry();
        stepEntry.setProject(project);
        stepEntry.setId(id);

        User owner = new User();
        owner.setEmail(ownerEmail);
        stepEntry.setOwner(owner);

        User assignee = new User();
        assignee.setEmail(assigneeEmail);
        stepEntry.setAssignee(assignee);

        return stepEntry;
    }


    private StepEntry createStepEntryWithProjectAndOwner(String project, String ownerZepId) {
        StepEntry stepEntry = new StepEntry();
        stepEntry.setProject(project);

        User owner = new User();
        owner.setZepId(ownerZepId);
        stepEntry.setOwner(owner);

        return stepEntry;
    }


    private List<StepEntry> createStepEntriesForPM() {
        return List.of(
                createStepEntry(1L, "no-reply@gepardec.com", "008"),
                createStepEntry(2L, "no-reply@gepardec.com", "010"),
                createStepEntry(3L, "no-reply@gepardec.com", "012"),
                createStepEntry(4L, "no-reply@gepardec.com", "020")
        );
    }

    private StepEntry createStepEntry(Long id, String ownerEmail, String ownerZepId) {
        StepEntry entry = createStepEntry(id);
        User owner = new User();
        owner.setEmail(ownerEmail);
        owner.setZepId(ownerZepId);
        entry.setOwner(owner);
        return entry;
    }

    private StepEntry createStepEntry(Long id) {
        StepEntry stepEntry = new StepEntry();
        stepEntry.setId(id);
        stepEntry.setCreationDate(LocalDateTime.now());
        stepEntry.setDate(LocalDate.now());
        stepEntry.setProject("Liwest-EMS");
        stepEntry.setState(EmployeeState.IN_PROGRESS);
        stepEntry.setUpdatedDate(LocalDateTime.now());
        return stepEntry;
    }

    private Employee createEmployee() {
        return Employee.builder()
                .userId("1")
                .email("max.mustermann@gpeardec.com")
                .releaseDate(LocalDate.now().toString())
                .build();
    }
}

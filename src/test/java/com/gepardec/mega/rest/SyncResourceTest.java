package com.gepardec.mega.rest;

import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.domain.model.Employee;


import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.rest.api.SyncResource;
import com.gepardec.mega.rest.model.EmployeeDto;
import com.gepardec.mega.service.api.EmployeeService;

import com.gepardec.mega.service.api.StepEntryService;
import com.gepardec.mega.zep.ZepService;
import de.provantis.zep.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class SyncResourceTest {
    @InjectMock
    EmployeeService employeeService;


    @InjectMock
    ZepService zepService;

    @InjectMock
    StepEntryService stepEntryService;


    @Inject
    SyncResource syncResource;



    @Test
    void testUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth_whenEmployeeHasNoTimesAndAllAbsences_thenSetStepStateDone() {
        Employee userUnderTest = createEmployeeForId("099-testUser", "test.user@gepardec.com", "2024-02-29");
        when(employeeService.getAllActiveEmployees())
                .thenReturn(
                            List.of(
                                    createEmployeeForId("e02-testExternal", "external.user@gepardec.com", "2024-02-29"),
                                    userUnderTest,
                                    createEmployeeForId("100-testUser2", "test.user2@gepardec.com", "2024-02-29")
                            )
                );


        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);
        List<FehlzeitType> fehlzeitList = createFehlzeitTypeListForUser(
                "099-testUser",
                new AbsenceEntry(firstOfPreviousMonth.toString(), DateUtils.getLastDayOfMonth(firstOfPreviousMonth.getYear(), firstOfPreviousMonth.getMonth().getValue()).toString(), AbsenceType.PAID_SICK_LEAVE.getAbsenceName())
        );

        when(zepService.getAbsenceForEmployee(eq(userUnderTest), any(LocalDate.class)))
                .thenReturn(fehlzeitList);


        when(zepService.getEmployee(eq(userUnderTest.getUserId())))
                .thenReturn(userUnderTest);

        ArgumentCaptor<Employee> employeeArgumentCaptor = ArgumentCaptor.forClass(Employee.class);
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<LocalDate> localStartDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> localEndDateCaptor = ArgumentCaptor.forClass(LocalDate.class);

        when(stepEntryService.setOpenAndAssignedStepEntriesDone(
                employeeArgumentCaptor.capture(),
                longArgumentCaptor.capture(),
                localStartDateCaptor.capture(),
                localEndDateCaptor.capture()
                )
            )
            .thenReturn(true);

        when(stepEntryService.findStepEntryForEmployeeAtStep(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(createStepEntry());

        List<EmployeeDto> actual = syncResource.updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();

        assertThat(actual).isNotNull().size().isEqualTo(1);
        assertThat(employeeArgumentCaptor.getValue()).isEqualTo(userUnderTest);
        assertThat(longArgumentCaptor.getValue()).isEqualTo(1L);
        assertThat(localStartDateCaptor.getValue()).isEqualTo(firstOfPreviousMonth);
        assertThat(localEndDateCaptor.getValue()).isEqualTo(DateUtils.getLastDayOfMonth(firstOfPreviousMonth.getYear(), firstOfPreviousMonth.getMonth().getValue()));
        assertThat(actual.get(0).getUserId()).isEqualTo(userUnderTest.getUserId());
    }



    @Test
    void testUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth_whenEmployeeHasNoTimesAndAllAbsencesWithHomeOfficeAndVacation_thenReturnEmptyList(){
        Employee userUnderTest = createEmployeeForId("099-testUser", "test.user@gepardec.com", "2024-02-29");
        when(employeeService.getAllActiveEmployees())
                .thenReturn(
                            List.of(
                                    createEmployeeForId("e02-externalUser", "external.user@gepardec.com", "2024-02-29"),
                                    userUnderTest,
                                    createEmployeeForId("100-testUser2", "test.user2@gepardec.com", "2024-02-29")
                            )
                );


        List<FehlzeitType> fehlzeitList = createFehlzeitTypeListForUser(
                "099-testUser",
                new AbsenceEntry("2024-03-01", "2024-03-01", AbsenceType.PAID_SICK_LEAVE.getAbsenceName()),
                new AbsenceEntry("2024-03-04", "2024-03-08", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-11", "2024-03-15", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-18", "2024-03-22", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-25", "2024-03-29", AbsenceType.HOME_OFFICE_DAYS.getAbsenceName())
        );

        when(zepService.getAbsenceForEmployee(eq(userUnderTest), any(LocalDate.class)))
                .thenReturn(fehlzeitList);


        List<EmployeeDto> actual = syncResource.updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();

        assertThat(actual).isEmpty();
    }



    @Test
    void testUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth_whenEmployeeHasNoTimesAndSomeAbsences_thenReturnEmptyList(){
        Employee userUnderTest = createEmployeeForId("099-testUser", "test.user@gepardec.com", "2024-02-29");
        when(employeeService.getAllActiveEmployees())
                .thenReturn(
                            List.of(
                                    createEmployeeForId("e02-externalUser", "external.user@gepardec.com", "2024-02-29"),
                                    createEmployeeForId("100-testUser2", "test.user2@gepardec.com", "2024-02-29"),
                                    userUnderTest
                            )
                );


        List<FehlzeitType> fehlzeitList = createFehlzeitTypeListForUser(
                "099-testuser",
                new AbsenceEntry("2024-03-01", "2024-03-01", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-18", "2024-03-22", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-25", "2024-03-29", AbsenceType.HOME_OFFICE_DAYS.getAbsenceName())
        );

        Mockito.when(zepService.getAbsenceForEmployee(eq(userUnderTest), any(LocalDate.class)))
                .thenReturn(fehlzeitList);


        when(zepService.getEmployee(eq(userUnderTest.getUserId())))
                .thenReturn(createEmployeeForId("099-testUser", "test.user@gepardec.com", "2024-02-29"));

        List<EmployeeDto> actual = syncResource.updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();

        assertThat(actual).isEmpty();
    }

    @Test
    void testUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth_whenEmployeeIsExternal_thenReturnEmptyList() {
        Employee userUnderTest =  createEmployeeForId("e02-externalUser", "external.user@gepardec.com", "2024-02-29");
        when(employeeService.getAllActiveEmployees())
                .thenReturn(
                            List.of(
                                    userUnderTest,
                                    createEmployeeForId("099-testUser", "test.user@gepardec.com","2024-02-29"),
                                    createEmployeeForId("100-testUser2", "test.user2@gepardec.com", "2024-02-29")
                            )
                );


        List<FehlzeitType> fehlzeitList = createFehlzeitTypeListForUser(
                "e02-externalUser",
                new AbsenceEntry("2024-03-01", "2024-03-01", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-04", "2024-03-08", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-11", "2024-03-15", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-18", "2024-03-22", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-25", "2024-03-29", AbsenceType.VACATION_DAYS.getAbsenceName())
        );

        when(zepService.getAbsenceForEmployee(eq(userUnderTest), any(LocalDate.class)))
                .thenReturn(fehlzeitList);

        doNothing().when(zepService).updateEmployeesReleaseDate(anyString(), anyString());

        when(zepService.getEmployee(eq(userUnderTest.getUserId())))
                .thenReturn(createEmployeeForId("e02-externalUser", "external.user@gepardec.com", "2024-02-29"));

        List<EmployeeDto> actual = syncResource.updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();

        assertThat(actual).isEmpty();
    }

    //helpers
    private Employee createEmployeeForId(final String id, final String email, final String releaseDate){
        return Employee.builder()
                .userId(id)
                .email(email)
                .releaseDate(releaseDate)
                .active(true)
                .build();
    }


    private static FehlzeitType createFehlzeitTypeForUser(final String userId, final String startDate, final String endDate, final String reason){
        FehlzeitType fehlzeitType = new FehlzeitType();
        fehlzeitType.setUserId(userId);
        fehlzeitType.setStartdatum(startDate);
        fehlzeitType.setEnddatum(endDate);
        fehlzeitType.setFehlgrund(reason);
        return fehlzeitType;
    }

    private static StepEntry createStepEntry(){
        StepEntry entry = new StepEntry();
        entry.setState(EmployeeState.OPEN);
        return entry;
    }

    //helper class for test classes above to reduce loc
    static class AbsenceEntry {
        String startDate;
        String endDate;
        String reason;

        public AbsenceEntry(String startDate, String endDate, String reason) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.reason = reason;
        }
    }

    private static List<FehlzeitType> createFehlzeitTypeListForUser(String userId, AbsenceEntry... entries) {
        List<FehlzeitType> fehlzeitTypeList = new ArrayList<>();
        for (AbsenceEntry entry : entries) {
            fehlzeitTypeList.add(createFehlzeitTypeForUser(userId, entry.startDate, entry.endDate, entry.reason));
        }
        return fehlzeitTypeList;
    }
}

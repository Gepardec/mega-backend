package com.gepardec.mega.rest;

import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.domain.model.Employee;


import com.gepardec.mega.rest.api.SyncResource;
import com.gepardec.mega.rest.model.EmployeeDto;
import com.gepardec.mega.service.api.EmployeeService;

import com.gepardec.mega.zep.ZepService;
import de.provantis.zep.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
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


    @Inject
    SyncResource syncResource;

    @Test
    void testUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth_whenEmployeeHasNoTimesAndAllAbsences_thenSetReleaseDateAndReturnUpdatedEmployee() {
        Employee userUnderTest = createEmployeeForId("039-cgattringer", "chiara.gattringer@gepardec.com", "2024-02-29");
        when(employeeService.getAllActiveEmployees())
                .thenReturn(
                            List.of(
                                    createEmployeeForId("e02-oseimel", "oliver.seimel@gepardec.com", "2024-02-29"),
                                    userUnderTest,
                                    createEmployeeForId("026-cruhsam", "christoph.ruhsam@gepardec.com", "2024-02-29")
                            )
                );


        List<FehlzeitType> fehlzeitList = createFehlzeitTypeListForUser(
                "039-cgattringer",
                new AbsenceEntry("2024-03-01", "2024-03-01", AbsenceType.PAID_SICK_LEAVE.getAbsenceName()),
                new AbsenceEntry("2024-03-04", "2024-03-08", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-11", "2024-03-15", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-18", "2024-03-22", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-25", "2024-03-29", AbsenceType.VACATION_DAYS.getAbsenceName())
        );

        when(zepService.getAbsenceForEmployee(eq(userUnderTest), any(LocalDate.class)))
                .thenReturn(fehlzeitList);

        doNothing().when(zepService).updateEmployeesReleaseDate(anyString(), anyString());

        var updatedEmployee = createEmployeeForId("039-cgattringer", "chiara.gattringer@gepardec.com", "2024-03-31");
        when(zepService.getEmployee(eq(userUnderTest.getUserId())))
                .thenReturn(updatedEmployee);

        List<EmployeeDto> actual = syncResource.updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();

        assertThat(actual).isNotNull().size().isEqualTo(1);
        assertThat(actual.get(0).getUserId()).isEqualTo(userUnderTest.getUserId());
        assertThat(updatedEmployee.getReleaseDate()).isEqualTo("2024-03-31");

    }

    @Test
    void testUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth_whenEmployeeHasNoTimesAndAllAbsencesWithHomeOfficeAndVacation_thenReturnEmptyList(){
        Employee userUnderTest = createEmployeeForId("039-cgattringer", "chiara.gattringer@gepardec.com", "2024-02-29");
        when(employeeService.getAllActiveEmployees())
                .thenReturn(
                            List.of(
                                    createEmployeeForId("e02-oseimel", "oliver.seimel@gepardec.com", "2024-02-29"),
                                    userUnderTest,
                                    createEmployeeForId("026-cruhsam", "christoph.ruhsam@gepardec.com", "2024-02-29")
                            )
                );


        List<FehlzeitType> fehlzeitList = createFehlzeitTypeListForUser(
                "039-cgattringer",
                new AbsenceEntry("2024-03-01", "2024-03-01", AbsenceType.PAID_SICK_LEAVE.getAbsenceName()),
                new AbsenceEntry("2024-03-04", "2024-03-08", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-11", "2024-03-15", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-18", "2024-03-22", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-25", "2024-03-29", AbsenceType.HOME_OFFICE_DAYS.getAbsenceName())
        );

        when(zepService.getAbsenceForEmployee(eq(userUnderTest), any(LocalDate.class)))
                .thenReturn(fehlzeitList);

        doNothing().when(zepService).updateEmployeesReleaseDate(anyString(), anyString());

        when(zepService.getEmployee(eq(userUnderTest.getUserId())))
                .thenReturn(createEmployeeForId("039-cgattringer", "chiara.gattringer@gepardec.com", "2024-02-29"));

        List<EmployeeDto> actual = syncResource.updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();

        assertThat(actual).isEmpty();

    }

    @Test
    void testUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth_whenEmployeeHasNoTimesAndSomeAbsences_thenReturnEmptyList(){
        Employee userUnderTest = createEmployeeForId("026-cruhsam", "christoph.ruhsam@gepardec.com", "2024-02-29");
        when(employeeService.getAllActiveEmployees())
                .thenReturn(
                            List.of(
                                    createEmployeeForId("e02-oseimel", "oliver.seimel@gepardec.com", "2024-02-29"),
                                    createEmployeeForId("039-cgattringer", "chiara.gattringer@gepardec.com", "2024-02-29"),
                                    userUnderTest
                            )
                );


        List<FehlzeitType> fehlzeitList = createFehlzeitTypeListForUser(
                "026-cruhsam",
                new AbsenceEntry("2024-03-01", "2024-03-01", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-18", "2024-03-22", AbsenceType.VACATION_DAYS.getAbsenceName()),
                new AbsenceEntry("2024-03-25", "2024-03-29", AbsenceType.HOME_OFFICE_DAYS.getAbsenceName())
        );

        Mockito.when(zepService.getAbsenceForEmployee(eq(userUnderTest), any(LocalDate.class)))
                .thenReturn(fehlzeitList);

        doNothing().when(zepService).updateEmployeesReleaseDate(anyString(), anyString());

        when(zepService.getEmployee(eq(userUnderTest.getUserId())))
                .thenReturn(createEmployeeForId("026-cruhsam", "christoph.ruhsam@gepardec.com", "2024-02-29"));

        List<EmployeeDto> actual = syncResource.updateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();

        assertThat(actual).isEmpty();

    }

    @Test
    void testUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth_whenEmployeeIsExternal_thenReturnEmptyList() {
        Employee userUnderTest =  createEmployeeForId("e02-oseimel", "oliver.seimel@gepardec.com", "2024-02-29");
        when(employeeService.getAllActiveEmployees())
                .thenReturn(
                            List.of(
                                    userUnderTest,
                                    createEmployeeForId("039-cgattringer", "chiara.gattringer@gepardec.com","2024-02-29"),
                                    createEmployeeForId("026-cruhsam", "christoph.ruhsam@gepardec.com", "2024-02-29")
                            )
                );


        List<FehlzeitType> fehlzeitList = createFehlzeitTypeListForUser(
                "e02-oseimel",
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
                .thenReturn(createEmployeeForId("e02-oseimel", "oliver.seimel@gepardec.com", "2024-02-29"));

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

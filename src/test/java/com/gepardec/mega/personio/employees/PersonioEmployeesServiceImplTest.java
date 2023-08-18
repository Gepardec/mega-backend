package com.gepardec.mega.personio.employees;

import com.gepardec.mega.personio.commons.model.Attribute;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
class PersonioEmployeesServiceImplTest {

    @Inject
    PersonioEmployeesService personioEmployeesService;

    @InjectMock
    @RestClient
    PersonioEmployeesClient personioEmployeesClient;

    @Test
    void getVacationDayBalance_ValidResponse_Ten() {
        //GIVEN
        var employeesResponse = new EmployeesResponse();
        employeesResponse.setSuccess(true);
        employeesResponse.setData(createValidEmployeesResponseData());

        when(personioEmployeesClient.getByEmail(anyString())).thenReturn(employeesResponse);

        //WHEN
        var result = personioEmployeesService.getVacationDayBalance("mega.test@gepardec.com");

        //THEN
        assertThat(result).isEqualTo(10d);
    }

    @Test
    void getVacationDayBalance_InvalidResponseWithTwoEmployees_Zero() {
        //GIVEN
        var employeesResponse = new EmployeesResponse();
        employeesResponse.setSuccess(true);
        employeesResponse.setData(createInvalidEmployeesResponseData());

        when(personioEmployeesClient.getByEmail(anyString())).thenReturn(employeesResponse);

        //WHEN
        var result = personioEmployeesService.getVacationDayBalance("mega.test@gepardec.com");

        //THEN
        assertThat(result).isEqualTo(0d);
    }

    @Test
    void getVacationDayBalance_NotSuccessful_Zero() {
        //GIVEN
        var employeesResponse = new EmployeesResponse();
        employeesResponse.setSuccess(false);

        when(personioEmployeesClient.getByEmail(anyString())).thenReturn(employeesResponse);

        //WHEN
        var result = personioEmployeesService.getVacationDayBalance("mega.test@gepardec.com");

        //THEN
        assertThat(result).isEqualTo(0d);
    }

    private static List<EmployeesResponseData> createValidEmployeesResponseData() {
        var data = new EmployeesResponseData();
        data.setAttributes(createPersonioEmployee());

        return List.of(data);
    }

    private static List<EmployeesResponseData> createInvalidEmployeesResponseData() {
        var data1 = new EmployeesResponseData();
        data1.setAttributes(createPersonioEmployee());

        var data2 = new EmployeesResponseData();
        data2.setAttributes(createPersonioEmployee());

        return List.of(data1, data2);
    }

    private static PersonioEmployee createPersonioEmployee() {
        var vacationDayBalance = new Attribute<Double>();
        vacationDayBalance.setValue(10d);

        var employee = new PersonioEmployee();
        employee.setVacationDayBalance(vacationDayBalance);

        return employee;
    }
}

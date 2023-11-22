package com.gepardec.mega.personio.employees;

import com.gepardec.mega.personio.commons.model.Attribute;
import com.gepardec.mega.personio.commons.model.BaseResponse;
import com.gepardec.mega.personio.commons.model.ErrorResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class PersonioEmployeesServiceImplTest {

    @Inject
    PersonioEmployeesServiceImpl personioEmployeesService;

    @InjectMock
    @RestClient
    PersonioEmployeesClient personioEmployeesClient;

    @InjectMock
    Logger logger;


    @Test
    void getVacationDayBalance_ValidResponse_Ten() {
        //GIVEN
        var employeesResponse = new BaseResponse<List<EmployeesResponse>>();
        employeesResponse.setSuccess(true);
        employeesResponse.setData(createValidEmployeesResponseData());

        var response = Response.ok(employeesResponse).build();

        when(personioEmployeesClient.getByEmail(anyString())).thenReturn(response);

        //WHEN
        var result = personioEmployeesService.getVacationDayBalance("mega.test@gepardec.com");

        //THEN
        assertThat(result).isEqualTo(10d);
    }

    @Test
    void getVacationDayBalance_InvalidResponseWithTwoEmployees_Zero() {
        //GIVEN
        var employeesResponse = new BaseResponse<List<EmployeesResponse>>();
        employeesResponse.setSuccess(true);
        employeesResponse.setData(createInvalidEmployeesResponseData());

        var response = Response.ok(employeesResponse).build();

        when(personioEmployeesClient.getByEmail(anyString())).thenReturn(response);

        //WHEN
        var result = personioEmployeesService.getVacationDayBalance("mega.test@gepardec.com");

        //THEN
        assertThat(result).isEqualTo(0d);
    }

    @Test
    void getVacationDayBalance_NotSuccessful_Zero() {
        //GIVEN
        var employeesResponse = new BaseResponse<List<EmployeesResponse>>();
        employeesResponse.setSuccess(false);
        employeesResponse.setError(createErrorResponse());

        var response = Response.status(404).entity(employeesResponse).build();

        when(personioEmployeesClient.getByEmail(anyString())).thenReturn(response);

        //WHEN
        var result = personioEmployeesService.getVacationDayBalance("mega.test@gepardec.com");

        //THEN
        verify(logger).info("Fehler bei Aufruf der Personio-Schnittstelle: {}", "Personio-Fehler");

        assertThat(result).isEqualTo(0d);
    }

    private static List<EmployeesResponse> createValidEmployeesResponseData() {
        var data = new EmployeesResponse();
        data.setAttributes(createPersonioEmployee());

        return List.of(data);
    }

    private static List<EmployeesResponse> createInvalidEmployeesResponseData() {
        var data1 = new EmployeesResponse();
        data1.setAttributes(createPersonioEmployee());

        var data2 = new EmployeesResponse();
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

    private static ErrorResponse createErrorResponse() {
        var errorResponse = new ErrorResponse();
        errorResponse.setMessage("Personio-Fehler");

        return errorResponse;
    }
}

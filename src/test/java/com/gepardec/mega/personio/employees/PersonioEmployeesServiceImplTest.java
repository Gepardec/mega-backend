package com.gepardec.mega.personio.employees;

import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.personio.commons.constants.AbsenceConstants;
import com.gepardec.mega.personio.commons.model.Attribute;
import com.gepardec.mega.personio.commons.model.BaseResponse;
import com.gepardec.mega.personio.commons.model.ErrorResponse;
import com.gepardec.mega.personio.employees.absenceBalance.AbsenceBalanceResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
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
        var result = personioEmployeesService.getPersonioEmployeeByEmail("mega.test@gepardec.com");

        //THEN
        assertThat(result).isNotEmpty();
        PersonioEmployee personioEmployee = result.get();
        assertThat(personioEmployee.getGuildLead()).isEqualTo("guildLead");
        assertThat(personioEmployee.getInternalProjectLead()).isEqualTo("internalProjectLead");
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
        var result = personioEmployeesService.getPersonioEmployeeByEmail(("mega.test@gepardec.com"));

        //THEN
        assertThat(result).isEmpty();
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
        var result = personioEmployeesService.getPersonioEmployeeByEmail("mega.test@gepardec.com");

        //THEN
        verify(logger).info("Fehler bei Aufruf der Personio-Schnittstelle: {}", "Personio-Fehler");

        assertThat(result).isEmpty();
    }

    @Test
    void getAvailableVacationDaysForEmployeeByEmail_whenDaysAvailable_thenReturnDaysCount() {
        var employeesResponse = new BaseResponse<List<EmployeesResponse>>();
        employeesResponse.setSuccess(true);
        employeesResponse.setData(createValidEmployeesResponseData());

        var responseEmployee = Response.ok(employeesResponse).build();

        when(personioEmployeesClient.getByEmail(anyString()))
                .thenReturn(responseEmployee);

        var absenceBalanceResponse = new BaseResponse<List<AbsenceBalanceResponse>>();
        absenceBalanceResponse.setSuccess(true);
        absenceBalanceResponse.setData(createValidAbsenceBalanceResponseData());

        var responseAbsence = Response.ok(absenceBalanceResponse).build();

        when(personioEmployeesClient.getAbsenceBalanceForEmployeeById(anyInt()))
                .thenReturn(responseAbsence);

        var result = personioEmployeesService.getAvailableVacationDaysForEmployeeByEmail("mega.test@gepardec.com");
        assertThat(result).isEqualTo(createValidAbsenceBalanceResponseData().get(0).getAvailableBalance());
    }

    @Test
    void getAvailableVacationDaysForEmployeeByEmail_whenNotSuccessful_thenReturnZero() {
        var employeesResponse = new BaseResponse<List<EmployeesResponse>>();
        employeesResponse.setSuccess(true);
        employeesResponse.setData(createValidEmployeesResponseData());

        var responseEmployee = Response.ok(employeesResponse).build();


        var absenceBalanceResponse = new BaseResponse<List<AbsenceBalanceResponse>>();
        absenceBalanceResponse.setSuccess(false);
        absenceBalanceResponse.setError(createErrorResponse());

        var responseAbsence = Response.status(Response.Status.NOT_FOUND).entity(absenceBalanceResponse).build();

        when(personioEmployeesClient.getByEmail(anyString()))
                .thenReturn(responseEmployee);

        when(personioEmployeesClient.getAbsenceBalanceForEmployeeById(anyInt()))
                .thenReturn(responseAbsence);


        var result = personioEmployeesService.getAvailableVacationDaysForEmployeeByEmail("mega.test@gepardec.com");

        verify(logger).info("Fehler bei Aufruf der Personio-Schnittstelle: {}", "Personio-Fehler");
        assertThat(result).isZero();
    }

    private static List<EmployeesResponse> createValidEmployeesResponseData() {
        var data = new EmployeesResponse();
        data.setAttributes(createPersonioEmployee());

        return List.of(data);
    }

    private static List<AbsenceBalanceResponse> createValidAbsenceBalanceResponseData() {
        var data = new AbsenceBalanceResponse(AbsenceConstants.PAID_VACATION_ID, "", List.of(), 2, 12);
        return List.of(data);
    }

    private static List<EmployeesResponse> createInvalidEmployeesResponseData() {
        var data1 = new EmployeesResponse();
        data1.setAttributes(createPersonioEmployee());

        var data2 = new EmployeesResponse();
        data2.setAttributes(createPersonioEmployee());

        return List.of(data1, data2);
    }

    private static PersonioEmployeeDto createPersonioEmployee() {
        return PersonioEmployeeDto.builder()
                .id(Attribute.ofValue(123))
                .guildLead(Attribute.ofValue("guildLead"))
                .internalProjectLead(Attribute.ofValue("internalProjectLead"))
                .build();
    }

    private static ErrorResponse createErrorResponse() {
        var errorResponse = new ErrorResponse();
        errorResponse.setMessage("Personio-Fehler");

        return errorResponse;
    }
}

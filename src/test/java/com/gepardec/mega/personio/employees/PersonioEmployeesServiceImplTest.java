package com.gepardec.mega.personio.employees;

import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.personio.commons.constants.AbsenceConstants;
import com.gepardec.mega.personio.commons.model.Attribute;
import com.gepardec.mega.personio.commons.model.BaseResponse;
import com.gepardec.mega.personio.commons.model.ErrorResponse;
import com.gepardec.mega.personio.employees.absenceBalance.AbsenceBalanceResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
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
    void getPersonioEmployeeByEmail_ValidResponse_Ten() {
        //GIVEN
        var employeesResponse = new BaseResponse<List<EmployeesResponse>>();
        employeesResponse.setSuccess(true);
        employeesResponse.setData(createValidEmployeesResponseData());

        when(personioEmployeesClient.getByEmail(anyString())).thenReturn(employeesResponse);

        //WHEN
        var result = personioEmployeesService.getPersonioEmployeeByEmail("mega.test@gepardec.com");

        //THEN
        assertThat(result).isNotEmpty();
        PersonioEmployee personioEmployee = result.get();
        assertThat(personioEmployee.getGuildLead()).isEqualTo("guildLead");
        assertThat(personioEmployee.getInternalProjectLead()).isEqualTo("internalProjectLead");
    }

    @Test
    void getPersonioEmployeeByEmail_InvalidResponseWithTwoEmployees_Zero() {
        //GIVEN
        var employeesResponse = new BaseResponse<List<EmployeesResponse>>();
        employeesResponse.setSuccess(true);
        employeesResponse.setData(createInvalidEmployeesResponseData());

        when(personioEmployeesClient.getByEmail(anyString())).thenReturn(employeesResponse);

        //WHEN
        var result = personioEmployeesService.getPersonioEmployeeByEmail(("mega.test@gepardec.com"));

        //THEN
        assertThat(result).isEmpty();
    }

    @Test
    void getPersonioEmployeeByEmail_NotSuccessful_Zero() {
        //GIVEN
        var employeesResponse = new BaseResponse<List<EmployeesResponse>>();
        employeesResponse.setSuccess(false);
        employeesResponse.setError(createErrorResponse());

        when(personioEmployeesClient.getByEmail(anyString())).thenReturn(employeesResponse);

        //WHEN
        var result = personioEmployeesService.getPersonioEmployeeByEmail("mega.test@gepardec.com");

        //THEN
        verify(logger).info("Fehler bei Aufruf der Personio-Schnittstelle: {}", "Personio-Fehler");
        assertThat(result).isEmpty();
    }

    @Test
    void getAvailableVacationDaysForEmployeeByEmail_whenDaysAvailable_thenReturnDaysCount() {
        //GIVEN
        var employeesResponse = new BaseResponse<List<EmployeesResponse>>();
        employeesResponse.setSuccess(true);
        employeesResponse.setData(createValidEmployeesResponseData());

        when(personioEmployeesClient.getByEmail(anyString()))
                .thenReturn(employeesResponse);

        var absenceBalanceResponse = new BaseResponse<List<AbsenceBalanceResponse>>();
        absenceBalanceResponse.setSuccess(true);
        absenceBalanceResponse.setData(createValidAbsenceBalanceResponseData());

        when(personioEmployeesClient.getAbsenceBalanceForEmployeeById(anyInt()))
                .thenReturn(absenceBalanceResponse);

        //WHEN
        var result = personioEmployeesService.getAvailableVacationDaysForEmployeeByEmail("mega.test@gepardec.com");

        //THEN
        assertThat(result).isEqualTo(createValidAbsenceBalanceResponseData().get(0).getAvailableBalance());
    }

    @Test
    void getAvailableVacationDaysForEmployeeByEmail_whenNotSuccessful_thenReturnZero() {
        //GIVEN
        var employeesResponse = new BaseResponse<List<EmployeesResponse>>();
        employeesResponse.setSuccess(true);
        employeesResponse.setData(createValidEmployeesResponseData());

        var absenceBalanceResponse = new BaseResponse<List<AbsenceBalanceResponse>>();
        absenceBalanceResponse.setSuccess(false);
        absenceBalanceResponse.setError(createErrorResponse());

        when(personioEmployeesClient.getByEmail(anyString()))
                .thenReturn(employeesResponse);

        when(personioEmployeesClient.getAbsenceBalanceForEmployeeById(anyInt()))
                .thenReturn(absenceBalanceResponse);

        //WHEN
        var result = personioEmployeesService.getAvailableVacationDaysForEmployeeByEmail("mega.test@gepardec.com");

        //THEN
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
                .hasCreditCard(Attribute.ofValue("Ja"))
                .build();
    }

    private static ErrorResponse createErrorResponse() {
        var errorResponse = new ErrorResponse();
        errorResponse.setMessage("Personio-Fehler");

        return errorResponse;
    }
}

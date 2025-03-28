package com.gepardec.mega.personio.employees;

import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.personio.commons.constants.AbsenceConstants;
import com.gepardec.mega.personio.commons.model.BaseResponse;
import com.gepardec.mega.personio.commons.model.ErrorResponse;
import com.gepardec.mega.personio.employees.absenceBalance.AbsenceBalanceResponse;
import com.gepardec.mega.rest.mapper.PersonioEmployeeMapper;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

@RequestScoped
public class PersonioEmployeesServiceImpl implements PersonioEmployeesService {

    @Inject
    @RestClient
    PersonioEmployeesClient personioEmployeesClient;

    @Inject
    Logger logger;

    @Inject
    PersonioEmployeeMapper mapper;

    public Optional<PersonioEmployee> getPersonioEmployeeByEmail(String email) {
        return getPersonioEmployeeDtoByEmail(email).map(mapper::mapToDomain);
    }

    public int getAvailableVacationDaysForEmployeeByEmail(String email) {
        Optional<PersonioEmployeeDto> dto = getPersonioEmployeeDtoByEmail(email);

        if (dto.isEmpty()) {
            return 0;
        }

        var absenceBalanceResponse = personioEmployeesClient.getAbsenceBalanceForEmployeeById(dto.get().id().getValue());
        if (!absenceBalanceResponse.isSuccess()) {
            logError(absenceBalanceResponse.getError());
            return 0;
        }

        return absenceBalanceResponse.getData()
                .stream()
                .filter(absenceBalanceObject -> absenceBalanceObject.getId().equals(AbsenceConstants.PAID_VACATION_ID)) // only paid vacation (id = 104066) is relevant in this case
                .findFirst() // there is only one
                .map(AbsenceBalanceResponse::getAvailableBalance)
                .orElse(0);
    }

    private Optional<PersonioEmployeeDto> getPersonioEmployeeDtoByEmail(String email) {
        try {
            BaseResponse<List<EmployeesResponse>> employeesResponse = personioEmployeesClient.getByEmail(email);
            if (employeesResponse.isSuccess() && employeesResponse.getData().size() == 1) {
                return Optional.ofNullable(employeesResponse.getData().get(0).getAttributes());
            }
            return Optional.empty();
        } catch (WebApplicationException e) {
            logError(e.getResponse().readEntity(BaseResponse.class).getError());
            return Optional.empty();
        }
    }

    private void logError(ErrorResponse absenceBalanceResponse) {
        logger.info("Fehler bei Aufruf der Personio-Schnittstelle: {}", absenceBalanceResponse.getMessage());
    }
}

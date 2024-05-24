package com.gepardec.mega.personio.employees;
import com.gepardec.mega.personio.commons.constants.AbsenceConstants;
import com.gepardec.mega.personio.employees.absenceBalance.AbsenceBalanceResponse;
import com.gepardec.mega.rest.mapper.PersonioEmployeeMapper;
import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.personio.commons.model.BaseResponse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;
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
        Optional<PersonioEmployeeDto> dto = getPersonioEmployeeDtoByEmail(email);
        return dto.map(mapper::mapToDomain);
    }

    public int getAvailableVacationDaysForEmployeeByEmail(String email) {
        Optional<PersonioEmployeeDto> dto = getPersonioEmployeeDtoByEmail(email);
        int id;

        if (dto.isPresent()) {
            id = dto.get().id().getValue();
            var response = personioEmployeesClient.getAbsenceBalanceForEmployeeById(id);
            var absenceBalanceResponse = response.readEntity(new GenericType<BaseResponse<List<AbsenceBalanceResponse>>>() {
            });
            if (absenceBalanceResponse.isSuccess()) {
                var absenceBalanceResponseDataForVacation = absenceBalanceResponse.getData()
                        .stream()
                        .filter(absenceBalanceObject -> absenceBalanceObject.getId().equals(AbsenceConstants.PAID_VACATION_ID)) // only paid vacation (id = 104066) is relevant in this case
                        .findFirst(); // there is only one

                if (absenceBalanceResponseDataForVacation.isPresent()) {
                    return absenceBalanceResponseDataForVacation.get().getAvailableBalance();
                }
            } else {
                logger.info("Fehler bei Aufruf der Personio-Schnittstelle: {}", absenceBalanceResponse.getError().getMessage());
            }
        }
        return 0;
    }

    private Optional<PersonioEmployeeDto> getPersonioEmployeeDtoByEmail(String email) {
        var response = personioEmployeesClient.getByEmail(email);
        var employeesResponse = response.readEntity(new GenericType<BaseResponse<List<EmployeesResponse>>>() {
        });
        if (employeesResponse.isSuccess()) {
            if (employeesResponse.getData().size() == 1) {
                return Optional.ofNullable(employeesResponse.getData().get(0).getAttributes());
            }
        } else {
            logger.info("Fehler bei Aufruf der Personio-Schnittstelle: {}", employeesResponse.getError().getMessage());
        }
        return Optional.empty();
    }
}

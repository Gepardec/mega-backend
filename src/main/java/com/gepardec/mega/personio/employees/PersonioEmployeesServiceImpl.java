package com.gepardec.mega.personio.employees;

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
        var response = personioEmployeesClient.getByEmail(email);
        var employeesResponse = response.readEntity(new GenericType<BaseResponse<List<EmployeesResponse>>>() {
        });
        if (employeesResponse.isSuccess()) {
            if (employeesResponse.getData().size() == 1) {
                PersonioEmployeeDto dto = employeesResponse.getData().get(0).getAttributes();
                return Optional.ofNullable(mapper.mapToDomain(dto));
            }
        } else {
            logger.info("Fehler bei Aufruf der Personio-Schnittstelle: {}", employeesResponse.getError().getMessage());
        }
        return Optional.empty();
    }
}

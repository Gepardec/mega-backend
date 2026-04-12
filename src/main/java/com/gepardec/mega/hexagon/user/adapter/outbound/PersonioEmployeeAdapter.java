package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.PersonioId;
import com.gepardec.mega.hexagon.user.domain.port.outbound.PersonioEmployeePort;
import com.gepardec.mega.personio.commons.model.BaseResponse;
import com.gepardec.mega.personio.employees.EmployeesResponse;
import com.gepardec.mega.personio.employees.PersonioEmployeeDto;
import com.gepardec.mega.personio.employees.PersonioEmployeesClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PersonioEmployeeAdapter implements PersonioEmployeePort {

    @Inject
    @RestClient
    PersonioEmployeesClient personioEmployeesClient;

    @Override
    public Optional<PersonioId> findPersonioIdByEmail(Email email) {
        return findEmployeeByEmail(email.value())
                .flatMap(dto -> dto.id() == null ? Optional.empty() : Optional.of(PersonioId.of(dto.id().getValue())));
    }

    private Optional<PersonioEmployeeDto> findEmployeeByEmail(String email) {
        try {
            BaseResponse<List<EmployeesResponse>> response = personioEmployeesClient.getByEmail(email);
            if (!response.isSuccess() || response.getData().size() != 1) {
                return Optional.empty();
            }

            PersonioEmployeeDto dto = response.getData().getFirst().getAttributes();
            return Optional.ofNullable(dto);
        } catch (WebApplicationException e) {
            return Optional.empty();
        }
    }
}

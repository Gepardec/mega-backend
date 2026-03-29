package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.PersonioProfile;
import com.gepardec.mega.hexagon.user.domain.port.outbound.PersonioEmployeePort;
import com.gepardec.mega.personio.commons.constants.AbsenceConstants;
import com.gepardec.mega.personio.commons.model.BaseResponse;
import com.gepardec.mega.personio.employees.EmployeesResponse;
import com.gepardec.mega.personio.employees.PersonioEmployeeDto;
import com.gepardec.mega.personio.employees.PersonioEmployeesClient;
import com.gepardec.mega.personio.employees.absencebalance.AbsenceBalanceResponse;
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
    public Optional<PersonioProfile> findByEmail(Email email) {
        try {
            BaseResponse<List<EmployeesResponse>> response = personioEmployeesClient.getByEmail(email.value());
            if (!response.isSuccess() || response.getData().size() != 1) {
                return Optional.empty();
            }

            PersonioEmployeeDto dto = response.getData().getFirst().getAttributes();
            if (dto == null) {
                return Optional.empty();
            }

            double vacationDayBalance = fetchVacationBalance(dto);

            return Optional.of(
                    new PersonioProfile(
                            dto.id() != null ? dto.id().getValue() : 0,
                            vacationDayBalance,
                            dto.guildLead() != null ? dto.guildLead().getValue() : null,
                            dto.internalProjectLead() != null ? dto.internalProjectLead().getValue() : null,
                            dto.hasCreditCard() != null && Boolean.parseBoolean(dto.hasCreditCard().getValue())
                    )
            );
        } catch (WebApplicationException e) {
            return Optional.empty();
        }
    }

    private double fetchVacationBalance(PersonioEmployeeDto dto) {
        if (dto.id() == null) {
            return 0;
        }
        try {
            BaseResponse<List<AbsenceBalanceResponse>> balanceResponse =
                    personioEmployeesClient.getAbsenceBalanceForEmployeeById(dto.id().getValue());
            if (!balanceResponse.isSuccess()) {
                return 0;
            }
            return balanceResponse.getData().stream()
                    .filter(b -> b.getId() != null && b.getId().equals(AbsenceConstants.PAID_VACATION_ID))
                    .findFirst()
                    .map(b -> b.getAvailableBalance() != null ? b.getAvailableBalance().doubleValue() : 0.0)
                    .orElse(0.0);
        } catch (WebApplicationException e) {
            return 0;
        }
    }
}

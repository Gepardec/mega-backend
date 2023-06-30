package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.RolesAllowed;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.rest.api.EnterpriseResource;
import com.gepardec.mega.rest.model.EnterpriseEntryDto;
import com.gepardec.mega.service.api.EnterpriseEntryService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@RequestScoped
@Authenticated
@RolesAllowed(value = {Role.PROJECT_LEAD, Role.OFFICE_MANAGEMENT})
public class EnterpriseResourceImpl implements EnterpriseResource {

    @Inject
    EnterpriseEntryService enterpriseEntryService;

    @Override
    public Response getEnterpriseEntryForMonthYear(Integer year, Integer month) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = LocalDate.of(year, month, 1).with(TemporalAdjusters.lastDayOfMonth());

        return Response.ok(enterpriseEntryService.findByDate(from, to)).build();
    }

    @Override
    public Response updateEnterpriseEntry(Integer year, Integer month, final EnterpriseEntryDto entryDto) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = LocalDate.of(year, month, 1).with(TemporalAdjusters.lastDayOfMonth());

        return Response.ok(enterpriseEntryService.update(entryDto, from, to)).build();
    }
}

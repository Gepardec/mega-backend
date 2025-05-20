package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.MegaRolesAllowed;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.rest.api.EnterpriseResource;
import com.gepardec.mega.rest.model.EnterpriseEntryDto;
import com.gepardec.mega.service.api.EnterpriseEntryService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.YearMonth;

@RequestScoped
@Authenticated
@MegaRolesAllowed(value = {Role.PROJECT_LEAD, Role.OFFICE_MANAGEMENT})
public class EnterpriseResourceImpl implements EnterpriseResource {

    @Inject
    EnterpriseEntryService enterpriseEntryService;

    @Override
    public Response getEnterpriseEntryForMonthYear(Integer year, Integer month) {
        return Response.ok(enterpriseEntryService.findByDate(YearMonth.of(year, month))).build();
    }

    @Override
    public Response updateEnterpriseEntry(Integer year, Integer month, final EnterpriseEntryDto entryDto) {
        return Response.ok(enterpriseEntryService.update(entryDto, YearMonth.of(year, month))).build();
    }
}

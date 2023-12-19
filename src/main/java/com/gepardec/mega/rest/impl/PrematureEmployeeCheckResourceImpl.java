package com.gepardec.mega.rest.impl;

import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.rest.api.PrematureEmployeeCheckResource;
import com.gepardec.mega.rest.mapper.PrematureEmployeeCheckMapper;
import com.gepardec.mega.rest.model.PrematureEmployeeCheckDto;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Authenticated
public class PrematureEmployeeCheckResourceImpl implements PrematureEmployeeCheckResource {

    @Inject
    PrematureEmployeeCheckService prematureEmployeeCheckService;

    @Inject
    PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;

    @Override
    public Response add(PrematureEmployeeCheckDto prematureEmployeeCheckDto) {
        PrematureEmployeeCheck prematureEmployeeCheck = prematureEmployeeCheckMapper.mapToDomain(prematureEmployeeCheckDto);

        boolean succeeded = prematureEmployeeCheckService.addPrematureEmployeeCheck(prematureEmployeeCheck);

        return Response.ok(succeeded).build();
    }
}

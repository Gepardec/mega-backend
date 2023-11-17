package com.gepardec.mega.rest.impl;

import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.rest.api.PrematureEmployeeCheckResource;
import com.gepardec.mega.rest.model.PrematureEmployeeCheckDto;
import com.gepardec.mega.rest.model.UserDto;
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

    @Override
    public Response add(PrematureEmployeeCheckDto prematureEmployeeCheckDto) {
        // TODO integrate with mapper

        UserDto userDto = prematureEmployeeCheckDto.getUser();


        PrematureEmployeeCheck prematureEmployeeCheck = PrematureEmployeeCheck.builder()
                .forMonth(prematureEmployeeCheckDto.getForMonth())
                .user(User.builder()
                        .userId(userDto.getUserId())
                        .email(userDto.getEmail())
                        .firstname(userDto.getFirstname())
                        .lastname(userDto.getLastname())
                        .dbId(userDto.getDbId())
                        .roles(userDto.getRoles())
                        .releaseDate(userDto.getReleaseDate()).build()).build();


        boolean suceeded = prematureEmployeeCheckService.addPrematureEmployeeCheck(prematureEmployeeCheck);

        return Response.ok(suceeded).build();
    }
}

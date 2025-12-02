package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.rest.model.PrematureEmployeeCheckDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PrematureEmployeeCheckMapper implements DtoMapper<PrematureEmployeeCheck, PrematureEmployeeCheckDto> {

    @Inject
    UserMapper userMapper;

    @Inject
    UserContext userContext;

    @Override
    public PrematureEmployeeCheckDto mapToDto(PrematureEmployeeCheck object) {
        return PrematureEmployeeCheckDto.builder()
                .id(object.getId())
                .user(userMapper.mapToDto(userContext.getUser()))
                .forMonth(object.getForMonth())
                .reason(object.getReason())
                .state(object.getState())
                .build();
    }

    @Override
    public PrematureEmployeeCheck mapToDomain(PrematureEmployeeCheckDto object) {
        return PrematureEmployeeCheck.builder()
                .id(object.getId())
                .user(userContext.getUser())
                .forMonth(object.getForMonth())
                .reason(object.getReason())
                .state(object.getState())
                .build();
    }
}

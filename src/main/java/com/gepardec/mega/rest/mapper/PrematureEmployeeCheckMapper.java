package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.rest.model.PrematureEmployeeCheckDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PrematureEmployeeCheckMapper implements DtoMapper<PrematureEmployeeCheck, PrematureEmployeeCheckDto> {

    @Inject
    UserMapper userMapper;

    @Override
    public PrematureEmployeeCheckDto mapToDto(PrematureEmployeeCheck object) {
        return PrematureEmployeeCheckDto.builder()
                .id(object.getId())
                .user(userMapper.mapToDto(object.getUser()))
                .forMonth(object.getForMonth())
                .reason(object.getReason())
                .state(object.getState())
                .build();
    }

    @Override
    public PrematureEmployeeCheck mapToDomain(PrematureEmployeeCheckDto object) {
        return PrematureEmployeeCheck.builder()
                .id(object.getId())
                .user(userMapper.mapToDomain(object.getUser()))
                .forMonth(object.getForMonth())
                .reason(object.getReason())
                .state(object.getState())
                .build();
    }
}

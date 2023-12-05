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
                .user(userMapper.mapToDto(object.getUser()))
                .forMonth(object.getForMonth())
                .reason(object.getReason())
                .build();
    }

    @Override
    public PrematureEmployeeCheck mapToDomain(PrematureEmployeeCheckDto object) {
        return PrematureEmployeeCheck.builder()
                .user(userMapper.mapToDomain(object.getUser()))
                .forMonth(object.getForMonth())
                .reason(object.getReason())
                .build();
    }
}

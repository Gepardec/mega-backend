package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.EmployeeCheck;
import com.gepardec.mega.rest.model.EmployeeCheckDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class EmployeeCheckMapper implements DtoMapper<EmployeeCheck, EmployeeCheckDto> {

    @Inject
    EmployeeMapper employeeMapper;

    @Inject
    CommentMapper commentMapper;

    @Inject
    PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;

    @Override
    public EmployeeCheckDto mapToDto(EmployeeCheck object) {
        return new EmployeeCheckDto(
                employeeMapper.mapToDto(object.employee()),
                object.employeeCheckState(),
                object.employeeCheckStateReason(),
                object.internalCheckState(),
                object.otherChecksDone(),
                commentMapper.mapListToDto(object.comments()),
                Optional.ofNullable(object.prematureEmployeeCheck())
                        .map(prematureEmployeeCheckMapper::mapToDto)
                        .orElse(null)
        );
    }

    @Override
    public EmployeeCheck mapToDomain(EmployeeCheckDto object) {
        return new EmployeeCheck(
                employeeMapper.mapToDomain(object.employee()),
                object.employeeCheckState(),
                object.employeeCheckStateReason(),
                object.internalCheckState(),
                object.otherChecksDone(),
                commentMapper.mapListToDomain(object.comments()),
                prematureEmployeeCheckMapper.mapToDomain(object.prematureEmployeeCheck())
        );
    }
}

package com.gepardec.mega.service.mapper;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PrematureEmployeeCheckMapper {

    @Inject
    UserMapper userMapper;

    public com.gepardec.mega.domain.model.PrematureEmployeeCheck mapToDomain(PrematureEmployeeCheck prematureEmployeeCheck) {
        return com.gepardec.mega.domain.model.PrematureEmployeeCheck.builder()
                .id(prematureEmployeeCheck.getId())
                .user(userMapper.map(prematureEmployeeCheck.getUser()))
                .forMonth(prematureEmployeeCheck.getForMonth())
                .creationDate(prematureEmployeeCheck.getCreationDate())
                .build();
    }

    public List<com.gepardec.mega.domain.model.PrematureEmployeeCheck> mapListToDomain(List<PrematureEmployeeCheck> prematureEmployeeChecks) {
        return prematureEmployeeChecks.stream().map(this::mapToDomain).collect(Collectors.toList());
    }
}

package com.gepardec.mega.service.mapper;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PrematureEmployeeCheckMapper {

    @Inject
    UserMapper userMapper;

    public PrematureEmployeeCheck mapToDomain(PrematureEmployeeCheckEntity prematureEmployeeCheckEntity) {
        return PrematureEmployeeCheck.builder()
                .id(prematureEmployeeCheckEntity.getId())
                .user(userMapper.map(prematureEmployeeCheckEntity.getUser()))
                .forMonth(prematureEmployeeCheckEntity.getForMonth())
                .reason(prematureEmployeeCheckEntity.getReason())
                .creationDate(prematureEmployeeCheckEntity.getCreationDate())
                .state(prematureEmployeeCheckEntity.getState())
                .build();
    }

    public List<PrematureEmployeeCheck> mapListToDomain(List<PrematureEmployeeCheckEntity> prematureEmployeeCheckEntities) {
        return prematureEmployeeCheckEntities.stream().map(this::mapToDomain).collect(Collectors.toList());
    }
}

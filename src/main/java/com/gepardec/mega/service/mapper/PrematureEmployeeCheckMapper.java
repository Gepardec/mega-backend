package com.gepardec.mega.service.mapper;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PrematureEmployeeCheckMapper {

    @Inject
    UserMapper userMapper;

    public com.gepardec.mega.domain.model.PrematureEmployeeCheck mapToDomain(PrematureEmployeeCheckEntity prematureEmployeeCheckEntity) {
        return com.gepardec.mega.domain.model.PrematureEmployeeCheck.builder()
                .id(prematureEmployeeCheckEntity.getId())
                .user(userMapper.map(prematureEmployeeCheckEntity.getUser()))
                .forMonth(prematureEmployeeCheckEntity.getForMonth())
                .creationDate(prematureEmployeeCheckEntity.getCreationDate())
                .build();
    }

    public List<com.gepardec.mega.domain.model.PrematureEmployeeCheck> mapListToDomain(List<PrematureEmployeeCheckEntity> prematureEmployeeCheckEntities) {
        return prematureEmployeeCheckEntities.stream().map(this::mapToDomain).collect(Collectors.toList());
    }
}

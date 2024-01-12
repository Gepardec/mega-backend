package com.gepardec.mega.domain.mapper;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PrematureEmployeeCheckMapper implements DomainMapper<PrematureEmployeeCheck, PrematureEmployeeCheckEntity> {

    @Inject
    UserMapper userMapper;

    @Override
    public PrematureEmployeeCheck mapToDomain(PrematureEmployeeCheckEntity prematureEmployeeCheckEntity) {
        return PrematureEmployeeCheck.builder()
                .id(prematureEmployeeCheckEntity.getId())
                .user(userMapper.mapToDomain(prematureEmployeeCheckEntity.getUser()))
                .forMonth(prematureEmployeeCheckEntity.getForMonth())
                .reason(prematureEmployeeCheckEntity.getReason())
                .creationDate(prematureEmployeeCheckEntity.getCreationDate())
                .state(prematureEmployeeCheckEntity.getState())
                .build();
    }

    @Override
    public PrematureEmployeeCheckEntity mapToEntity(PrematureEmployeeCheck prematureEmployeeCheck){
        PrematureEmployeeCheckEntity prematureEmployeeCheckEntity = new PrematureEmployeeCheckEntity();
        prematureEmployeeCheckEntity.setId(prematureEmployeeCheck.getId());
        prematureEmployeeCheckEntity.setUser(userMapper.mapToEntity(prematureEmployeeCheck.getUser()));
        prematureEmployeeCheckEntity.setForMonth(prematureEmployeeCheck.getForMonth());
        prematureEmployeeCheckEntity.setReason(prematureEmployeeCheck.getReason());
        prematureEmployeeCheckEntity.setState(prematureEmployeeCheck.getState());
        return prematureEmployeeCheckEntity;
    }
}

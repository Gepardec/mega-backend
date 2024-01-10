package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.db.repository.PrematureEmployeeCheckRepository;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import com.gepardec.mega.service.mapper.PrematureEmployeeCheckMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class PrematureEmployeeCheckServiceImpl implements PrematureEmployeeCheckService {

    @Inject
    PrematureEmployeeCheckRepository prematureEmployeeCheckRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;

    @Inject
    Logger logger;

    @Override
    public boolean addPrematureEmployeeCheck(PrematureEmployeeCheck prematureEmployeeCheck) {

        PrematureEmployeeCheckEntity prematureEmployeeCheckEntity = prematureEmployeeCheckMapper.mapToEntity(prematureEmployeeCheck);
        prematureEmployeeCheckEntity.setUser(
                userRepository.findActiveByEmail(prematureEmployeeCheck.getUser().getEmail()).orElseThrow()
        );

        PrematureEmployeeCheckEntity saved = prematureEmployeeCheckRepository.update(prematureEmployeeCheckEntity);

        logger.info(
                String.format("PrematureEmployeeCheck created for %s in %s",
                        saved.getUser().getEmail(),
                        saved.getForMonth())
        );

        return saved.getId() != null;
    }

    @Override
    public String getPrematureEmployeeCheckReason(String email, LocalDate date) {
        PrematureEmployeeCheckEntity prematureEmployeeCheck = prematureEmployeeCheckRepository.findByEmailAndMonth(email, date);

        if (prematureEmployeeCheck == null) {
            return null;
        }

        return prematureEmployeeCheck.getReason();
    }


    @Override
    public PrematureEmployeeCheckState getPrematureEmployeeCheckState(String email, LocalDate date) {
        PrematureEmployeeCheckEntity prematureEmployeeCheck = prematureEmployeeCheckRepository.findByEmailAndMonth(email, date);

        if (prematureEmployeeCheck == null) {
            return PrematureEmployeeCheckState.NO_PEC_MADE;
        }

        return prematureEmployeeCheck.getState();
    }

    @Override
    public List<PrematureEmployeeCheck> findAllForMonth(LocalDate localDate) {
        List<PrematureEmployeeCheckEntity> prematureEmployeeCheckEntities = prematureEmployeeCheckRepository.findAllForMonth(localDate);
        return prematureEmployeeCheckMapper.mapListToDomain(prematureEmployeeCheckEntities);
    }

    @Override
    public long deleteAllForMonth(LocalDate localDate) {
        return prematureEmployeeCheckRepository.deleteByMonth(localDate);
    }
    @Override
    public boolean deleteById(Long id) {
        return prematureEmployeeCheckRepository.delete(id);
    }
}

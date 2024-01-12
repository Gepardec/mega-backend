package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.db.repository.PrematureEmployeeCheckRepository;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import com.gepardec.mega.domain.mapper.PrematureEmployeeCheckMapper;
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
    PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;

    @Inject
    Logger logger;

    @Override
    public boolean addPrematureEmployeeCheck(PrematureEmployeeCheck prematureEmployeeCheck) {

        PrematureEmployeeCheckEntity prematureEmployeeCheckEntity = prematureEmployeeCheckMapper.mapToEntity(prematureEmployeeCheck);

        PrematureEmployeeCheckEntity saved = prematureEmployeeCheckRepository.save(prematureEmployeeCheckEntity);

        logger.info(String.format("PrematureEmployeeCheck created for %s in %s", saved.getUser()
                .getEmail(), saved.getForMonth()));

        return saved.getId() != null;
    }

    @Override
    public boolean updatePrematureEmployeeCheck(PrematureEmployeeCheck prematureEmployeeCheck) {
        PrematureEmployeeCheckEntity prematureEmployeeCheckEntity = prematureEmployeeCheckRepository.findByEmailAndMonth(prematureEmployeeCheck.getUser()
                .getEmail(), prematureEmployeeCheck.getForMonth());

        prematureEmployeeCheckEntity.setState(prematureEmployeeCheck.getState());

        if (prematureEmployeeCheck.getReason() != null) {
            prematureEmployeeCheckEntity.setReason(prematureEmployeeCheck.getReason());
        }

        PrematureEmployeeCheckEntity updated = prematureEmployeeCheckRepository.update(prematureEmployeeCheckEntity);

        logger.info(String.format("PrematureEmployeeCheck (id: %s) updated", updated.getId()));

        return updated.getId() != null;
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
    public long deleteAllForMonthWithState(LocalDate localDate, List<PrematureEmployeeCheckState> states) {
        return prematureEmployeeCheckRepository.deleteByMonthAndStates(localDate, states);
    }

    @Override
    public boolean deleteById(Long id) {
        return prematureEmployeeCheckRepository.delete(id);
    }
}

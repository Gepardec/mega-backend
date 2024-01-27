package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.db.repository.PrematureEmployeeCheckRepository;
import com.gepardec.mega.domain.mapper.PrematureEmployeeCheckMapper;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PrematureEmployeeCheckServiceImpl implements PrematureEmployeeCheckService {

    @Inject
    PrematureEmployeeCheckRepository prematureEmployeeCheckRepository;


    @Inject
    PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;

    @Inject
    Logger logger;

    @Override
    public Optional<PrematureEmployeeCheck> findByEmailAndMonth(String email, LocalDate date) {
        return prematureEmployeeCheckRepository.findByEmailAndMonth(email, date)
                .map(prematureEmployeeCheckMapper::mapToDomain);
    }

    @Override
    public boolean create(PrematureEmployeeCheck prematureEmployeeCheck) {
        var prematureEmployeeCheckEntity = prematureEmployeeCheckMapper.mapToEntity(prematureEmployeeCheck);

        var saved = prematureEmployeeCheckRepository.create(prematureEmployeeCheckEntity);
        logger.info("PrematureEmployeeCheck created for {} in {}", saved.getUser().getEmail(), saved.getForMonth());

        return saved.getId() != null;
    }

    @Override
    public boolean update(PrematureEmployeeCheck prematureEmployeeCheck) {
        var prematureEmployeeCheckEntity = prematureEmployeeCheckMapper.mapToEntity(
                prematureEmployeeCheck,
                prematureEmployeeCheckRepository.findById(prematureEmployeeCheck.getId())
        );

        PrematureEmployeeCheckEntity updated = prematureEmployeeCheckRepository.update(prematureEmployeeCheckEntity);
        logger.info("PrematureEmployeeCheck (id: {}) updated", updated.getId());

        return updated.getId() != null;
    }

    @Override
    public List<PrematureEmployeeCheck> findAllForMonth(LocalDate localDate) {
        return prematureEmployeeCheckMapper.mapListToDomain(
                prematureEmployeeCheckRepository.findAllForMonth(localDate)
        );
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

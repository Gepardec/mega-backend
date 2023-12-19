package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
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

    public boolean addPrematureEmployeeCheck(PrematureEmployeeCheck prematureEmployeeCheck) {

        PrematureEmployeeCheckEntity prematureEmployeeCheckEntity = new PrematureEmployeeCheckEntity();
        prematureEmployeeCheckEntity.setUser(
                userRepository.findActiveByEmail(prematureEmployeeCheck.getUser().getEmail()).orElseThrow()
        );
        prematureEmployeeCheckEntity.setForMonth(prematureEmployeeCheck.getForMonth().withDayOfMonth(1));
        prematureEmployeeCheckEntity.setReason(prematureEmployeeCheck.getReason());

        PrematureEmployeeCheckEntity saved = prematureEmployeeCheckRepository.save(prematureEmployeeCheckEntity);

        logger.info(
                String.format("PrematureEmployeeCheck created for %s in %s",
                        saved.getUser().getEmail(),
                        saved.getForMonth())
        );

        return saved.getId() != null;
    }

    @Override
    public List<PrematureEmployeeCheck> getPrematureEmployeeChecksForEmail(String email) {
        List<PrematureEmployeeCheckEntity> fromUserId = prematureEmployeeCheckRepository.findByEmail(email);

        return prematureEmployeeCheckMapper.mapListToDomain(fromUserId);
    }

    @Override
    public boolean hasUserPrematureEmployeeCheck(String email) {
        List<PrematureEmployeeCheck> prematureEmployeeCheckForUserId = getPrematureEmployeeChecksForEmail(email);
        return !prematureEmployeeCheckForUserId.isEmpty();
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

package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.repository.PrematureEmployeeCheckRepository;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import com.gepardec.mega.service.mapper.PrematureEmployeeCheckMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class PrematureEmployeeCheckServiceImpl implements PrematureEmployeeCheckService {

    @Inject
    PrematureEmployeeCheckRepository prematureEmployeeCheckRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    PrematureEmployeeCheckMapper prematureEmployeeCheckMapper;

    public boolean addPrematureEmployeeCheck(PrematureEmployeeCheck prematureEmployeeCheck) {

        com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck prematureEmployeeCheckDB = new com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck();
        prematureEmployeeCheckDB.setUser(userRepository.findActiveByEmail(prematureEmployeeCheck.getUser().getEmail())
                .orElseThrow());
        prematureEmployeeCheckDB.setForMonth(prematureEmployeeCheck.getForMonth().withDayOfMonth(1));


        com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck saved = prematureEmployeeCheckRepository.save(prematureEmployeeCheckDB);

        return saved.getId() != null;
    }

    @Override
    public List<PrematureEmployeeCheck> getPrematureEmployeeCheckForEmail(String email) {
        List<com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck> fromUserId = prematureEmployeeCheckRepository.getFromEmail(email);

        return prematureEmployeeCheckMapper.mapListToDomain(fromUserId);
    }

    @Override
    public boolean hasUserPrematureEmployeeCheck(String userId) {
        List<PrematureEmployeeCheck> prematureEmployeeCheckForUserId = getPrematureEmployeeCheckForEmail(userId);
        return !prematureEmployeeCheckForUserId.isEmpty();
    }
}

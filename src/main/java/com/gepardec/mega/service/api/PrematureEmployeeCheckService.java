package com.gepardec.mega.service.api;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;

import java.time.LocalDate;
import java.util.List;

public interface PrematureEmployeeCheckService {
    boolean addPrematureEmployeeCheck(PrematureEmployeeCheck prematureEmployeeCheck);

    boolean updatePrematureEmployeeCheck(PrematureEmployeeCheck prematureEmployeeCheck);

    String getPrematureEmployeeCheckReason(String email, LocalDate date);

    PrematureEmployeeCheckState getPrematureEmployeeCheckState(String email, LocalDate date);

    List<PrematureEmployeeCheck> findAllForMonth(LocalDate localDate);

    long deleteAllForMonth(LocalDate localDate);

    boolean deleteById(Long id);
}

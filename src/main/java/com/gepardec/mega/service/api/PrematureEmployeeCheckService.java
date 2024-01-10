package com.gepardec.mega.service.api;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;

import java.time.LocalDate;
import java.util.List;

public interface PrematureEmployeeCheckService {
    boolean addPrematureEmployeeCheck(PrematureEmployeeCheck prematureEmployeeCheck);

//    List<PrematureEmployeeCheck> getPrematureEmployeeChecksForEmail(String email);

    PrematureEmployeeCheckState getPrematureEmployeeCheckState(String email, LocalDate date);

    List<PrematureEmployeeCheck> findAllForMonth(LocalDate localDate);

    long deleteAllForMonth(LocalDate localDate);

    boolean deleteById(Long id);
}

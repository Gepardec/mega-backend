package com.gepardec.mega.service.api;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrematureEmployeeCheckService {

    Optional<PrematureEmployeeCheck> findByEmailAndMonth(String email, LocalDate date);

    boolean create(PrematureEmployeeCheck prematureEmployeeCheck);

    boolean update(PrematureEmployeeCheck prematureEmployeeCheck);

    List<PrematureEmployeeCheck> findAllForMonth(LocalDate localDate);

    long deleteAllForMonthWithState(LocalDate localDate, List<PrematureEmployeeCheckState> states);

    boolean deleteById(Long id);

}

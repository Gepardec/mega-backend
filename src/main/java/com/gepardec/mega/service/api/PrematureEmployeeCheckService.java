package com.gepardec.mega.service.api;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface PrematureEmployeeCheckService {

    Optional<PrematureEmployeeCheck> findByEmailAndMonth(String email, YearMonth payrollMonth);

    boolean create(PrematureEmployeeCheck prematureEmployeeCheck);

    boolean update(PrematureEmployeeCheck prematureEmployeeCheck);

    List<PrematureEmployeeCheck> findAllForMonth(YearMonth payrollMonth);

    long deleteAllForMonthWithState(YearMonth payrollMonth, List<PrematureEmployeeCheckState> states);

    boolean deleteById(Long id);

}

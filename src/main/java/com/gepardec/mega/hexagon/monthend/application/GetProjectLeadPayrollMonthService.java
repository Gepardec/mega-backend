package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetProjectLeadPayrollMonthUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.YearMonth;

@ApplicationScoped
@Transactional
public class GetProjectLeadPayrollMonthService implements GetProjectLeadPayrollMonthUseCase {

    @Override
    public YearMonth getPayrollMonth() {
        return YearMonth.now().minusMonths(1);
    }
}

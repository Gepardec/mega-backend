package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetProjectLeadPayrollMonthUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;

@ApplicationScoped
@Transactional
public class GetProjectLeadPayrollMonthService implements GetProjectLeadPayrollMonthUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;

    @Inject
    public GetProjectLeadPayrollMonthService(MonthEndTaskRepository monthEndTaskRepository) {
        this.monthEndTaskRepository = monthEndTaskRepository;
    }

    @Override
    public YearMonth getPayrollMonth(UserId leadId) {
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        if (!monthEndTaskRepository.findLeadProjectTasks(leadId, currentMonth).isEmpty()) {
            return currentMonth;
        }

        return previousMonth;
    }
}

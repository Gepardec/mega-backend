package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetEmployeePayrollMonthUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.YearMonth;

@ApplicationScoped
@Transactional
public class GetEmployeePayrollMonthService implements GetEmployeePayrollMonthUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final Clock clock;

    @Inject
    public GetEmployeePayrollMonthService(MonthEndTaskRepository monthEndTaskRepository, Clock clock) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.clock = clock;
    }

    @Override
    public YearMonth getPayrollMonth(UserId actorId) {
        YearMonth currentMonth = YearMonth.now(clock);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        if (!monthEndTaskRepository.findOpenSubjectTasks(actorId, currentMonth).isEmpty()) {
            return currentMonth;
        }

        if (monthEndTaskRepository.findOpenSubjectTasks(actorId, previousMonth).isEmpty()) {
            return currentMonth;
        }

        return previousMonth;
    }
}

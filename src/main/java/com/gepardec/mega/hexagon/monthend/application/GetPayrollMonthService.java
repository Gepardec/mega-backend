package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetPayrollMonthUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndScope;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.Capacity;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.YearMonth;
import java.util.Objects;

@ApplicationScoped
@Transactional
public class GetPayrollMonthService implements GetPayrollMonthUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final Clock clock;

    @Inject
    public GetPayrollMonthService(MonthEndTaskRepository monthEndTaskRepository, Clock clock) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.clock = clock;
    }

    @Override
    public YearMonth getPayrollMonth(UserId actorId, Capacity capacity) {
        Objects.requireNonNull(capacity, "capacity must not be null");
        return switch (capacity) {
            case EMPLOYEE -> employeePayrollMonth(actorId);
            case CONTROLLING -> controllingPayrollMonth(actorId);
        };
    }

    private YearMonth employeePayrollMonth(UserId actorId) {
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

    private YearMonth controllingPayrollMonth(UserId leadId) {
        YearMonth currentMonth = YearMonth.now(clock);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        if (monthEndTaskRepository.existsTasks(currentMonth, new MonthEndScope.LedProjects(leadId))) {
            return currentMonth;
        }

        return previousMonth;
    }
}

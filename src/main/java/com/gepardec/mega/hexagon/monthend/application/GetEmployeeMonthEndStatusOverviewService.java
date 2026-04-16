package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.GetEmployeeMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
@Transactional
public class GetEmployeeMonthEndStatusOverviewService implements GetEmployeeMonthEndStatusOverviewUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final MonthEndClarificationRepository monthEndClarificationRepository;
    private final AssembleMonthEndStatusOverviewService assembleMonthEndStatusOverviewService;

    @Inject
    public GetEmployeeMonthEndStatusOverviewService(
            MonthEndTaskRepository monthEndTaskRepository,
            MonthEndClarificationRepository monthEndClarificationRepository,
            AssembleMonthEndStatusOverviewService assembleMonthEndStatusOverviewService
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.monthEndClarificationRepository = monthEndClarificationRepository;
        this.assembleMonthEndStatusOverviewService = assembleMonthEndStatusOverviewService;
    }

    @Override
    public MonthEndStatusOverview getOverview(UserId employeeId, YearMonth month) {
        List<MonthEndTask> tasks = monthEndTaskRepository.findEmployeeVisibleTasks(employeeId, month);
        List<MonthEndClarification> clarifications = monthEndClarificationRepository
                .findAllEmployeeClarifications(employeeId, month);
        return assembleMonthEndStatusOverviewService.assemble(tasks, clarifications, employeeId, month);
    }
}

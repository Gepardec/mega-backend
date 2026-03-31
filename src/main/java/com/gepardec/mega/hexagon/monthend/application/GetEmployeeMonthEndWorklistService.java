package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklistItem;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetEmployeeMonthEndWorklistUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
public class GetEmployeeMonthEndWorklistService implements GetEmployeeMonthEndWorklistUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final MonthEndWorklistMapper monthEndWorklistMapper;

    @Inject
    public GetEmployeeMonthEndWorklistService(
            MonthEndTaskRepository monthEndTaskRepository,
            MonthEndWorklistMapper monthEndWorklistMapper
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.monthEndWorklistMapper = monthEndWorklistMapper;
    }

    @Override
    public MonthEndWorklist getWorklist(UserId employeeId, YearMonth month) {
        List<MonthEndWorklistItem> tasks = monthEndTaskRepository.findOpenEmployeeTasks(employeeId, month).stream()
                .map(monthEndWorklistMapper::toItem)
                .toList();
        return new MonthEndWorklist(employeeId, month, tasks);
    }
}

package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.GetMonthEndStatusOverviewUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
public class GetMonthEndStatusOverviewService implements GetMonthEndStatusOverviewUseCase {

    private final MonthEndTaskRepository monthEndTaskRepository;
    private final MonthEndStatusOverviewMapper monthEndStatusOverviewMapper;

    @Inject
    public GetMonthEndStatusOverviewService(
            MonthEndTaskRepository monthEndTaskRepository,
            MonthEndStatusOverviewMapper monthEndStatusOverviewMapper
    ) {
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.monthEndStatusOverviewMapper = monthEndStatusOverviewMapper;
    }

    @Override
    public MonthEndStatusOverview getOverview(UserId actorId, YearMonth month) {
        List<MonthEndStatusOverviewItem> entries = monthEndTaskRepository.findTasksForActor(actorId, month).stream()
                .map(monthEndStatusOverviewMapper::toItem)
                .toList();
        return new MonthEndStatusOverview(actorId, month, entries);
    }
}

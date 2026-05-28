package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.application.port.outbound.PayrollMonthCompletionPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class PayrollMonthCompletionAdapter implements PayrollMonthCompletionPort {

    @Inject
    MonthEndTaskRepository monthEndTaskRepository;

    @Override
    public Set<UserId> findUsersWithAllTasksCompleted(YearMonth month) {
        return monthEndTaskRepository.findByMonth(month).stream()
                .filter(task -> task.subjectEmployeeId() != null)
                .collect(Collectors.groupingBy(MonthEndTask::subjectEmployeeId))
                .entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .filter(entry -> entry.getValue().stream().allMatch(task -> task.status() == MonthEndTaskStatus.DONE))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}

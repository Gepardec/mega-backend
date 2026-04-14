package com.gepardec.mega.hexagon.monthend.domain.services;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class MonthEndTaskPlanningService {

    public List<MonthEndTask> planProjectTasks(
            YearMonth month,
            MonthEndProjectSnapshot project,
            Set<UserId> activeLeadIds,
            Set<MonthEndUserSnapshot> assignedUsers
    ) {
        List<MonthEndTask> tasks = new ArrayList<>();

        for (MonthEndUserSnapshot assignedUser : assignedUsers) {
            tasks.addAll(planEmployeeOwnedTasks(month, project, assignedUser));

            if (!activeLeadIds.isEmpty()) {
                tasks.add(MonthEndTask.create(
                        MonthEndTaskId.generate(),
                        month,
                        MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        project.id(),
                        assignedUser.id(),
                        activeLeadIds
                ));
            }
        }

        if (project.billable() && !activeLeadIds.isEmpty()) {
            tasks.add(MonthEndTask.create(
                    MonthEndTaskId.generate(),
                    month,
                    MonthEndTaskType.ABRECHNUNG,
                    project.id(),
                    null,
                    activeLeadIds
            ));
        }

        return tasks;
    }

    public List<MonthEndTask> planEmployeeOwnedTasks(
            YearMonth month,
            MonthEndProjectSnapshot project,
            MonthEndUserSnapshot subjectEmployee
    ) {
        List<MonthEndTask> tasks = new ArrayList<>();
        tasks.add(MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                project.id(),
                subjectEmployee.id(),
                Set.of(subjectEmployee.id())
        ));

        if (project.billable()) {
            tasks.add(MonthEndTask.create(
                    MonthEndTaskId.generate(),
                    month,
                    MonthEndTaskType.LEISTUNGSNACHWEIS,
                    project.id(),
                    subjectEmployee.id(),
                    Set.of(subjectEmployee.id())
            ));
        }

        return tasks;
    }
}

package com.gepardec.mega.hexagon.monthend.domain.port.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskKey;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface MonthEndTaskRepository {

    Optional<MonthEndTask> findById(MonthEndTaskId id);

    Optional<MonthEndTask> findByBusinessKey(MonthEndTaskKey businessKey);

    List<MonthEndTask> findByMonth(YearMonth month);

    Optional<MonthEndTask> findProjectLeadReviewTask(YearMonth month, ProjectId projectId, UserId subjectEmployeeId);

    List<MonthEndTask> findTasksForActor(UserId actorId, YearMonth month);

    List<MonthEndTask> findOpenEmployeeTasks(UserId employeeId, YearMonth month);

    List<MonthEndTask> findOpenProjectLeadTasks(UserId projectLeadId, YearMonth month);

    void save(MonthEndTask task);

    void saveAll(List<MonthEndTask> tasks);
}

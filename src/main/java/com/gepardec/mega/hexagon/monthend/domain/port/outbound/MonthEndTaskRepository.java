package com.gepardec.mega.hexagon.monthend.domain.port.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndScope;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface MonthEndTaskRepository {

    Optional<MonthEndTask> findById(MonthEndTaskId id);

    List<MonthEndTask> findByMonth(YearMonth month);

    boolean existsForSubjectEmployee(YearMonth month, ProjectId projectId, UserId subjectEmployeeId);

    Optional<MonthEndTask> findProjectLeadReviewTask(YearMonth month, ProjectId projectId, UserId subjectEmployeeId);

    List<MonthEndTask> findTasks(YearMonth month, MonthEndScope scope);

    boolean existsTasks(YearMonth month, MonthEndScope scope);

    List<MonthEndTask> findOpenSubjectTasks(UserId subjectId, YearMonth month);

    List<MonthEndTask> findOpenProjectTasksOfType(YearMonth month, ProjectId projectId, MonthEndTaskType type);

    boolean existsEligibleTaskOfType(YearMonth month, ProjectId projectId, MonthEndTaskType type, UserId actorId);

    void save(MonthEndTask task);

    void saveAll(List<MonthEndTask> tasks);
}

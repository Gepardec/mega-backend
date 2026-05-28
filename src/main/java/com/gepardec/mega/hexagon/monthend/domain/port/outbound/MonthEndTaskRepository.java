package com.gepardec.mega.hexagon.monthend.domain.port.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
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

    List<MonthEndTask> findEmployeeVisibleTasks(UserId employeeId, YearMonth month);

    List<MonthEndTask> findLeadProjectTasks(UserId leadId, YearMonth month);

    List<MonthEndTask> findOpenSubjectTasks(UserId subjectId, YearMonth month);

    void save(MonthEndTask task);

    void saveAll(List<MonthEndTask> tasks);
}

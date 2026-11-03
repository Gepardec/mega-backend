package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndCompletionPolicy;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndScope;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
public class MonthEndTaskRepositoryAdapter implements MonthEndTaskRepository {

    // Shared by findTasks and existsTasks so the two cannot drift apart.
    private static final String SUBJECT_EMPLOYEE_QUERY = "monthValue = ?1 and subjectEmployeeId = ?2";
    private static final String LED_PROJECTS_QUERY =
            "from MonthEndTaskEntity task " +
                    "where task.monthValue = ?1 " +
                    "and exists (" +
                    "    select 1 from MonthEndTaskEntity lead " +
                    "    join lead.eligibleActorIds actor " +
                    "    where lead.monthValue = task.monthValue " +
                    "    and lead.projectId = task.projectId " +
                    "    and actor = ?2 " +
                    "    and lead.type in ?3" +
                    ")";

    @Inject
    MonthEndTaskPanacheRepository panache;

    @Inject
    MonthEndTaskMapper mapper;

    @Override
    public Optional<MonthEndTask> findById(MonthEndTaskId id) {
        return panache.find("id", id.value())
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public List<MonthEndTask> findByMonth(YearMonth month) {
        return panache.find("monthValue", toMonthValue(month))
                .list().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsForSubjectEmployee(YearMonth month, ProjectId projectId, UserId subjectEmployeeId) {
        return panache.count(
                "monthValue = ?1 and projectId = ?2 and subjectEmployeeId = ?3",
                toMonthValue(month),
                projectId.value(),
                subjectEmployeeId.value()
        ) > 0;
    }

    @Override
    public Optional<MonthEndTask> findProjectLeadReviewTask(YearMonth month, ProjectId projectId, UserId subjectEmployeeId) {
        return panache.find(
                        "monthValue = ?1 and projectId = ?2 and subjectEmployeeId = ?3 and type = ?4",
                        toMonthValue(month),
                        projectId.value(),
                        subjectEmployeeId.value(),
                        MonthEndTaskType.PROJECT_LEAD_REVIEW
                )
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public List<MonthEndTask> findTasks(YearMonth month, MonthEndScope scope) {
        return switch (scope) {
            case MonthEndScope.SubjectEmployee subject -> findSubjectEmployeeTasks(subject.actorId(), month);
            case MonthEndScope.LedProjects led -> findLedProjectsTasks(led.actorId(), month);
        };
    }

    @Override
    public boolean existsTasks(YearMonth month, MonthEndScope scope) {
        return switch (scope) {
            case MonthEndScope.SubjectEmployee subject -> panache.count(
                    SUBJECT_EMPLOYEE_QUERY,
                    toMonthValue(month),
                    subject.actorId().value()
            ) > 0;
            case MonthEndScope.LedProjects led -> panache.count(
                    LED_PROJECTS_QUERY,
                    toMonthValue(month),
                    led.actorId().value(),
                    leadTaskTypes()
            ) > 0;
        };
    }

    private List<MonthEndTask> findSubjectEmployeeTasks(UserId employeeId, YearMonth month) {
        return panache.find(
                        SUBJECT_EMPLOYEE_QUERY,
                        toMonthValue(month),
                        employeeId.value()
                )
                .list().stream()
                .map(mapper::toDomain)
                .toList();
    }

    private List<MonthEndTask> findLedProjectsTasks(UserId leadId, YearMonth month) {
        return panache.find(
                        LED_PROJECTS_QUERY,
                        toMonthValue(month),
                        leadId.value(),
                        leadTaskTypes()
                )
                .list().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<MonthEndTask> findOpenSubjectTasks(UserId subjectId, YearMonth month) {
        return panache.find(
                        "monthValue = ?1 and status = ?2 and subjectEmployeeId = ?3",
                        toMonthValue(month),
                        MonthEndTaskStatus.OPEN,
                        subjectId.value()
                )
                .list().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<MonthEndTask> findOpenProjectTasksOfType(YearMonth month, ProjectId projectId, MonthEndTaskType type) {
        return panache.find(
                        "monthValue = ?1 and projectId = ?2 and type = ?3 and status = ?4",
                        toMonthValue(month),
                        projectId.value(),
                        type,
                        MonthEndTaskStatus.OPEN
                )
                .list().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsEligibleTaskOfType(YearMonth month, ProjectId projectId, MonthEndTaskType type, UserId actorId) {
        return panache.count(
                "from MonthEndTaskEntity task " +
                        "join task.eligibleActorIds actor " +
                        "where task.monthValue = ?1 " +
                        "and task.projectId = ?2 " +
                        "and actor = ?3 " +
                        "and task.type = ?4",
                toMonthValue(month),
                projectId.value(),
                actorId.value(),
                type
        ) > 0;
    }

    @Override
    public void save(MonthEndTask task) {
        upsert(task);
    }

    @Override
    public void saveAll(List<MonthEndTask> tasks) {
        for (MonthEndTask task : tasks) {
            upsert(task);
        }
    }

    private List<MonthEndTaskType> leadTaskTypes() {
        return Stream.of(MonthEndTaskType.values())
                .filter(type -> type.completionPolicy() == MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR)
                .toList();
    }

    private void upsert(MonthEndTask task) {
        MonthEndTaskEntity entity = panache.find("id", task.id().value())
                .firstResultOptional()
                .orElseGet(MonthEndTaskEntity::new);
        boolean isNew = entity.getId() == null;
        mapper.updateEntity(task, entity);
        if (isNew) {
            panache.persist(entity);
        } else {
            panache.getEntityManager().merge(entity);
        }
    }

    private LocalDate toMonthValue(YearMonth month) {
        return month.atDay(1);
    }
}

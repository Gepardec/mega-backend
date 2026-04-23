package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndCompletionPolicy;
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
    public List<MonthEndTask> findEmployeeVisibleTasks(UserId employeeId, YearMonth month) {
        return panache.find(
                        "monthValue = ?1 and subjectEmployeeId = ?2",
                        toMonthValue(month),
                        employeeId.value()
                )
                .list().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<MonthEndTask> findLeadProjectTasks(UserId leadId, YearMonth month) {
        return panache.find(
                        "select task from MonthEndTaskEntity task " +
                                "where task.monthValue = ?1 " +
                                "and exists (" +
                                "    select 1 from MonthEndTaskEntity lead " +
                                "    join lead.eligibleActorIds actor " +
                                "    where lead.monthValue = task.monthValue " +
                                "    and lead.projectId = task.projectId " +
                                "    and actor = ?2 " +
                                "    and lead.type in ?3" +
                                ")",
                        toMonthValue(month),
                        leadId.value(),
                        taskTypesFor(MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR)
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
    public void save(MonthEndTask task) {
        upsert(task);
    }

    @Override
    public void saveAll(List<MonthEndTask> tasks) {
        for (MonthEndTask task : tasks) {
            upsert(task);
        }
    }

    private List<MonthEndTaskType> taskTypesFor(MonthEndCompletionPolicy policy) {
        return Stream.of(MonthEndTaskType.values())
                .filter(type -> type.completionPolicy() == policy)
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

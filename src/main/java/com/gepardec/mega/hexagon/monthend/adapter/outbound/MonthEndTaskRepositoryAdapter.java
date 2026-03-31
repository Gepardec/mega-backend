package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndCompletionPolicy;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
@Transactional
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
    public List<MonthEndTask> findOpenEmployeeTasks(UserId employeeId, YearMonth month) {
        return findOpenTasks(employeeId, month, MonthEndCompletionPolicy.INDIVIDUAL_ACTOR);
    }

    @Override
    public List<MonthEndTask> findOpenProjectLeadTasks(UserId projectLeadId, YearMonth month) {
        return findOpenTasks(projectLeadId, month, MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR);
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

    private List<MonthEndTask> findOpenTasks(UserId actorId, YearMonth month, MonthEndCompletionPolicy policy) {
        return panache.find(
                        "select distinct task from MonthEndTaskEntity task " +
                                "join task.eligibleActorIds actor " +
                                "where task.monthValue = ?1 and task.status = ?2 and actor = ?3 and task.type in ?4",
                        toMonthValue(month),
                        MonthEndTaskStatus.OPEN,
                        actorId.value(),
                        taskTypesFor(policy)
                )
                .list().stream()
                .map(mapper::toDomain)
                .toList();
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

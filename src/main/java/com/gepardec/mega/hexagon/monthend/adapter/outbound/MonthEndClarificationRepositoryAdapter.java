package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationStatus;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class MonthEndClarificationRepositoryAdapter implements MonthEndClarificationRepository {

    @Inject
    MonthEndClarificationPanacheRepository panache;

    @Inject
    MonthEndClarificationMapper mapper;

    @Override
    public Optional<MonthEndClarification> findById(MonthEndClarificationId id) {
        return panache.find("id", id.value())
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public List<MonthEndClarification> findOpenEmployeeClarifications(UserId employeeId, YearMonth month) {
        return panache.find(
                        "monthValue = ?1 and status = ?2 and subjectEmployeeId = ?3",
                        toMonthValue(month),
                        MonthEndClarificationStatus.OPEN,
                        employeeId.value()
                )
                .list().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<MonthEndClarification> findOpenProjectLeadClarifications(UserId projectLeadId, YearMonth month) {
        return panache.find(
                        "select distinct clarification from MonthEndClarificationEntity clarification " +
                                "join clarification.eligibleProjectLeadIds lead " +
                                "where clarification.monthValue = ?1 and clarification.status = ?2 and lead = ?3",
                        toMonthValue(month),
                        MonthEndClarificationStatus.OPEN,
                        projectLeadId.value()
                )
                .list().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void save(MonthEndClarification clarification) {
        MonthEndClarificationEntity entity = panache.find("id", clarification.id().value())
                .firstResultOptional()
                .orElseGet(MonthEndClarificationEntity::new);
        boolean isNew = entity.getId() == null;
        mapper.updateEntity(clarification, entity);
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

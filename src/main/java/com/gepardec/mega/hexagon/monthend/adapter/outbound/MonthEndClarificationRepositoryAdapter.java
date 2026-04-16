package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationStatus;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
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
        return findEmployeeClarifications(employeeId, month, MonthEndClarificationStatus.OPEN);
    }

    @Override
    public List<MonthEndClarification> findAllEmployeeClarifications(UserId employeeId, YearMonth month) {
        return findEmployeeClarifications(employeeId, month, null);
    }

    @Override
    public List<MonthEndClarification> findOpenProjectLeadClarifications(UserId projectLeadId, YearMonth month) {
        return findProjectLeadClarifications(projectLeadId, month, MonthEndClarificationStatus.OPEN);
    }

    @Override
    public List<MonthEndClarification> findAllProjectLeadClarifications(UserId projectLeadId, YearMonth month) {
        return findProjectLeadClarifications(projectLeadId, month, null);
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

    private List<MonthEndClarification> findEmployeeClarifications(
            UserId employeeId,
            YearMonth month,
            MonthEndClarificationStatus status
    ) {
        if (status == null) {
            return panache.find(
                            "monthValue = ?1 and subjectEmployeeId = ?2",
                            toMonthValue(month),
                            employeeId.value()
                    )
                    .list().stream()
                    .map(mapper::toDomain)
                    .toList();
        }

        return panache.find(
                        "monthValue = ?1 and status = ?2 and subjectEmployeeId = ?3",
                        toMonthValue(month),
                        status,
                        employeeId.value()
                )
                .list().stream()
                .map(mapper::toDomain)
                .toList();
    }

    private List<MonthEndClarification> findProjectLeadClarifications(
            UserId projectLeadId,
            YearMonth month,
            MonthEndClarificationStatus status
    ) {
        String query = "select distinct clarification from MonthEndClarificationEntity clarification " +
                "join clarification.eligibleProjectLeadIds lead " +
                "where clarification.monthValue = ?1 and lead = ?2";
        if (status == null) {
            return panache.find(
                            query,
                            toMonthValue(month),
                            projectLeadId.value()
                    )
                    .list().stream()
                    .map(mapper::toDomain)
                    .toList();
        }

        return panache.find(
                        query.replace("where clarification.monthValue = ?1 and lead = ?2",
                                "where clarification.monthValue = ?1 and clarification.status = ?2 and lead = ?3"),
                        toMonthValue(month),
                        status,
                        projectLeadId.value()
                )
                .list().stream()
                .map(mapper::toDomain)
                .toList();
    }
}

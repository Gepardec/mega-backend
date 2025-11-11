package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class PrematureEmployeeCheckRepository implements PanacheRepository<PrematureEmployeeCheckEntity> {

    private static final String P_EMAIL = "email";
    private static final String P_FOR_MONTH = "forMonth";
    private static final String P_STATES = "states";

    public PrematureEmployeeCheckEntity create(final PrematureEmployeeCheckEntity prematureEmployeeCheckEntity) {
        // Flushing to trigger the ConstraintViolationException to be able to catch it
        persistAndFlush(prematureEmployeeCheckEntity);
        return prematureEmployeeCheckEntity;
    }

    public PrematureEmployeeCheckEntity update(final PrematureEmployeeCheckEntity prematureEmployeeCheckEntity) {
        return getEntityManager().merge(prematureEmployeeCheckEntity);
    }

    public boolean delete(Long id) {
        return deleteById(id);
    }

    public List<PrematureEmployeeCheckEntity> findAllForMonth(LocalDate forMonth) {
        return find("#PrematureEmployeeCheck.findAllByMonth",
                Parameters.with(P_FOR_MONTH, forMonth))
                .list();
    }

    public Optional<PrematureEmployeeCheckEntity> findByEmailAndMonth(String email, LocalDate forMonth) {
        return find("#PrematureEmployeeCheck.findByEmailAndMonth",
                Parameters.with(P_EMAIL, email).and(P_FOR_MONTH, forMonth))
                .firstResultOptional();
    }

    public long deleteByMonthAndStates(LocalDate forMonth, List<PrematureEmployeeCheckState> states) {
        return delete("#PrematureEmployeeCheck.deleteAllByMonthAndStates",
                Parameters.with(P_FOR_MONTH, forMonth).and(P_STATES, states));
    }
}

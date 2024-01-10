package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
@Transactional
public class PrematureEmployeeCheckRepository implements PanacheRepository<PrematureEmployeeCheckEntity> {

    @Inject
    EntityManager em;

    @Inject
    Logger logger;

    public PrematureEmployeeCheckEntity save(final PrematureEmployeeCheckEntity prematureEmployeeCheckEntity) {
        this.persistAndFlush(prematureEmployeeCheckEntity);
        // Flushing to trigger the ConstraintViolationException to be able to catch it
        return prematureEmployeeCheckEntity;
    }


    public PrematureEmployeeCheckEntity update(final PrematureEmployeeCheckEntity prematureEmployeeCheckEntity) {
        return em.merge(prematureEmployeeCheckEntity);
    }


    public boolean delete(Long id) {
        return deleteById(id);
    }


    public List<PrematureEmployeeCheckEntity> findAllForMonth(LocalDate forMonth) {
        forMonth = forMonth.withDayOfMonth(1);
        return find("#PrematureEmployeeCheck.findAllByMonth",
                Parameters.with("forMonth", forMonth))
                .list();
    }


    public PrematureEmployeeCheckEntity findByEmailAndMonth(String email, LocalDate forMonth) {
        return find("#PrematureEmployeeCheck.findByEmailAndMonth",
                Parameters.with("email", email).and("forMonth", forMonth))
                .firstResult();
    }

    public long deleteByMonth(LocalDate forMonth) {
        return delete("#PrematureEmployeeCheck.deleteAllByMonth",
                Parameters.with("forMonth", forMonth));
    }
}

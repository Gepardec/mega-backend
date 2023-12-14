package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;

import java.util.List;

@ApplicationScoped
public class PrematureEmployeeCheckRepository implements PanacheRepository<PrematureEmployeeCheckEntity> {

    @Inject
    EntityManager em;

    @Inject
    Logger logger;

    @Transactional
    public PrematureEmployeeCheckEntity save(final PrematureEmployeeCheckEntity prematureEmployeeCheckEntity) {
        this.persist(prematureEmployeeCheckEntity);
        // Flushing to trigger the ConstraintViolationException to be able to catch it
        this.flush();
        return prematureEmployeeCheckEntity;
    }

    @Transactional
    public PrematureEmployeeCheckEntity update(final PrematureEmployeeCheckEntity prematureEmployeeCheckEntity) {
        return em.merge(prematureEmployeeCheckEntity);
    }

    @Transactional
    public boolean delete(Long id) {
        return deleteById(id);
    }

    public List<PrematureEmployeeCheckEntity> findByEmail(String email) {
        return find("#PrematureEmployeeCheck.findByEmail",
                Parameters.with("email", email))
                .list();
    }
}

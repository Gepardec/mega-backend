package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckEntity;
import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheckState;
import com.gepardec.mega.db.entity.employee.User;
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
    UserRepository userRepository;
    @Inject
    Logger logger;

    public PrematureEmployeeCheckEntity save(final PrematureEmployeeCheckEntity prematureEmployeeCheckEntity) {
        User user = userRepository.findActiveByEmail(prematureEmployeeCheckEntity.getUser().getEmail()).orElseThrow();
        prematureEmployeeCheckEntity.setUser(user);
        PrematureEmployeeCheckEntity merge = em.merge(prematureEmployeeCheckEntity);
        em.flush();
        // Flushing to trigger the ConstraintViolationExcepstion to be able to catch it
        return merge;
    }


    public PrematureEmployeeCheckEntity update(final PrematureEmployeeCheckEntity prematureEmployeeCheckEntity) {
        User user = userRepository.findActiveByEmail(prematureEmployeeCheckEntity.getUser().getEmail()).orElseThrow();
        prematureEmployeeCheckEntity.setUser(user);
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

    public long deleteByMonthAndStates(LocalDate forMonth, List<PrematureEmployeeCheckState> states) {
        return delete("#PrematureEmployeeCheck.deleteAllByMonthAndStates",
                Parameters.with("forMonth", forMonth).and("states", states));
    }
}

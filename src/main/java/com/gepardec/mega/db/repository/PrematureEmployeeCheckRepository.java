
package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;

import java.util.List;

@ApplicationScoped
public class PrematureEmployeeCheckRepository implements PanacheRepository<PrematureEmployeeCheck> {

    @Inject
    EntityManager em;

    @Inject
    Logger logger;

    @Transactional
    public PrematureEmployeeCheck save(final PrematureEmployeeCheck prematureEmployeeCheck){
        this.persist(prematureEmployeeCheck);
        this.flush();
        return prematureEmployeeCheck;

//        try {
//            this.persist(prematureEmployeeCheck);
//            // For triggring the ConstraintViolationExcpetion
//            this.flush();
//            return prematureEmployeeCheck;
//        } catch (ConstraintViolationException e) {
//            logger.error(String.format("Tried to add a PrematureEmployeeCheck for %s in %s, but there already exists one", prematureEmployeeCheck.getUser()
//                    .getEmail(), prematureEmployeeCheck.getForMonth()));
//            return new PrematureEmployeeCheck();
//        }
    }

    @Transactional
    public PrematureEmployeeCheck update(final PrematureEmployeeCheck prematureEmployeeCheck) {
        return em.merge(prematureEmployeeCheck);
    }

    @Transactional
    public boolean delete(Long id) {
        return deleteById(id);
    }

    public List<PrematureEmployeeCheck> getFromEmail(String email) {
        return find("#PrematureEmployeeCheck.findByEmail",
                Parameters.with("email", email))
                .list();
    }
}


package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.PrematureEmployeeCheck;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class PrematureEmployeeCheckRepository implements PanacheRepository<PrematureEmployeeCheck> {

    @Inject
    EntityManager em;

    @Transactional
    public PrematureEmployeeCheck save(final PrematureEmployeeCheck prematureEmployeeCheck) {
        this.persist(prematureEmployeeCheck);
        return prematureEmployeeCheck;
    }

    @Transactional
    public PrematureEmployeeCheck update(final PrematureEmployeeCheck prematureEmployeeCheck) {
        return em.merge(prematureEmployeeCheck);
    }

    @Transactional
    public boolean deletePrematureEmployeeCheck(Long id) {
        return deleteById(id);
    }

    public List<PrematureEmployeeCheck> getFromEmail(String email){
        return find("#PrematureEmployeeCheck.findByEmail",
                Parameters.with("email", email))
                .list();
    }
}

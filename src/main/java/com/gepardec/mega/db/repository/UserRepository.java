package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.domain.model.Role;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class UserRepository implements PanacheRepository<User> {

    @Inject
    EntityManager em;

    public Optional<User> findActiveByEmail(final String email) {
        return find("#User.findActiveByEmail", Parameters.with("email", email)).firstResultOptional();
    }

    public Optional<User> findActiveByName(final String firstname, final String lastname) {
        return find(
                "#User.findActiveByName",
                Parameters.with("firstname", firstname)
                        .and("lastname", lastname)
        )
                .firstResultOptional();
    }

    public List<User> findActive() {
        return find("#User.findActive").list();
    }

    public List<User> findByRoles(final List<Role> roles) {
        return find("#User.findByRoles", Parameters.with("roles", roles)).list();
    }

    public User persistOrUpdate(final User user) {
        if (user.getId() == null) {
            persist(user);
            return user;
        } else {
            return update(user);
        }
    }

    public User update(final User user) {
        return em.merge(user);
    }
}

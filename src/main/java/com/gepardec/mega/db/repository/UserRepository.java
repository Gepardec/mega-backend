package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.UserEntity;
import com.gepardec.mega.domain.model.Role;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@Transactional
public class UserRepository implements PanacheRepository<UserEntity> {

    @Inject
    EntityManager em;

    public Optional<UserEntity> findActiveByEmail(final String email) {
        return find("#User.findActiveByEmail", Parameters.with("email", email)).firstResultOptional();
    }

    public Optional<UserEntity> findActiveByName(final String firstname, final String lastname) {
        return find(
                "#User.findActiveByName",
                Parameters.with("firstname", firstname)
                        .and("lastname", lastname)
        )
                .firstResultOptional();
    }

    public Optional<UserEntity> findByZepId(final String zepId) {
        return find(
                "#User.findActiveByZepId",
                Parameters.with("zepId", zepId)
        )
                .firstResultOptional();
    }

    public List<UserEntity> findByZepIds(final Set<String> zepIds) {
        if (zepIds == null || zepIds.isEmpty()) {
            return List.of();
        }
        return find(
                "active = true AND zepId IN :zepIds",
                Parameters.with("zepIds", zepIds)
        ).list();
    }

    public List<UserEntity> findActive() {
        return find("#User.findActive").list();
    }

    public List<UserEntity> findByRoles(final List<Role> roles) {
        return find("#User.findByRoles", Parameters.with("roles", roles)).list();
    }

    public UserEntity persistOrUpdate(final UserEntity user) {
        if (user.getId() == null) {
            persist(user);
            return user;
        } else {
            return update(user);
        }
    }

    public UserEntity update(final UserEntity user) {
        return em.merge(user);
    }
}

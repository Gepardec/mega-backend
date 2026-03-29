package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.port.outbound.UserLookupPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class UserLookupAdapter implements UserLookupPort {

    @Inject
    EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public Optional<UUID> findUserIdByZepUsername(String username) {
        List<UUID> result = entityManager
                .createNativeQuery("SELECT id FROM hexagon_users WHERE zep_username = :username", UUID.class)
                .setParameter("username", username)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
}

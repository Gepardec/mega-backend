package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.port.outbound.UserLookupPort;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
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
    public Optional<UserId> findUserIdByZepUsername(ZepUsername username) {
        List<UUID> result = entityManager
                .createNativeQuery("SELECT id FROM hexagon_users WHERE zep_username = :username", UUID.class)
                .setParameter("username", username.value())
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(UserId.of(result.get(0)));
    }
}

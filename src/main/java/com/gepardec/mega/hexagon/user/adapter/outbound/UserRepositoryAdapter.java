package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class UserRepositoryAdapter implements UserRepository {

    @Inject
    UserPanacheRepository panache;

    @Inject
    UserMapper mapper;

    @Override
    public Optional<User> findById(UserId userId) {
        return panache.find("id", userId.value())
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return panache.find("email", email.value())
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByZepUsername(ZepUsername username) {
        return panache.find("zepUsername", username.value())
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public List<User> findByZepUsernames(Set<ZepUsername> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return List.of();
        }

        Set<String> values = usernames.stream()
                .map(ZepUsername::value)
                .collect(Collectors.toSet());

        return panache.list("zepUsername in ?1", values).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<User> findAll() {
        return panache.listAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<User> findByIds(Set<UserId> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }

        Set<UUID> ids = userIds.stream()
                .map(UserId::value)
                .collect(Collectors.toSet());
        return panache.list("id in ?1", ids).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<User> users) {
        for (User user : users) {
            UserEntity entity = panache.find("id", user.id().value())
                    .firstResultOptional()
                    .orElseGet(UserEntity::new);
            boolean isNew = entity.getId() == null;
            mapper.updateEntity(user, entity);
            if (isNew) {
                panache.persist(entity);
            }
        }
    }
}

package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class UserRepositoryAdapter implements UserRepository {

    @Inject
    UserPanacheRepository panache;

    @Inject
    UserMapper mapper;

    @Override
    public Optional<User> findByZepUsername(String username) {
        return panache.find("zepUsername", username)
                .firstResultOptional()
                .map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return panache.listAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<User> users) {
        for (User user : users) {
            UserEntity entity = panache.find("id", user.getId().value())
                    .firstResultOptional()
                    .orElseGet(UserEntity::new);
            boolean isNew = entity.getId() == null;
            mapper.updateEntity(user, entity);
            if (isNew) {
                panache.persist(entity);
            } else {
                panache.getEntityManager().merge(entity);
            }
        }
    }
}

package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.UserStatus;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class UserRepositoryAdapter implements UserRepository {

    @Inject
    UserPanacheRepository panache;

    @Override
    public Optional<User> findByZepUsername(String username) {
        return panache.find("zepUsername", username)
                .firstResultOptional()
                .map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return panache.listAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<User> users) {
        for (User user : users) {
            UserEntity entity = panache.find("id", user.id().value())
                    .firstResultOptional()
                    .orElseGet(UserEntity::new);
            toEntity(user, entity);
            if (entity.getId() == null) {
                panache.persist(entity);
            } else {
                panache.getEntityManager().merge(entity);
            }
        }
    }

    private User toDomain(UserEntity entity) {
        Set<Role> roles = entity.getRoles() == null
                ? Set.of()
                : entity.getRoles().stream()
                  .map(Role::valueOf)
                  .collect(Collectors.toSet());

        return User.reconstitute(
                UserId.of(entity.getId()),
                Email.of(entity.getEmail()),
                FullName.of(entity.getFirstname(), entity.getLastname()),
                UserStatus.valueOf(entity.getStatus()),
                roles,
                entity.getZepProfile(),
                entity.getPersonioProfile()
        );
    }

    private void toEntity(User user, UserEntity entity) {
        entity.setId(user.id().value());
        entity.setEmail(user.email() != null ? user.email().value() : null);
        entity.setFirstname(user.name() != null ? user.name().firstname() : null);
        entity.setLastname(user.name() != null ? user.name().lastname() : null);
        entity.setStatus(user.status().name());
        entity.setRoles(user.roles().stream().map(Role::name).collect(Collectors.toSet()));
        entity.setZepUsername(user.zepProfile() != null ? user.zepProfile().username() : null);
        entity.setZepProfile(user.zepProfile());
        entity.setPersonioProfile(user.personioProfile());
    }
}

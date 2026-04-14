package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.port.outbound.UserIdentityLookupPort;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.adapter.outbound.UserEntity;
import com.gepardec.mega.hexagon.user.adapter.outbound.UserPanacheRepository;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
@Transactional
public class UserIdentityLookupAdapter implements UserIdentityLookupPort {

    @Inject
    UserPanacheRepository userPanacheRepository;

    @Override
    public Optional<UserId> findUserIdByZepUsername(ZepUsername username) {
        return userPanacheRepository.find("zepUsername", username.value())
                .firstResultOptional()
                .map(UserEntity::getId)
                .map(UserId::of);
    }
}

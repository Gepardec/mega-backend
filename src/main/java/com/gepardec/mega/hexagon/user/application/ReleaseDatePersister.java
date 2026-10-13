package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Saves a single user in its own transaction, so one failed save cannot roll back another user's.
 * Lives in its own bean because CDI interceptors do not apply to self-invocation.
 */
@ApplicationScoped
public class ReleaseDatePersister {

    private final UserRepository userRepository;

    @Inject
    public ReleaseDatePersister(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void save(User user) {
        userRepository.saveAll(List.of(user));
    }
}

package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import com.gepardec.mega.hexagon.recognition.application.port.outbound.RecognitionSubmitterDirectoryPort;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class RecognitionSubmitterDirectoryAdapter implements RecognitionSubmitterDirectoryPort {

    private final UserRepository userRepository;

    @Inject
    public RecognitionSubmitterDirectoryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Map<UserId, String> findDisplayNamesByIds(Set<UserId> userIds) {
        Objects.requireNonNull(userIds, "userIds must not be null");

        return userRepository.findByIds(userIds).stream()
                .collect(Collectors.toMap(User::id, user -> user.name().displayName()));
    }
}

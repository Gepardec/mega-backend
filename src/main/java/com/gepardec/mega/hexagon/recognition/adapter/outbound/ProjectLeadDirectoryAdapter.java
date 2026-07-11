package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import com.gepardec.mega.hexagon.recognition.application.port.outbound.ProjectLeadDirectoryPort;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionMailRecipient;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ProjectLeadDirectoryAdapter implements ProjectLeadDirectoryPort {

    private final UserRepository userRepository;
    private final ProjectLeadDirectoryMapper mapper;

    @Inject
    public ProjectLeadDirectoryAdapter(UserRepository userRepository, ProjectLeadDirectoryMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public List<RecognitionMailRecipient> findActiveInternalProjectLeads(LocalDate referenceDate) {
        Objects.requireNonNull(referenceDate, "referenceDate must not be null");

        return userRepository.findByRole(Role.PROJECT_LEAD).stream()
                .filter(user -> user.roles().contains(Role.PROJECT_LEAD))
                .filter(user -> user.isActiveOn(referenceDate))
                .filter(user -> !user.isExternal())
                .map(mapper::toRecipient)
                .toList();
    }
}

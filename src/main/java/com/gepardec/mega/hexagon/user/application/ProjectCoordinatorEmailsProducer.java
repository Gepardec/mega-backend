package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.user.domain.model.ProjectCoordinatorEmails;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProjectCoordinatorEmailsProducer {

    private final Optional<List<String>> projectCoordinatorEmails;

    @Inject
    public ProjectCoordinatorEmailsProducer(@ConfigProperty(name = "mega.project-coordinator.emails") Optional<List<String>> projectCoordinatorEmails) {
        this.projectCoordinatorEmails = projectCoordinatorEmails;
    }

    @Produces
    @Dependent
    public ProjectCoordinatorEmails projectCoordinatorEmails() {
        return new ProjectCoordinatorEmails(new HashSet<>(projectCoordinatorEmails.orElse(List.of())));
    }
}

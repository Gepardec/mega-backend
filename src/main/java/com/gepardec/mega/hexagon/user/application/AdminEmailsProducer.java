package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.user.domain.model.AdminEmails;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AdminEmailsProducer {

    private final Optional<List<String>> adminEmails;

    @Inject
    public AdminEmailsProducer(@ConfigProperty(name = "mega.admin.emails") Optional<List<String>> adminEmails) {
        this.adminEmails = adminEmails;
    }

    @Produces
    @Dependent
    public AdminEmails adminEmails() {
        return new AdminEmails(new HashSet<>(adminEmails.orElse(List.of())));
    }
}

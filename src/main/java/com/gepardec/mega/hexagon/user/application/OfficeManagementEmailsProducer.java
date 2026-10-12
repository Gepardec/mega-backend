package com.gepardec.mega.hexagon.user.application;

import com.gepardec.mega.hexagon.user.domain.model.OfficeManagementEmails;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class OfficeManagementEmailsProducer {

    private final List<String> officeManagementEmails;

    @Inject
    public OfficeManagementEmailsProducer(@ConfigProperty(name = "mega.mail.reminder.om") List<String> officeManagementEmails) {
        this.officeManagementEmails = officeManagementEmails;
    }

    @Produces
    @Dependent
    public OfficeManagementEmails officeManagementEmails() {
        return new OfficeManagementEmails(new HashSet<>(officeManagementEmails));
    }
}

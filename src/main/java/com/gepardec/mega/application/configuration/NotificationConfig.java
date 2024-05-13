package com.gepardec.mega.application.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class NotificationConfig {

    private static final String LOGO_PATH = "img/logo.png";

    @Inject
    @ConfigProperty(name = "mega.mail.subject-prefix", defaultValue = "")
    Optional<String> subjectPrefix;

    @Inject
    @ConfigProperty(name = "mega.wiki.eom-url")
    String megaWikiEomUrl;

    @Inject
    @ConfigProperty(name = "mega.dash-url")
    String megaDashUrl;

    @Inject
    @ConfigProperty(name = "mega.mail.reminder.om")
    List<String> omMailAddresses;

    @Inject
    @ConfigProperty(name = "mega.mail.employees.notification")
    boolean employeesNotification;

    public Optional<String> getSubjectPrefix() {
        return subjectPrefix;
    }

    public String getMegaImageLogoUrl() {
        return LOGO_PATH;
    }

    public String getMegaWikiEomUrl() {
        return megaWikiEomUrl;
    }

    public String getMegaDashUrl() {
        return megaDashUrl;
    }

    public List<String> getOmMailAddresses() {
        return omMailAddresses;
    }

    public boolean isEmployeesNotification() {
        return employeesNotification;
    }
}

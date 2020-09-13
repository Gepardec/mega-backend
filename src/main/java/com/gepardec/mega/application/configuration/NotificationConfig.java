package com.gepardec.mega.application.configuration;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class NotificationConfig {

    @Inject
    @ConfigProperty(name = "mega.mail.subject-prefix", defaultValue = " ")
    String subjectPrefix;

    @Inject
    @ConfigProperty(name = "mega.logo-path")
    String megaImageLogoUrl;

    @Inject
    @ConfigProperty(name = "mega.wiki.eom-url")
    String megaWikiEomUrl;

    @Inject
    @ConfigProperty(name = "mega.dash-url")
    String megaDashUrl;

    @Inject
    @ConfigProperty(name = "mega.mail.reminder.pl")
    String plMailAddresses;

    @Inject
    @ConfigProperty(name = "mega.mail.reminder.om")
    String omMailAddresses;

    @Inject
    @ConfigProperty(name = "mega.mail.employees.notification")
    boolean employeesNotification;

    public String getSubjectPrefix() {
        return subjectPrefix;
    }

    public String getMegaImageLogoUrl() {
        return megaImageLogoUrl;
    }

    public String getMegaWikiEomUrl() {
        return megaWikiEomUrl;
    }

    public String getMegaDashUrl() {
        return megaDashUrl;
    }

    public String getPlMailAddresses() {
        return plMailAddresses;
    }

    public String getOmMailAddresses() {
        return omMailAddresses;
    }

    public boolean isEmployeesNotification() {
        return employeesNotification;
    }
}

package com.gepardec.mega.application.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class ApplicationConfig {

    @Inject
    @ConfigProperty(name = "mega.budget-calculation-excel-url")
    URL budgetCalculationExcelUrl;

    @Inject
    @ConfigProperty(name = "mega.info.build.version")
    String version;

    @Inject
    @ConfigProperty(name = "mega.info.build.date")
    LocalDateTime buildDate;

    @Inject
    @ConfigProperty(name = "mega.info.git.commit")
    String commit;

    @Inject
    @ConfigProperty(name = "mega.info.git.branch")
    String branch;

    @Inject
    @ConfigProperty(name = "quarkus.locales")
    List<Locale> locales;

    @Inject
    @ConfigProperty(name = "quarkus.default-locale")
    Locale defaultLocale;

    private LocalDateTime startAt;

    @PostConstruct
    public void init() {
        startAt = LocalDateTime.now();
    }

    public String getBudgetCalculationExcelUrlAsString() {
        return budgetCalculationExcelUrl.toString();
    }

    public String getVersion() {
        return version;
    }

    public LocalDateTime getBuildDate() {
        return buildDate;
    }

    public String getCommit() {
        return commit;
    }

    public String getBranch() {
        return branch;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }
}

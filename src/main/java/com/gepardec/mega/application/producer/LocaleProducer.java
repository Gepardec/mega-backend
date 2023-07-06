package com.gepardec.mega.application.producer;

import com.gepardec.mega.application.configuration.ApplicationConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

@RequestScoped
public class LocaleProducer {

    private final ApplicationConfig applicationConfig;

    private final HttpServletRequest request;

    private Locale currentLocale;

    @Inject
    public LocaleProducer(
            final ApplicationConfig applicationConfig,
            HttpServletRequest request) {
        this.applicationConfig = applicationConfig;
        this.request = request;
    }

    @PostConstruct
    void init() {
        currentLocale = determineCurrentLocale();
    }

    @Produces
    @Dependent
    public Locale getCurrentLocale() {
        return currentLocale;
    }

    private Locale determineCurrentLocale() {
        final Locale requestLocale = request.getLocale();
        if (requestLocale != null && applicationConfig.getLocales().contains(requestLocale)) {
            return request.getLocale();
        }
        return applicationConfig.getDefaultLocale();
    }
}

package com.gepardec.mega.application.producer;

import com.gepardec.mega.application.configuration.ApplicationConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

import java.util.List;
import java.util.Locale;

@RequestScoped
public class LocaleProducer {

    private final ApplicationConfig applicationConfig;

    private final HttpHeaders headers;

    private Locale currentLocale;

    @Inject
    public LocaleProducer(
            final ApplicationConfig applicationConfig,
            @Context HttpHeaders headers) {
        this.applicationConfig = applicationConfig;
        this.headers = headers;
        init();
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
        List<Locale> acceptableLocales = headers.getAcceptableLanguages();
        if (acceptableLocales != null) {
            for (Locale locale : acceptableLocales) {
                if (applicationConfig.getLocales().contains(locale)) {
                    return locale;
                }
            }
        }
        return applicationConfig.getDefaultLocale();
    }
}

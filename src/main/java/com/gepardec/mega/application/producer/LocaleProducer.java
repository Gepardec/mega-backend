package com.gepardec.mega.application.producer;

import com.gepardec.mega.application.configuration.ApplicationConfig;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;

import java.util.Locale;

@RequestScoped
public class LocaleProducer {

    private final ApplicationConfig config;

    @Inject
    public LocaleProducer(ApplicationConfig config) {
        this.config = config;
    }

    @Inject
    HttpHeaders headers;

    @Produces
    @Dependent
    public Locale produceLocale() {
        Locale requestLocale = extractLocaleFromHeaders();
        if (requestLocale != null && config.getLocales().contains(requestLocale)) {
            return requestLocale;
        }
        return config.getDefaultLocale();
    }

    private Locale extractLocaleFromHeaders() {
        var langs = headers.getAcceptableLanguages();
        return langs.isEmpty() ? null : langs.get(0);
    }
}

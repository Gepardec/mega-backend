package com.gepardec.mega.application.producer;

import com.gepardec.mega.application.configuration.ApplicationConfig;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class LocaleProducerTest {

    private static final Locale DEFAULT_LOCALE = Locale.GERMAN;

    private LocaleProducer producer;
    private HttpHeaders headersMock;

    @BeforeEach
    void beforeEach() {
        ApplicationConfig applicationConfigMock = mock(ApplicationConfig.class);
        headersMock = mock(HttpHeaders.class);

        when(applicationConfigMock.getLocales()).thenReturn(List.of(Locale.GERMAN, Locale.ENGLISH));
        when(applicationConfigMock.getDefaultLocale()).thenReturn(DEFAULT_LOCALE);

        producer = new LocaleProducer(applicationConfigMock);
        producer.headers = headersMock;
    }

    @Test
    void produceLocale_whenRequestHasNoLocale_thenReturnsDefaultLocale() {
        when(headersMock.getAcceptableLanguages()).thenReturn(Collections.emptyList());

        final Locale actual = producer.produceLocale();

        assertThat(actual).isEqualTo(DEFAULT_LOCALE);
    }

    @Test
    void produceLocale_whenRequestHasUnsupportedLocaleFRENCH_thenReturnsDefaultLocale() {
        when(headersMock.getAcceptableLanguages()).thenReturn(List.of(Locale.FRENCH));

        final Locale actual = producer.produceLocale();

        assertThat(actual).isEqualTo(DEFAULT_LOCALE);
    }

    @Test
    void produceLocale_whenRequestHasLocaleGERMAN_thenReturnsGERMAN() {
        when(headersMock.getAcceptableLanguages()).thenReturn(List.of(Locale.GERMAN));

        final Locale actual = producer.produceLocale();

        assertThat(actual).isEqualTo(Locale.GERMAN);
    }

    @Test
    void produceLocale_whenRequestHasLocaleENGLISH_thenReturnsENGLISH() {
        when(headersMock.getAcceptableLanguages()).thenReturn(List.of(Locale.ENGLISH));

        final Locale actual = producer.produceLocale();

        assertThat(actual).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void produceLocale_whenRequestHasMultipleLocales_thenReturnsFirstSupportedLocale() {
        when(headersMock.getAcceptableLanguages()).thenReturn(List.of(Locale.ENGLISH, Locale.GERMAN));

        final Locale actual = producer.produceLocale();

        assertThat(actual).isEqualTo(Locale.ENGLISH);
    }
}

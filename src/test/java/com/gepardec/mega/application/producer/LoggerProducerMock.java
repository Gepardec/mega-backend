package com.gepardec.mega.application.producer;

import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.mock;

/**
 * Alternative implementation of the LoggerProducer for unit testing purposes
 */
@ApplicationScoped
@Mock
public class LoggerProducerMock {

    /**
     * Produced bean must be normal-scoped.
     * @return Mock instance of a Logger
     */
    @Produces
    @ApplicationScoped
    Logger createLogger() {
        return mock(Logger.class);
    }
}

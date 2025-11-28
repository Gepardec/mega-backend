package com.gepardec.mega.application.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.time.Clock;

@ApplicationScoped
public class ClockProducer {

    @ApplicationScoped
    @Produces
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}

package com.gepardec.mega.application.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.time.Clock;

public class ClockProducer {

    @ApplicationScoped
    @Produces
    public Clock produceClock() {
        return Clock.systemDefaultZone();
    }
}

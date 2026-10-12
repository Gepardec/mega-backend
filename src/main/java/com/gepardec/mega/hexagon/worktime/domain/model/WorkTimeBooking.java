package com.gepardec.mega.hexagon.worktime.domain.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public sealed interface WorkTimeBooking permits ProjectBooking, JourneyBooking {
    LocalDateTime from();
    LocalDateTime to();
    Task task();
    WorkingLocation workingLocation();
    boolean workLocationProjectRelevant();

    default LocalDate date() {
        return from().toLocalDate();
    }

    default double durationInHours() {
        return Duration.between(from(), to()).toMinutes() / 60d;
    }
}

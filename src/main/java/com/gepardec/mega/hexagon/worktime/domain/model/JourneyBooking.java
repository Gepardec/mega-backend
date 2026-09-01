package com.gepardec.mega.hexagon.worktime.domain.model;

import java.time.LocalDateTime;

public record JourneyBooking(
        LocalDateTime from,
        LocalDateTime to,
        Task task,
        WorkingLocation workingLocation,
        boolean workLocationProjectRelevant,
        JourneyDirection direction,
        Vehicle vehicle
) implements WorkTimeBooking {
}

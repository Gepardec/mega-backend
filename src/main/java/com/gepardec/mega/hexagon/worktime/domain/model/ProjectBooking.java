package com.gepardec.mega.hexagon.worktime.domain.model;

import java.time.LocalDateTime;

public record ProjectBooking(
        LocalDateTime from,
        LocalDateTime to,
        Task task,
        WorkingLocation workingLocation,
        boolean workLocationProjectRelevant,
        Integer projectZepId,
        String note,
        String process
) implements WorkTimeBooking {
}

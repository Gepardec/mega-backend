package com.gepardec.mega.hexagon.user.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.HourlyRate;

import java.time.LocalDate;
import java.util.Objects;

public record InternalRateUpdateCommand(
        ZepUsername zepUsername,
        HourlyRate hourlyRate,
        LocalDate effectiveFrom
) {

    public InternalRateUpdateCommand {
        Objects.requireNonNull(zepUsername, "zepUsername must not be null");
        Objects.requireNonNull(hourlyRate, "hourlyRate must not be null");
        Objects.requireNonNull(effectiveFrom, "effectiveFrom must not be null");
    }
}

package com.gepardec.mega.hexagon.monthend.domain.model;

import java.time.YearMonth;
import java.util.Objects;

public record MonthEndTaskGenerationResult(
        YearMonth month,
        int created,
        int skipped
) {

    public MonthEndTaskGenerationResult {
        Objects.requireNonNull(month, "month must not be null");
    }
}

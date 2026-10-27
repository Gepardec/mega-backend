package com.gepardec.mega.hexagon.worktime.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * @param activeFrom first date the rule applies to, or {@code null} if it applies to every date
 */
public record WarningRuleSetting(WarningRule rule, boolean enabled, LocalDate activeFrom) {

    public WarningRuleSetting {
        Objects.requireNonNull(rule, "rule must not be null");
    }
}

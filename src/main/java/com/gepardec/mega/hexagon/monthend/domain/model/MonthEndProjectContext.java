package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.YearMonth;
import java.util.Objects;
import java.util.Set;

public record MonthEndProjectContext(
        YearMonth month,
        MonthEndProjectSnapshot project,
        Set<UserId> eligibleProjectLeadIds
) {

    public MonthEndProjectContext {
        Objects.requireNonNull(month, "month must not be null");
        Objects.requireNonNull(project, "project must not be null");
        Objects.requireNonNull(eligibleProjectLeadIds, "eligibleProjectLeadIds must not be null");

        eligibleProjectLeadIds = Set.copyOf(eligibleProjectLeadIds);
    }
}

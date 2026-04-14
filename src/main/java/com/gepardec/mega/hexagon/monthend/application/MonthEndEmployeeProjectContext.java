package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;

import java.time.YearMonth;
import java.util.Objects;
import java.util.Set;

public record MonthEndEmployeeProjectContext(
        YearMonth month,
        MonthEndProjectSnapshot project,
        UserRef subjectEmployee,
        Set<UserId> eligibleProjectLeadIds
) {

    public MonthEndEmployeeProjectContext {
        Objects.requireNonNull(month, "month must not be null");
        Objects.requireNonNull(project, "project must not be null");
        Objects.requireNonNull(subjectEmployee, "subjectEmployee must not be null");
        Objects.requireNonNull(eligibleProjectLeadIds, "eligibleProjectLeadIds must not be null");

        eligibleProjectLeadIds = Set.copyOf(eligibleProjectLeadIds);
    }
}

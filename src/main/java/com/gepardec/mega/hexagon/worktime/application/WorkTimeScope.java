package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;

import java.util.Map;
import java.util.Optional;

/**
 * Which employees a work time report covers and how booked ZEP projects are resolved.
 * Selected per capacity; everything after that is shared.
 *
 * @param unresolvedProjectsExpected whether bookings on projects the resolver cannot find are normal
 *                                   (controlling: the lead doesn't lead every project) or point to a
 *                                   missing snapshot (employee)
 */
record WorkTimeScope(
        Map<String, UserRef> employeesByZepUsername,
        ProjectResolver projects,
        boolean unresolvedProjectsExpected
) {

    @FunctionalInterface
    interface ProjectResolver {

        Optional<ProjectRef> resolve(Integer projectZepId);
    }
}

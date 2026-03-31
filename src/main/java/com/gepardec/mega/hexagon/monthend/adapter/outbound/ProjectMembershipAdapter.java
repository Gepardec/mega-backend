package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectAssignmentPort;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.service.ProjectService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectMembershipAdapter implements MonthEndProjectAssignmentPort {

    @Inject
    ProjectService projectService;

    @Override
    public Set<String> findAssignedUsernames(int zepProjectId, YearMonth month) {
        return projectService.getProjectEmployeesForId(zepProjectId, month).stream()
                .map(ZepProjectEmployee::username)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

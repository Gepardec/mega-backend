package com.gepardec.mega.hexagon.project.adapter.outbound;

import com.gepardec.mega.hexagon.project.domain.model.ZepProjectProfile;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ZepProjectPort;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.service.ProjectService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ZepProjectAdapter implements ZepProjectPort {

    @Inject
    ProjectService projectService;

    @Inject
    ZepProjectMapper mapper;

    @Override
    public List<ZepProjectProfile> fetchAll() {
        return projectService.getAllProjects().stream()
                .map(mapper::toProfile)
                .toList();
    }

    @Override
    public List<String> fetchLeadUsernames(int zepId) {
        // Passing null for YearMonth skips the activity filter — we want all current leads
        return projectService.getProjectEmployeesForId(zepId, null).stream()
                .filter(this::isLead)
                .map(ZepProjectEmployee::username)
                .toList();
    }

    private boolean isLead(ZepProjectEmployee employee) {
        // Based on ProjectEmployeesMapper: type.id() != 0 indicates a lead
        return employee.type() != null && employee.type().id() != 0;
    }
}

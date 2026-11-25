package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeProjectTaskZepPort;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectTask;
import com.gepardec.mega.zep.rest.service.ProjectService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@ApplicationScoped
public class WorkTimeProjectTaskZepAdapter implements WorkTimeProjectTaskZepPort {

    /** Narrows ZEP's answer to candidates; the exact name rule is applied in the domain. */
    private static final String SEARCH = "reisepauschale";

    private final ProjectService projectService;
    private final WorkTimeProjectTaskZepMapper mapper;

    @Inject
    public WorkTimeProjectTaskZepAdapter(ProjectService projectService, WorkTimeProjectTaskZepMapper mapper) {
        this.projectService = projectService;
        this.mapper = mapper;
    }

    @Override
    public List<ProjectTask> fetchTasks(Set<Integer> projectZepIds) {
        Objects.requireNonNull(projectZepIds, "projectZepIds must not be null");

        return projectZepIds.stream()
                .flatMap(projectZepId -> {
                    Log.debugf("Fetching travel-allowance task candidates for ZEP project %d", projectZepId);
                    return projectService.getProjectTasks(projectZepId, SEARCH).stream();
                })
                .map(mapper::toDomain)
                .toList();
    }
}

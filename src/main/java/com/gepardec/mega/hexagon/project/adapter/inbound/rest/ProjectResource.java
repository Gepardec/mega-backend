package com.gepardec.mega.hexagon.project.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.ProjectApi;
import com.gepardec.mega.hexagon.generated.model.LeistungsnachweisToggleRequestDto;
import com.gepardec.mega.hexagon.generated.model.ProjectSettingsDto;
import com.gepardec.mega.hexagon.project.application.port.inbound.GetProjectSettingsUseCase;
import com.gepardec.mega.hexagon.project.application.port.inbound.SetLeistungsnachweisEnabledUseCase;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.PROJECT_LEAD)
public class ProjectResource implements ProjectApi {

    private final GetProjectSettingsUseCase getProjectSettingsUseCase;
    private final SetLeistungsnachweisEnabledUseCase setLeistungsnachweisEnabledUseCase;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final ProjectRestMapper projectRestMapper;

    @Inject
    public ProjectResource(GetProjectSettingsUseCase getProjectSettingsUseCase,
                           SetLeistungsnachweisEnabledUseCase setLeistungsnachweisEnabledUseCase,
                           AuthenticatedActorContext authenticatedActorContext,
                           ProjectRestMapper projectRestMapper) {
        this.getProjectSettingsUseCase = getProjectSettingsUseCase;
        this.setLeistungsnachweisEnabledUseCase = setLeistungsnachweisEnabledUseCase;
        this.authenticatedActorContext = authenticatedActorContext;
        this.projectRestMapper = projectRestMapper;
    }

    @Override
    public Response getProjectSettings() {
        List<ProjectSettingsDto> projects = projectRestMapper.toDtoList(
                getProjectSettingsUseCase.getLeadProjects(authenticatedActorContext.userId())
        );

        return Response.ok(projects).build();
    }

    @Override
    public Response setLeistungsnachweisEnabled(
            @PathParam("projectId") UUID projectId,
            LeistungsnachweisToggleRequestDto leistungsnachweisToggleRequestDto) {

        setLeistungsnachweisEnabledUseCase.setLeistungsnachweisEnabled(
                ProjectId.of(projectId),
                authenticatedActorContext.userId(),
                leistungsnachweisToggleRequestDto.getEnabled()
        );

        return Response.noContent().build();
    }
}

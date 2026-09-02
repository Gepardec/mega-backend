package com.gepardec.mega.hexagon.project.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.ProjectApi;
import com.gepardec.mega.hexagon.generated.model.ApiErrorDto;
import com.gepardec.mega.hexagon.generated.model.LeistungsnachweisToggleRequestDto;
import com.gepardec.mega.hexagon.generated.model.ProjectItemDto;
import com.gepardec.mega.hexagon.project.application.port.inbound.GetLeadProjectsUseCase;
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

    private final GetLeadProjectsUseCase getLeadProjectsUseCase;
    private final SetLeistungsnachweisEnabledUseCase setLeistungsnachweisEnabledUseCase;
    private final AuthenticatedActorContext authenticatedActorContext;
    private final ProjectRestMapper projectRestMapper;

    @Inject
    public ProjectResource(GetLeadProjectsUseCase getLeadProjectsUseCase,
                           SetLeistungsnachweisEnabledUseCase setLeistungsnachweisEnabledUseCase,
                           AuthenticatedActorContext authenticatedActorContext,
                           ProjectRestMapper projectRestMapper) {
        this.getLeadProjectsUseCase = getLeadProjectsUseCase;
        this.setLeistungsnachweisEnabledUseCase = setLeistungsnachweisEnabledUseCase;
        this.authenticatedActorContext = authenticatedActorContext;
        this.projectRestMapper = projectRestMapper;
    }

    @Override
    public Response getLeadProjects() {
        List<ProjectItemDto> projects = projectRestMapper.toDtoList(
                getLeadProjectsUseCase.getLeadProjects(authenticatedActorContext.userId())
        );

        return Response.ok(projects).build();
    }

    @Override
    public Response setLeistungsnachweisEnabled(
            @PathParam("projectId") UUID projectId,
            LeistungsnachweisToggleRequestDto leistungsnachweisToggleRequestDto) {

        Boolean enabled = leistungsnachweisToggleRequestDto.getEnabled();
        if (enabled == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiErrorDto().message("'enabled' field is required"))
                    .build();
        }

        setLeistungsnachweisEnabledUseCase.setLeistungsnachweisEnabled(
                ProjectId.of(projectId),
                authenticatedActorContext.userId(),
                enabled
        );

        return Response.noContent().build();
    }
}

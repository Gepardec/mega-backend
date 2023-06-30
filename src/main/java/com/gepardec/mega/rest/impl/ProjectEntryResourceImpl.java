package com.gepardec.mega.rest.impl;

import com.gepardec.mega.rest.api.ProjectEntryResource;
import com.gepardec.mega.rest.model.ProjectEntryDto;
import com.gepardec.mega.service.api.ProjectEntryService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Authenticated
public class ProjectEntryResourceImpl implements ProjectEntryResource {

    @Inject
    ProjectEntryService projectEntryService;

    @Override
    public Response update(final ProjectEntryDto projectEntryDTO) {
        return Response.ok(projectEntryService.update(projectEntryDTO)).build();
    }
}

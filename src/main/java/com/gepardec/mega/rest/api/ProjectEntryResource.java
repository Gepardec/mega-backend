package com.gepardec.mega.rest.api;

import com.gepardec.mega.rest.model.ProjectEntryDto;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/projectentry")
@Tag(name = "ProjectEntryResource")
public interface ProjectEntryResource {
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response update(ProjectEntryDto projectEntryDTO);
}

package com.gepardec.mega.rest.api;

import com.gepardec.mega.rest.model.ProjectCommentDto;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/projectcomments")
@Tag(name = "ProjectCommentResource")
public interface ProjectCommentResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response get(
            @QueryParam("date") @NotNull(message = "{projectCommentResource.date.notNull}") String currentMonthYear,
            @QueryParam("projectName") @NotNull(message = "{projectCommentResource.projectName.notNull}") String projectName
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response create(@NotNull(message = "{projectCommentResource.projectCommentEntry.notNull}") ProjectCommentDto newProjectCommentDto);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response update(@NotNull(message = "{projectCommentResource.projectCommentEntry.notNull}") ProjectCommentDto projectCommentDto);
}

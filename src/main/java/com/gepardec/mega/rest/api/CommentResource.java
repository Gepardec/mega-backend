package com.gepardec.mega.rest.api;

import com.gepardec.mega.rest.model.CommentDto;
import com.gepardec.mega.rest.model.NewCommentEntryDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/comments")
public interface CommentResource {
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/finish")
    Response finish(@NotNull(message = "{commentResource.comment.notNull}") CommentDto commentDto);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getallcommentsforemployee")
    Response getAllCommentsForEmployee(
            @QueryParam("email") @NotNull(message = "{commentResource.email.notNull}") @Email(message = "{commentResource.email.invalid}") String employeeEmail,
            @QueryParam("date") @NotNull(message = "{commentResource.date.notNull}") String currentMonthYear);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response newCommentForEmployee(@NotNull(message = "{commentResource.commentEntry.notNull}") NewCommentEntryDto newComment);

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    Response deleteComment(@PathParam("id") @NotNull(message = "{commentResource.id.notNull}") Long id);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response updateCommentForEmployee(@NotNull(message = "{commentResource.comment.notNull}") CommentDto commentDto);
}

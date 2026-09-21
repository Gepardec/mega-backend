package com.gepardec.mega.hexagon.project.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ApiErrorDto;
import com.gepardec.mega.hexagon.project.domain.error.ProjectException;
import com.gepardec.mega.hexagon.project.domain.error.ProjectNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@ApplicationScoped
@Provider
public class ProjectDomainExceptionMapper implements ExceptionMapper<ProjectException> {

    @Override
    public Response toResponse(ProjectException exception) {
        Response.Status status = exception instanceof ProjectNotFoundException
                ? Response.Status.NOT_FOUND
                : Response.Status.BAD_REQUEST;

        return Response.status(status)
                .entity(new ApiErrorDto().message(exception.getMessage()))
                .build();
    }
}

package com.gepardec.mega.personio.employees;

import com.gepardec.mega.personio.commons.factory.PersonioHeadersFactory;
import com.gepardec.mega.personio.commons.model.BaseResponse;
import com.gepardec.mega.personio.employees.absenceBalance.AbsenceBalanceResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/company/employees")
@ApplicationScoped
@RegisterRestClient(configKey = "personio-api-v1")
@RegisterClientHeaders(PersonioHeadersFactory.class)
public interface PersonioEmployeesClient {

    /**
     * Fetches information about an employee based on the email address.
     * Due to limitations of RESTEasy, a plain response object needs to be returned.
     * This is because all responses from the Personio API contain a payload even when they have an error status code.
     *
     * @param email Email address of the employee.
     * @return Response which is actually a {@code BaseResponse<List<EmployeesResponse>>}.
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    BaseResponse<List<EmployeesResponse>> getByEmail(@QueryParam("email") String email);

    @GET
    @Path("/{id}/absences/balance")
    BaseResponse<List<AbsenceBalanceResponse>> getAbsenceBalanceForEmployeeById(@PathParam("id") int id);
}

package com.gepardec.mega.rest.api;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.rest.model.BillDto;
import com.gepardec.mega.rest.model.MonthlyAbsencesDto;
import com.gepardec.mega.rest.model.MonthlyOfficeTimesDto;
import com.gepardec.mega.rest.model.ProjectHoursSummaryDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.YearMonth;
import java.util.List;

@Path("/worker")
@Tag(name = "WorkerResource")
@Produces(MediaType.APPLICATION_JSON)
public interface WorkerResource {
    @GET
    @Path("/monthendreports")
    Response monthlyReport();

    @GET
    @Path("/monthendreports/{year}/{month}")
    Response monthlyReport(@PathParam("year") Integer year, @PathParam("month") Integer month);

    @Operation(operationId = "getBillsForEmployeeByMonth", description = "Get all bills that the user with given id uploaded for current month.")
    @APIResponse(responseCode = "200",
            description = "Successfully retrieved bills for employee.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = BillDto[].class))
            }
    )
    @Parameter(name = "id", description = "ID of the employee for whom the bills are to be retrieved.")
    @Parameter(name = "from",
            description = "If not given uses the whole current month. <br> " +
                    "If given uses the whole month of the parameter-date. <br>" +
                    "For example if 2024-03 is given it retrieves all bills from 2024-03-01 to 2024-03-31.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Path("/{id}/bills")
    @GET
    List<BillDto> getBillsForEmployeeByMonth(@PathParam(value = "id") String employeeId, @QueryParam("from") YearMonth from);

    @Operation(operationId = "getAllProjectsForMonthAndEmployee", description = "Get all projects for the employee with given id and for current month.")
    @APIResponse(responseCode = "200",
            description = "Successfully retrieved projects for employee.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ProjectHoursSummaryDto[].class))
            }
    )
    @Parameter(name = "id", description = "ID of the employee for whom the projects are to be retrieved.")
    @Parameter(name = "from",
            description = "If not given uses the whole current month. <br> " +
                    "If given uses the whole month of the parameter-date. <br>" +
                    "For example if 2024-03 is given it retrieves all projects from 2024-03-01 to 2024-03-31.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Path("/{id}/projects")
    @GET
    List<ProjectHoursSummaryDto> getAllProjectsForMonthAndEmployee(@PathParam(value = "id") String employeeId, @QueryParam("from") YearMonth from);

    @Operation(operationId = "getAllAbsencesForMonthAndEmployee", description = "Get absences (inclusive doctor's visiting time and available vacation days) for the employee with given id and for current month.")
    @APIResponse(responseCode = "200",
            description = "Successfully retrieved absences for employee.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MonthlyAbsencesDto.class))
            }
    )
    @Parameter(name = "id", description = "ID of the employee for whom the absences are to be retrieved.")
    @Parameter(name = "from",
            description = "If not given uses the whole current month. <br> " +
                    "If given uses the whole month of the parameter-date. <br>" +
                    "For example if 2024-03 is given it retrieves all absences from 2024-03-01 to 2024-03-31.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Path("/{id}/absences")
    @GET
    MonthlyAbsencesDto getAllAbsencesForMonthAndEmployee(@PathParam(value = "id") String employeeId, @QueryParam("from") YearMonth from);

    @Operation(operationId = "getOfficeTimesForMonthAndEmployee", description = "Get office times, homeoffice times and fridays spent in the office for the employee with given id and for current month.")
    @APIResponse(responseCode = "200",
            description = "Successfully retrieved office times for employee.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MonthlyOfficeTimesDto.class))
            }
    )
    @Parameter(name = "id", description = "ID of the employee for whom the office times are to be retrieved.")
    @Parameter(name = "from",
            description = "If not given uses the whole current month. <br> " +
                    "If given uses the whole month of the parameter-date. <br>" +
                    "For example if 2024-03 is given it retrieves all office times from 2024-03-01 to 2024-03-31.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING, example = "yyyy-MM"))
    @Path("/{id}/officetimes")
    @GET
    MonthlyOfficeTimesDto getOfficeTimesForMonthAndEmployee(@PathParam(value = "id") String employeeId, @QueryParam("from") YearMonth from);

}

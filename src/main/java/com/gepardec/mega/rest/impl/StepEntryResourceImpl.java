package com.gepardec.mega.rest.impl;

import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.rest.api.StepEntryResource;
import com.gepardec.mega.rest.model.EmployeeStepDto;
import com.gepardec.mega.rest.model.ProjectStepDto;
import com.gepardec.mega.rest.model.UpdateEmployeeStepDto;
import com.gepardec.mega.service.api.StepEntryService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;

@RequestScoped
@Authenticated
public class StepEntryResourceImpl implements StepEntryResource {

    @Inject
    StepEntryService stepEntryService;

    @Inject
    UserContext userContext;

    @Override
    public Response close(final EmployeeStepDto employeeStepDto) {
        LocalDate from = DateUtils.getFirstDayOfCurrentMonth(employeeStepDto.currentMonthYear());
        LocalDate to = DateUtils.getLastDayOfCurrentMonth(employeeStepDto.currentMonthYear());

        return Response.ok(stepEntryService.setOpenAndAssignedStepEntriesDone(employeeStepDto.employee(), employeeStepDto.stepId(), from, to)).build();
    }

    @Override
    public Response updateEmployeeStateForOffice(UpdateEmployeeStepDto updateEmployeeStepDto) {
        LocalDate from = DateUtils.getFirstDayOfCurrentMonth(updateEmployeeStepDto.currentMonthYear());
        LocalDate to = DateUtils.getLastDayOfCurrentMonth(updateEmployeeStepDto.currentMonthYear());

        return Response.ok(stepEntryService.updateStepEntryStateForEmployee(updateEmployeeStepDto.employee(),
                updateEmployeeStepDto.stepId(), from, to, updateEmployeeStepDto.newState(), updateEmployeeStepDto.newStateReason())).build();
    }

    @Override
    public Response updateEmployeeStateForProject(final ProjectStepDto projectStepDto) {
        return Response.ok(stepEntryService.updateStepEntryStateForEmployeeInProject(
                projectStepDto.employee(),
                projectStepDto.stepId(),
                projectStepDto.projectName(),
                projectStepDto.currentMonthYear(),
                projectStepDto.newState()
        )).build();
    }
}

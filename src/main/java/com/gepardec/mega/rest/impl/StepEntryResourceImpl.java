package com.gepardec.mega.rest.impl;

import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.rest.api.StepEntryResource;
import com.gepardec.mega.rest.mapper.EmployeeMapper;
import com.gepardec.mega.rest.model.EmployeeStepDto;
import com.gepardec.mega.rest.model.ProjectStepDto;
import com.gepardec.mega.rest.model.UpdateEmployeeStepDto;
import com.gepardec.mega.service.api.StepEntryService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.YearMonth;

@RequestScoped
@Authenticated
public class StepEntryResourceImpl implements StepEntryResource {

    @Inject
    StepEntryService stepEntryService;

    @Inject
    EmployeeMapper employeeMapper;

    @Override
    public Response close(final EmployeeStepDto employeeStepDto) {
        return Response.ok(
                        stepEntryService.setOpenAndAssignedStepEntriesDone(
                                employeeMapper.mapToDomain(employeeStepDto.getEmployee()),
                                employeeStepDto.getStepId(),
                                YearMonth.from(LocalDate.parse(employeeStepDto.getCurrentMonthYear()))
                        )
                )
                .build();
    }

    @Override
    public Response updateEmployeeStateForOffice(UpdateEmployeeStepDto updateEmployeeStepDto) {
        LocalDate from = DateUtils.getFirstDayOfCurrentMonth(updateEmployeeStepDto.getCurrentMonthYear());
        LocalDate to = DateUtils.getLastDayOfCurrentMonth(updateEmployeeStepDto.getCurrentMonthYear());

        return Response.ok(
                        stepEntryService.updateStepEntryStateForEmployee(
                                employeeMapper.mapToDomain(updateEmployeeStepDto.getEmployee()),
                                updateEmployeeStepDto.getStepId(),
                                from,
                                to,
                                updateEmployeeStepDto.getNewState(),
                                updateEmployeeStepDto.getNewStateReason()
                        )
                )
                .build();
    }

    @Override
    public Response updateEmployeeStateForProject(final ProjectStepDto projectStepDto) {
        return Response.ok(
                stepEntryService.updateStepEntryStateForEmployeeInProject(
                        employeeMapper.mapToDomain(projectStepDto.getEmployee()),
                        projectStepDto.getStepId(),
                        projectStepDto.getProjectName(),
                        projectStepDto.getCurrentMonthYear(),
                        projectStepDto.getNewState()
                )
        ).build();
    }
}

package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.RolesAllowed;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.rest.api.EmployeeResource;
import com.gepardec.mega.rest.mapper.MapperManager;
import com.gepardec.mega.rest.model.EmployeeDto;
import com.gepardec.mega.service.api.EmployeeService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.List;

@RequestScoped
@Authenticated
@RolesAllowed({Role.PROJECT_LEAD, Role.OFFICE_MANAGEMENT})
public class EmployeeResourceImpl implements EmployeeResource {

    @Inject
    MapperManager mapper;

    @Inject
    EmployeeService employeeService;

    @Override
    public Response list() {
        final List<Employee> allActiveEmployees = employeeService.getAllActiveEmployees();
        return Response.ok(mapper.mapAsList(allActiveEmployees, Employee.class, EmployeeDto.class)).build();
    }

    @Override
    public Response update(final List<EmployeeDto> employeesDto) {
        return Response.ok(employeeService.updateEmployeesReleaseDate(mapper.mapAsList(employeesDto, EmployeeDto.class, Employee.class))).build();
    }
}

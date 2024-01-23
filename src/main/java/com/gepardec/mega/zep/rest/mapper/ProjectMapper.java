package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.BillabilityPreset;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepProject;
import com.gepardec.mega.zep.rest.entity.ZepProjectEmployee;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectMapper {

    public static List<Employee> mapList(List<ZepEmployee> zepEmployees) {
        if (zepEmployees == null)
            return null;

        return zepEmployees.stream()
                .map(EmployeeMapper::map)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static Project map(ZepProject zepProject) {
        if (zepProject == null)
            return null;

        LocalDate startDate = zepProject.getStartDate() == null ?
                null : zepProject.getStartDate().toLocalDate();
        LocalDate endDate = zepProject.getEndDate() == null ?
                null : zepProject.getEndDate().toLocalDate();

        List<String> employees = new ArrayList<>();
        List<String> leads = new ArrayList<>();

        fillEmployeesLeadLists(employees, leads, zepProject);

        BillabilityPreset billabilityPreset = null;
        if (BillabilityPreset.byZepId(zepProject.getBillingType()).isPresent()) {
            billabilityPreset = BillabilityPreset.byZepId(zepProject.getBillingType()).get();
        }

        return Project.builder()
                .zepId(zepProject.getId())
                .projectId(zepProject.getName())
                .startDate(startDate)
                .endDate(endDate)
                .billabilityPreset(billabilityPreset)
                .employees(employees)
                .leads(leads)
                .build();
        /**
         * Austrittsdatum, wird durch Aufruf von employeeService.getAllEmployeesConsideringExitDate befüllt,
         * wenn Mitarbeiter inaktiv ist.
         */
    }

    public static void fillEmployeesLeadLists(List<String> employees, List<String> leads, ZepProject project) {
        project.getEmployees().forEach(employee -> {
            employees.add(employee.getUsername());
            if (employee.isLead()) {
                leads.add(employee.getUsername());
            }
        });
    }
}

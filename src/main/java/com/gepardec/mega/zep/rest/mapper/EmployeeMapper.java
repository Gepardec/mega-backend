package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.mapper.MapperUtil;
import com.gepardec.mega.zep.mapper.ProjectTimeMapper;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmployment;
import com.gepardec.mega.zep.rest.entity.ZepRights;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class EmployeeMapper {

    public static List<Employee> mapList(List<ZepEmployee> zepEmployees) {
        if (zepEmployees == null)
            return null;

        return zepEmployees.stream()
                .map(EmployeeMapper::map)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static Employee map(ZepEmployee zepEmployee) {
        if (zepEmployee == null)
            return null;

        boolean active = true;                         //TODO: Active check

        return Employee.builder()
            .userId(zepEmployee.getPersonalNumber())
            .email(zepEmployee.getEmail())
            .title(zepEmployee.getTitle())
            .firstname(zepEmployee.getFirstname())
            .lastname(zepEmployee.getLastname())
            .salutation(zepEmployee.getSalutation())
            .releaseDate(zepEmployee.getReleaseDate())
            .workDescription(null) //TODO: klären was das ist (getPreisgruppe() in Employee Mapper)
            .language(zepEmployee.getLanguage())
            .regularWorkingHours(null)
            .active(active)
            .build();
        /**
         * Austrittsdatum, wird durch Aufruf von employeeService.getAllEmployeesConsideringExitDate befüllt,
         * wenn Mitarbeiter inaktiv ist.
         */
    }

}

package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.mapper.MapperUtil;
import com.gepardec.mega.zep.mapper.ProjectTimeMapper;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmployment;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.entity.ZepRights;
import com.gepardec.mega.zep.rest.service.EmploymentPeriodService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmployeeMapper {

    @Inject
    EmploymentPeriodService employmentPeriodService;

    public List<Employee> mapList(List<ZepEmployee> zepEmployees) {
        if (zepEmployees == null)
            return null;

        return zepEmployees.stream()
                .map(this::map)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Employee map(ZepEmployee zepEmployee) {
        if (zepEmployee == null)
            return null;

        boolean active = getActiveOfZepEmploymentPeriods(zepEmployee.getEmploymentPeriods());

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

    public static boolean getActiveOfZepEmploymentPeriods(ZepEmploymentPeriod[] zepEmploymentPeriods) {

        if (Arrays.stream(zepEmploymentPeriods).allMatch(Objects::isNull)) {
            return false;
        }

        return Arrays.stream(zepEmploymentPeriods)
                .anyMatch(zepEmploymentPeriod -> zepEmploymentPeriod.getEndDate() == null);

    }

}

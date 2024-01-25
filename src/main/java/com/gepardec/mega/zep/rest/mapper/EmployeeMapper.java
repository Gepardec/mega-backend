package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.mapper.MapperUtil;
import com.gepardec.mega.zep.mapper.ProjectTimeMapper;
import com.gepardec.mega.zep.rest.entity.*;
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
public class EmployeeMapper implements Mapper<Employee, ZepEmployee> {

    @Override
    public Employee map(ZepEmployee zepEmployee) {
        if (zepEmployee == null)
            return null;

        boolean active = getActiveOfZepEmploymentPeriods(zepEmployee.getEmploymentPeriods());

        String salutation = zepEmployee.getSalutation() == null ?
                null : zepEmployee.getSalutation().getName();
        String releaseDate = zepEmployee.getReleaseDate() == null ?
                null : zepEmployee.getReleaseDate().toString();
        return Employee.builder()
            .userId(zepEmployee.getPersonalNumber())
            .email(zepEmployee.getEmail())
            .title(zepEmployee.getTitle())
            .firstname(zepEmployee.getFirstname())
            .lastname(zepEmployee.getLastname())
            .salutation(salutation)
            .releaseDate(releaseDate)
            .workDescription(null) //TODO: klären was das ist (getPreisgruppe() in Employee Mapper)
            .language(zepEmployee.getLanguage())
            .regularWorkingHours(zepEmployee.getRegularWorkingHours())
            .active(active)
            .build();
        /**
         * Austrittsdatum, wird durch Aufruf von employeeService.getAllEmployeesConsideringExitDate befüllt,
         * wenn Mitarbeiter inaktiv ist.
         */
    }

    public static boolean getActiveOfZepEmploymentPeriods(ZepEmploymentPeriod[] zepEmploymentPeriods) {
        if (zepEmploymentPeriods == null) {
            return false;
        }

        if (Arrays.stream(zepEmploymentPeriods).allMatch(Objects::isNull)) {
            return false;
        }

        return Arrays.stream(zepEmploymentPeriods)
                .anyMatch(zepEmploymentPeriod -> zepEmploymentPeriod.getEndDate() == null);
    }



}

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

        String salutation = zepEmployee.getSalutation() == null ?
                null : zepEmployee.getSalutation().getName();
        String releaseDate = zepEmployee.getReleaseDate() == null ?
                null : zepEmployee.getReleaseDate().toString();
        String language = zepEmployee.getLanguage() == null ?
                null : zepEmployee.getLanguage().getId();
        return Employee.builder()
            .userId(zepEmployee.getUsername())
            .email(zepEmployee.getEmail())
            .title(zepEmployee.getTitle())
            .firstname(zepEmployee.getFirstname())
            .lastname(zepEmployee.getLastname())
            .salutation(salutation)
            .releaseDate(releaseDate)
            .workDescription(null) //TODO: klären was das ist (getPreisgruppe() in Employee Mapper)
            .language(language)
            .build();
        /**
         * Austrittsdatum, wird durch Aufruf von employeeService.getAllEmployeesConsideringExitDate befüllt,
         * wenn Mitarbeiter inaktiv ist.
         */
    }

    public boolean getActiveOfZepEmploymentPeriods(List<ZepEmploymentPeriod> zepEmploymentPeriods) {
        if (zepEmploymentPeriods == null) {
            return false;
        }

        if (zepEmploymentPeriods.stream().allMatch(Objects::isNull)) {
            return false;
        }

        return zepEmploymentPeriods.stream()
                .anyMatch(zepEmploymentPeriod -> zepEmploymentPeriod.getEndDate() == null);
    }



}
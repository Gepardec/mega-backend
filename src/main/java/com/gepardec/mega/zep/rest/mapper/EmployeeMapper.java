package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class EmployeeMapper implements Mapper<Employee, ZepEmployee> {

    @Inject
    Logger logger;

    @Override
    public Employee map(ZepEmployee zepEmployee) {
        if (zepEmployee == null) {
            logger.info("ZEP REST implementation -- While trying to map ZepEmployee to Employee, ZepEmployee was null");
            return null;
        }

        try {

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
                    .workDescription(zepEmployee.getPriceGroup())
                    .language(language)
                    .build();
            /**
             * Austrittsdatum, wird durch Aufruf von employeeService.getAllEmployeesConsideringExitDate befüllt,
             * wenn Mitarbeiter inaktiv ist.
             */
        } catch (Exception e) {
            throw new ZepServiceException("While trying to map ZepEmployee to Employee, an error occurred", e);
        }

    }

    public static boolean getActiveOfZepEmploymentPeriods(List<ZepEmploymentPeriod> zepEmploymentPeriods) {
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

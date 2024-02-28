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

            String salutation = zepEmployee.salutation() == null ?
                    null : zepEmployee.salutation().name();
            String releaseDate = zepEmployee.releaseDate() == null ?
                    null : zepEmployee.releaseDate().toString();
            String language = zepEmployee.language() == null ?
                    null : zepEmployee.language().id();
            return Employee.builder()
                    .userId(zepEmployee.username())
                    .email(zepEmployee.email())
                    .title(zepEmployee.title())
                    .firstname(zepEmployee.firstname())
                    .lastname(zepEmployee.lastname())
                    .salutation(salutation)
                    .releaseDate(releaseDate)
                    .workDescription(zepEmployee.priceGroup())
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

}

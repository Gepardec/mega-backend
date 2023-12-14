package com.gepardec.mega.application.exception.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;

@Provider
public class HibernateConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Inject
    Logger logger;

    @Override
    public Response toResponse(ConstraintViolationException e) {
        logger.error(getLogMessage(e));
        return Response.status(400).build();
    }

    private String getLogMessage(ConstraintViolationException e) {
        switch (e.getConstraintName()) {
            case "uc_premature_employee_check_userid_and_formonth":
                String prematureEmployeeDetails = e.getMessage()
                        .split("\\(user_id, for_month\\)=\\(")[1].split("\\) already exists.")[0];
                return String.format("Tried to add a PrematureEmployeeCheck but there already exists one with following UserId and Month: %s", prematureEmployeeDetails);

            default:
                return "Database encountered a unknown constraint violation without matching handling";
        }

    }
}

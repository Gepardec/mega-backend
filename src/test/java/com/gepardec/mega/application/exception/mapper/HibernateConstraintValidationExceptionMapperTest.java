package com.gepardec.mega.application.exception.mapper;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.slf4j.Logger;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@QuarkusTest
class HibernateConstraintValidationExceptionMapperTest {

    @Inject
    HibernateConstraintViolationExceptionMapper hibernateConstraintViolationExceptionMapper;

    @Spy
    Logger logger;

    @BeforeEach
    void setup() {
        logger = spy(Logger.class);
        hibernateConstraintViolationExceptionMapper.logger = logger;
    }

    @Test
    void when_ConstraintViolationExcpetion_THEN_LOG_WITH_STATUS_400() {
        final Response response = hibernateConstraintViolationExceptionMapper.toResponse(new ConstraintViolationException("""
                 ERROR: duplicate key value violates unique constraint "uc_premature_employee_check_userid_and_formonth"
                  Detail: Key (user_id, for_month)=(18, 2023-11-01) already exists.""", new SQLException(), "uc_premature_employee_check_userid_and_formonth"));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(logger, times(1)).error(contains("PrematureEmployeeCheck"));
    }
}

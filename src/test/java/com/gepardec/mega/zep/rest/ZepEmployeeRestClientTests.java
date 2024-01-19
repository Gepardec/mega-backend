package com.gepardec.mega.zep.rest;

import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.service.EmployeeService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class ZepEmployeeRestClientTests {

    @Inject
    EmployeeService zepEmployeeService;

    @Test
    public void getRegularWorkingTime_validUser(){
        ZepRegularWorkingTimes regularWorkingTimes = zepEmployeeService.getRegularWorkingTimesByUsername("082-tmeindl");
        System.out.println(regularWorkingTimes.getEmployee_id());
    }

    @Test
    public void getRegularWorkingTime_invalidUser(){
        assertThrows(ZepServiceException.class, () -> {
            zepEmployeeService.getRegularWorkingTimesByUsername("uff-wrong");
        });
    }
}

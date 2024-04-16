package com.gepardec.mega.service.api;

import com.gepardec.mega.rest.model.EmployeeDto;

import java.util.List;

/**
 * @author Thomas Herzog <herzog.thomas81@gmail.com>
 * @since 10/3/2020
 */
public interface SyncService {

    void syncEmployees();
    List<EmployeeDto> syncUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth();

}

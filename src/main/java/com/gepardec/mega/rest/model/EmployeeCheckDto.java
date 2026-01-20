package com.gepardec.mega.rest.model;

import com.gepardec.mega.db.entity.employee.EmployeeState;

import java.util.List;

public record EmployeeCheckDto(
        EmployeeDto employee,
        EmployeeState employeeCheckState,
        String employeeCheckStateReason,
        EmployeeState internalCheckState,
        boolean otherChecksDone,
        List<CommentDto> comments,
        PrematureEmployeeCheckDto prematureEmployeeCheck
) {
}

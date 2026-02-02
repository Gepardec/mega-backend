package com.gepardec.mega.domain.model;

import java.util.List;

public record EmployeeCheck(
        Employee employee,
        EmployeeState employeeCheckState,
        String employeeCheckStateReason,
        EmployeeState internalCheckState,
        boolean otherChecksDone,
        List<Comment> comments,
        PrematureEmployeeCheck prematureEmployeeCheck
) {
}

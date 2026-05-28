package com.gepardec.mega.rest.model;

import com.gepardec.mega.domain.model.State;

import java.util.List;

public record OfficeManagementEntryDto(
        EmployeeDto employee,
        State employeeCheckState,
        String employeeCheckStateReason,
        State internalCheckState,
        State projectCheckState,
        List<PmProgressDto> employeeProgresses,
        long totalComments,
        long finishedComments,
        String entryDate
) {
}

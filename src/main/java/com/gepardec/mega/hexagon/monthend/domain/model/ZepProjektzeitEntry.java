package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;

import java.time.LocalDate;

public record ZepProjektzeitEntry(
        LocalDate date,
        String timeFrom,
        String timeTo,
        String message,
        ZepUsername zepIdErsteller,
        String employeeFirstName,
        String employeeLastName,
        String projectName,
        String task,
        String remark,
        String clarification
) {
}

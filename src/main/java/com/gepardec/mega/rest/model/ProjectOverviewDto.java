package com.gepardec.mega.rest.model;

import com.gepardec.mega.domain.model.ProjectState;
import com.gepardec.mega.domain.model.State;

public record ProjectOverviewDto(
        Integer zepId,
        String name,
        State employeesChecked,
        ProjectState controllingState,
        ProjectState billingState,
        ProjectCommentDto comment
) {
}

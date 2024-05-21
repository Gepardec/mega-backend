package com.gepardec.mega.rest.mapper;

import com.gepardec.mega.domain.model.ProjectHoursSummary;
import com.gepardec.mega.rest.model.ProjectHoursSummaryDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProjectHoursSummaryMapper implements DtoMapper<ProjectHoursSummary, ProjectHoursSummaryDto> {
    @Override
    public ProjectHoursSummaryDto mapToDto(ProjectHoursSummary object) {
        return ProjectHoursSummaryDto.builder()
                .projectName(object.getProjectName())
                .billableHoursSum(object.getBillableHoursSum())
                .nonBillableHoursSum(object.getNonBillableHoursSum())
                .chargeability(object.getChargeability())
                .isInternalProject(object.getIsInternalProject())
                .build();
    }

    @Override
    public ProjectHoursSummary mapToDomain(ProjectHoursSummaryDto object) {
        return ProjectHoursSummary.builder()
                .projectName(object.getProjectName())
                .billableHoursSum(object.getBillableHoursSum())
                .nonBillableHoursSum(object.getNonBillableHoursSum())
                .chargeability(object.getChargeability())
                .isInternalProject(object.getIsInternalProject())
                .build();
    }
}

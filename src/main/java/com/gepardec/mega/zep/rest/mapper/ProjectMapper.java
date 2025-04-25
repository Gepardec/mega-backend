package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.BillabilityPreset;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;

@ApplicationScoped
public class ProjectMapper implements Mapper<Project.Builder, ZepProject> {
    @Inject
    Logger logger;

    public Project.Builder map(ZepProject zepProject) {
        if (zepProject == null) {
            logger.info("ZEP REST implementation -- While trying to map ZepProject to Project, ZepProject was null");
            return null;
        }

        try {
            LocalDate startDate = zepProject.startDate() == null
                    ? null
                    : zepProject.startDate().toLocalDate();
            LocalDateTime endDateTime = zepProject.endDate();
            LocalDate endDate = endDateTime == null
                    ? LocalDate.now().plusYears(5).with(TemporalAdjusters.lastDayOfYear())
                    : endDateTime.toLocalDate();
            BillabilityPreset billabilityPreset = BillabilityPreset.byZepId(zepProject.billingType().id()).orElse(null);

            return Project.builder()
                    .zepId(zepProject.id())
                    .projectId(zepProject.name())
                    .startDate(startDate)
                    .endDate(endDate)
                    .billabilityPreset(billabilityPreset)
                    .employees(Collections.emptyList())
                    .leads(Collections.emptyList())
                    .categories(Collections.emptyList());
        } catch (Exception e) {
            throw new ZepServiceException("While trying to map ZepProject to Project, an error occurred", e);
        }
    }
}

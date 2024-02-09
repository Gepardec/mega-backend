package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.BillabilityPreset;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.zep.rest.entity.ZepProject;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProjectMapper implements Mapper<Project, ZepProject> {
    public Project map(ZepProject zepProject) {
        if (zepProject == null)
            return null;

        LocalDate startDate = zepProject.getStartDate() == null ?
                null : zepProject.getStartDate().toLocalDate();
        LocalDateTime endDateTime = zepProject.getEndDate();
        LocalDate endDate;
        endDate = endDateTime == null ?
                LocalDate.now().plusYears(5).with(TemporalAdjusters.lastDayOfYear())
                : endDateTime.toLocalDate();

        List<String> employees = new ArrayList<>();
        List<String> leads = new ArrayList<>();

        BillabilityPreset billabilityPreset = null;
        if (BillabilityPreset.byZepId(zepProject.getBillingType()).isPresent()) {
            billabilityPreset = BillabilityPreset.byZepId(zepProject.getBillingType()).get();
        }

        return Project.builder()
                .zepId(zepProject.getId())
                .projectId(zepProject.getName())
                .startDate(startDate)
                .endDate(endDate)
                .billabilityPreset(billabilityPreset)
                .employees(employees)
                .leads(leads)
                .categories(List.of("CONS"))
                .build();
        /**
         * Austrittsdatum, wird durch Aufruf von employeeService.getAllEmployeesConsideringExitDate befüllt,
         * wenn Mitarbeiter inaktiv ist.
         */
    }

}

package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.application.port.outbound.ZepEmployeePort;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.HourlyRate;
import com.gepardec.mega.hexagon.user.domain.model.ZepEmployeeSyncData;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.dto.ZepEmployeeReleaseDateUpdate;
import com.gepardec.mega.zep.rest.service.EmployeeService;
import com.gepardec.mega.zep.rest.service.EmploymentPeriodService;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ZepEmployeeAdapter implements ZepEmployeePort {

    @Inject
    EmployeeService employeeService;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    @Inject
    ZepEmployeeMapper mapper;

    @Inject
    ZepService zepService;

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Override
    public List<ZepEmployeeSyncData> fetchAll() {
        return employeeService.getZepEmployees().stream()
                .map(this::toSyncData)
                .toList();
    }

    @Override
    public Uni<Void> updateReleaseDate(ZepUsername username, LocalDate releaseDate) {
        Log.infof("Updating release date in ZEP for %s", username.value());
        return zepEmployeeRestClient.updateReleaseDate(
                username.value(),
                new ZepEmployeeReleaseDateUpdate(username.value(), releaseDate)
        );
    }

    @Override
    public Uni<Void> updateHourlyRate(ZepUsername username, HourlyRate hourlyRate, LocalDate effectiveFrom) {
        return Uni.createFrom().item(() -> {
            zepService.updateEmployeeHourlyRate(username.value(), hourlyRate.value(), effectiveFrom.toString());
            return null;
        }).replaceWithVoid();
    }

    private ZepEmployeeSyncData toSyncData(ZepEmployee zepEmployee) {
        String username = zepEmployee.username();

        EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                employmentPeriodService.getZepEmploymentPeriodsByUsername(username).stream()
                        .map(period -> new EmploymentPeriod(
                                period.startDate() != null ? period.startDate().toLocalDate() : null,
                                period.endDate() != null ? period.endDate().toLocalDate() : null
                        ))
                        .toList()
        );

        return mapper.toSyncData(zepEmployee, employmentPeriods);
    }
}

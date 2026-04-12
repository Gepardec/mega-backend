package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.ZepEmployeeSyncData;
import com.gepardec.mega.hexagon.user.domain.port.outbound.ZepEmployeePort;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.service.EmployeeService;
import com.gepardec.mega.zep.rest.service.EmploymentPeriodService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ZepEmployeeAdapter implements ZepEmployeePort {

    @Inject
    EmployeeService employeeService;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    @Inject
    ZepEmployeeMapper mapper;

    @Override
    public List<ZepEmployeeSyncData> fetchAll() {
        return employeeService.getZepEmployees().stream()
                .map(this::toSyncData)
                .toList();
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

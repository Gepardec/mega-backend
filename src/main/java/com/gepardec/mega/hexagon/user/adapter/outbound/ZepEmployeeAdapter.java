package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.RegularWorkingTime;
import com.gepardec.mega.hexagon.user.domain.model.ZepProfile;
import com.gepardec.mega.hexagon.user.domain.port.outbound.ZepEmployeePort;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.service.EmployeeService;
import com.gepardec.mega.zep.rest.service.EmploymentPeriodService;
import com.gepardec.mega.zep.rest.service.RegularWorkingTimesService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ZepEmployeeAdapter implements ZepEmployeePort {

    @Inject
    EmployeeService employeeService;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Override
    public List<ZepProfile> fetchAll() {
        return employeeService.getZepEmployees().stream()
                .map(this::toZepProfile)
                .toList();
    }

    private ZepProfile toZepProfile(ZepEmployee zepEmployee) {
        String username = zepEmployee.username();

        List<EmploymentPeriod> employmentPeriods = employmentPeriodService
                .getZepEmploymentPeriodsByUsername(username).stream()
                .map(period -> new EmploymentPeriod(
                        period.startDate() != null ? period.startDate().toLocalDate() : null,
                        period.endDate() != null ? period.endDate().toLocalDate() : null
                ))
                .toList();

        List<RegularWorkingTime> regularWorkingTimes = regularWorkingTimesService
                .getRegularWorkingTimesByUsername(username).stream()
                .map(this::toRegularWorkingTime)
                .toList();

        return new ZepProfile(
                username,
                zepEmployee.email(),
                zepEmployee.firstname(),
                zepEmployee.lastname(),
                zepEmployee.title(),
                zepEmployee.salutation() != null ? zepEmployee.salutation().name() : null,
                null, // workDescription not available in ZepEmployee
                zepEmployee.language() != null ? zepEmployee.language().id() : null,
                zepEmployee.releaseDate(),
                employmentPeriods,
                regularWorkingTimes
        );
    }

    private RegularWorkingTime toRegularWorkingTime(ZepRegularWorkingTimes zrwt) {
        Map<DayOfWeek, Duration> workingHours = new LinkedHashMap<>();
        putIfNonNull(workingHours, DayOfWeek.MONDAY, zrwt.monday());
        putIfNonNull(workingHours, DayOfWeek.TUESDAY, zrwt.tuesday());
        putIfNonNull(workingHours, DayOfWeek.WEDNESDAY, zrwt.wednesday());
        putIfNonNull(workingHours, DayOfWeek.THURSDAY, zrwt.thursday());
        putIfNonNull(workingHours, DayOfWeek.FRIDAY, zrwt.friday());
        putIfNonNull(workingHours, DayOfWeek.SATURDAY, zrwt.saturday());
        putIfNonNull(workingHours, DayOfWeek.SUNDAY, zrwt.sunday());

        return new RegularWorkingTime(
                zrwt.startDate() != null ? zrwt.startDate().toLocalDate() : null,
                workingHours
        );
    }

    private void putIfNonNull(Map<DayOfWeek, Duration> map, DayOfWeek day, Double hours) {
        if (hours != null) {
            map.put(day, Duration.ofMinutes(Math.round(hours * 60)));
        }
    }
}

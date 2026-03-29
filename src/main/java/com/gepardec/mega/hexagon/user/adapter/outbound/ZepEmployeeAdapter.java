package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.RegularWorkingTime;
import com.gepardec.mega.hexagon.user.domain.model.RegularWorkingTimes;
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
import java.util.EnumMap;
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

    @Inject
    ZepEmployeeMapper mapper;

    @Override
    public List<ZepProfile> fetchAll() {
        return employeeService.getZepEmployees().stream()
                .map(this::toZepProfile)
                .toList();
    }

    private ZepProfile toZepProfile(ZepEmployee zepEmployee) {
        String username = zepEmployee.username();

        EmploymentPeriods employmentPeriods = new EmploymentPeriods(
                employmentPeriodService.getZepEmploymentPeriodsByUsername(username).stream()
                        .map(period -> new EmploymentPeriod(
                                period.startDate() != null ? period.startDate().toLocalDate() : null,
                                period.endDate() != null ? period.endDate().toLocalDate() : null
                        ))
                        .toList()
        );

        RegularWorkingTimes regularWorkingTimes = new RegularWorkingTimes(
                regularWorkingTimesService.getRegularWorkingTimesByUsername(username).stream()
                        .map(this::toRegularWorkingTime)
                        .toList()
        );

        return mapper.toZepProfile(zepEmployee, employmentPeriods, regularWorkingTimes);
    }

    private RegularWorkingTime toRegularWorkingTime(ZepRegularWorkingTimes zrwt) {
        Map<DayOfWeek, Duration> regularWorkingHours = new EnumMap<>(DayOfWeek.class);
        regularWorkingHours.put(DayOfWeek.MONDAY, toDuration(zrwt.monday()));
        regularWorkingHours.put(DayOfWeek.TUESDAY, toDuration(zrwt.tuesday()));
        regularWorkingHours.put(DayOfWeek.WEDNESDAY, toDuration(zrwt.wednesday()));
        regularWorkingHours.put(DayOfWeek.THURSDAY, toDuration(zrwt.thursday()));
        regularWorkingHours.put(DayOfWeek.FRIDAY, toDuration(zrwt.friday()));
        regularWorkingHours.put(DayOfWeek.SATURDAY, toDuration(zrwt.saturday()));
        regularWorkingHours.put(DayOfWeek.SUNDAY, toDuration(zrwt.sunday()));

        return new RegularWorkingTime(
                zrwt.startDate() != null ? zrwt.startDate().toLocalDate() : null,
                regularWorkingHours
        );
    }

    private Duration toDuration(Double hours) {
        return Duration.ofHours(hours == null ? 0 : hours.longValue());
    }
}

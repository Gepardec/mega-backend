package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.util.OfficeCalendarUtil;
import com.gepardec.mega.hexagon.user.domain.model.RegularWorkingTime;
import com.gepardec.mega.hexagon.user.domain.model.RegularWorkingTimes;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeExpectedWorkingDaysPort;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.service.RegularWorkingTimesService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class WorkTimeExpectedWorkingDaysAdapter implements WorkTimeExpectedWorkingDaysPort {
    private final UserRepository userRepository;
    private final RegularWorkingTimesService regularWorkingTimesService;

    @Inject
    public WorkTimeExpectedWorkingDaysAdapter(UserRepository userRepository, RegularWorkingTimesService regularWorkingTimesService) {
        this.userRepository = userRepository;
        this.regularWorkingTimesService = regularWorkingTimesService;
    }

    @Override
    public Set<LocalDate> expectedWorkingDays(UserId userId, YearMonth month) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.zepUsername() == null) {
            return Set.of();
        }
        LocalDate employmentStart = user.employmentPeriods().active(month)
                .map(period -> period.start() == null ? month.atDay(1) : period.start())
                .orElse(null);
        if (employmentStart == null) {
            return Set.of();
        }

        Map<DayOfWeek, Duration> hours = activeWorkingHours(
                regularWorkingTimesService.getRegularWorkingTimesByUsername(user.zepUsername().value()),
                month
        );
        return OfficeCalendarUtil.getWorkingDaysForYearMonth(month).stream()
                .filter(date -> !date.isBefore(employmentStart))
                .filter(date -> hours.isEmpty() || !hours.getOrDefault(date.getDayOfWeek(), Duration.ZERO).isZero())
                .collect(Collectors.toUnmodifiableSet());
    }

    private Map<DayOfWeek, Duration> activeWorkingHours(List<ZepRegularWorkingTimes> values, YearMonth month) {
        return new RegularWorkingTimes(values.stream().map(this::toRegularWorkingTime).toList())
                .active(month)
                .map(RegularWorkingTime::workingHours)
                .orElseGet(Map::of);
    }

    private RegularWorkingTime toRegularWorkingTime(ZepRegularWorkingTimes value) {
        Map<DayOfWeek, Duration> hours = new EnumMap<>(DayOfWeek.class);
        hours.put(DayOfWeek.MONDAY, toDuration(value.monday()));
        hours.put(DayOfWeek.TUESDAY, toDuration(value.tuesday()));
        hours.put(DayOfWeek.WEDNESDAY, toDuration(value.wednesday()));
        hours.put(DayOfWeek.THURSDAY, toDuration(value.thursday()));
        hours.put(DayOfWeek.FRIDAY, toDuration(value.friday()));
        hours.put(DayOfWeek.SATURDAY, toDuration(value.saturday()));
        hours.put(DayOfWeek.SUNDAY, toDuration(value.sunday()));
        return new RegularWorkingTime(value.startDate() == null ? null : value.startDate().toLocalDate(), hours);
    }

    private Duration toDuration(Double hours) {
        return hours == null ? Duration.ZERO : Duration.ofMinutes(Math.round(hours * 60));
    }
}

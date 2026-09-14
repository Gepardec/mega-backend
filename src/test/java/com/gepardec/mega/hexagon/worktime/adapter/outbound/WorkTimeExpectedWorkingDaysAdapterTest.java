package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.service.RegularWorkingTimesService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkTimeExpectedWorkingDaysAdapterTest {
    @Mock UserRepository userRepository;
    @Mock RegularWorkingTimesService regularWorkingTimesService;
    @InjectMocks WorkTimeExpectedWorkingDaysAdapter adapter;

    @Test
    void expectedWorkingDays_intersectsCalendarEmploymentAndNonZeroWeekdays() {
        UserId id = UserId.of(Instancio.create(UUID.class));
        ZepUsername username = Instancio.create(ZepUsername.class);
        User user = user(id, username, new EmploymentPeriods(
                new EmploymentPeriod(LocalDate.of(2026, 5, 6), null)));
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(regularWorkingTimesService.getRegularWorkingTimesByUsername(username.value())).thenReturn(List.of(
                ZepRegularWorkingTimes.builder().monday(8d).tuesday(0d).wednesday(8d)
                        .thursday(8d).friday(8d).saturday(0d).sunday(0d).build()));

        assertThat(adapter.expectedWorkingDays(id, YearMonth.of(2026, 5)))
                .allMatch(date -> !date.isBefore(LocalDate.of(2026, 5, 6)))
                .noneMatch(date -> DayOfWeek.TUESDAY.equals(date.getDayOfWeek()));
    }

    @Test
    void expectedWorkingDays_noActiveEmploymentReturnsEmptySet() {
        UserId id = UserId.of(Instancio.create(UUID.class));
        User user = user(id, Instancio.create(ZepUsername.class), EmploymentPeriods.empty());
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertThat(adapter.expectedWorkingDays(id, YearMonth.of(2026, 5))).isEmpty();
    }

    @Test
    void expectedWorkingDays_doesNotApplyMidMonthScheduleRetroactively() {
        UserId id = UserId.of(Instancio.create(UUID.class));
        ZepUsername username = Instancio.create(ZepUsername.class);
        User user = user(id, username, new EmploymentPeriods(
                new EmploymentPeriod(LocalDate.of(2020, 1, 1), null)));
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(regularWorkingTimesService.getRegularWorkingTimesByUsername(username.value())).thenReturn(List.of(
                ZepRegularWorkingTimes.builder().startDate(LocalDateTime.of(2020, 1, 1, 0, 0))
                        .monday(8d).tuesday(8d).wednesday(8d).thursday(8d).friday(8d).build(),
                ZepRegularWorkingTimes.builder().startDate(LocalDateTime.of(2026, 5, 15, 0, 0))
                        .monday(0d).tuesday(0d).wednesday(0d).thursday(0d).friday(0d).build()));

        assertThat(adapter.expectedWorkingDays(id, YearMonth.of(2026, 5))).isNotEmpty();
    }

    private User user(UserId id, ZepUsername username, EmploymentPeriods employmentPeriods) {
        return Instancio.of(User.class)
                .set(field(User::id), id)
                .set(field(User::zepUsername), username)
                .set(field(User::employmentPeriods), employmentPeriods)
                .set(field(User::roles), Set.of(Role.EMPLOYEE))
                .create();
    }
}

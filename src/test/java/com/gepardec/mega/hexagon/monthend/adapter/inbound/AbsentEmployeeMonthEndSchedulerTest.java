package com.gepardec.mega.hexagon.monthend.adapter.inbound;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.AbsentEmployeeAutoCompletion;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteTasksForAbsentEmployeeUseCase;
import com.gepardec.mega.hexagon.monthend.application.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import io.quarkus.scheduler.Scheduled;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbsentEmployeeMonthEndSchedulerTest {

    @Mock
    CompleteTasksForAbsentEmployeeUseCase completeTasksForAbsentEmployeeUseCase;

    @Mock
    MonthEndUserSnapshotPort monthEndUserSnapshotPort;

    AbsentEmployeeMonthEndScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new AbsentEmployeeMonthEndScheduler(completeTasksForAbsentEmployeeUseCase, monthEndUserSnapshotPort);
    }

    @Test
    void completeTasksForAbsentEmployees_shouldFanOutOverActiveUsers() {
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        UserRef activeUser = new UserRef(employeeId, FullName.of("Ada", "Lovelace"), ZepUsername.of("ada"));
        when(monthEndUserSnapshotPort.findActiveIn(any())).thenReturn(List.of(activeUser));
        when(completeTasksForAbsentEmployeeUseCase.complete(any(), any()))
                .thenReturn(Optional.of(new AbsentEmployeeAutoCompletion(employeeId, YearMonth.now())));

        scheduler.completeTasksForAbsentEmployees();

        verify(monthEndUserSnapshotPort).findActiveIn(YearMonth.now());
        verify(completeTasksForAbsentEmployeeUseCase).complete(employeeId, YearMonth.now());
    }

    @Test
    void completeTasksForAbsentEmployees_shouldRunAtSeventeenHundredOnLastDayOfMonth() throws NoSuchMethodException {
        Scheduled scheduled = AbsentEmployeeMonthEndScheduler.class
                .getDeclaredMethod("completeTasksForAbsentEmployees")
                .getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 0 17 L * ?");
    }
}

package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetEmployeeWorkTimeUseCase;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeUserNotFoundException;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeValidationException;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEntry;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeProjectSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeZepPort;
import com.gepardec.mega.hexagon.worktime.domain.services.WorkTimeReportAssembler;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class GetEmployeeWorkTimeService implements GetEmployeeWorkTimeUseCase {

    private final WorkTimeUserSnapshotPort workTimeUserSnapshotPort;
    private final WorkTimeProjectSnapshotPort workTimeProjectSnapshotPort;
    private final WorkTimeZepPort workTimeZepPort;
    private final WorkTimeReportAssembler workTimeReportAssembler;

    @Inject
    public GetEmployeeWorkTimeService(
            WorkTimeUserSnapshotPort workTimeUserSnapshotPort,
            WorkTimeProjectSnapshotPort workTimeProjectSnapshotPort,
            WorkTimeZepPort workTimeZepPort,
            WorkTimeReportAssembler workTimeReportAssembler
    ) {
        this.workTimeUserSnapshotPort = workTimeUserSnapshotPort;
        this.workTimeProjectSnapshotPort = workTimeProjectSnapshotPort;
        this.workTimeZepPort = workTimeZepPort;
        this.workTimeReportAssembler = workTimeReportAssembler;
    }

    @Override
    public WorkTimeReport getWorkTime(UserId employeeId, YearMonth month) {
        UserRef employee = workTimeUserSnapshotPort.findById(employeeId, month)
                .orElseThrow(() -> new WorkTimeUserNotFoundException("user not found: " + employeeId.value()));

        return workTimeZepPort.fetchAttendancesForEmployee(requireZepUsername(employee), month)
                .map(attendances -> toReport(employee, month, attendances))
                .await().indefinitely();
    }

    private WorkTimeReport toReport(UserRef employee, YearMonth month, List<WorkTimeAttendance> attendances) {
        if (attendances.isEmpty()) {
            return new WorkTimeReport(month, List.of());
        }

        double employeeMonthTotalHours = workTimeReportAssembler.totalHours(attendances);

        List<WorkTimeEntry> entries = attendances.stream()
                .collect(Collectors.groupingBy(WorkTimeAttendance::projectZepId))
                .entrySet().stream()
                .map(entry -> toEntry(employee, month, entry.getKey(), entry.getValue(), employeeMonthTotalHours))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing((WorkTimeEntry entry) -> entry.project().name()))
                .toList();

        return new WorkTimeReport(month, entries);
    }

    private Optional<WorkTimeEntry> toEntry(
            UserRef employeeRef,
            YearMonth month,
            Integer zepProjectId,
            List<WorkTimeAttendance> attendances,
            double employeeMonthTotalHours
    ) {
        return workTimeProjectSnapshotPort.findByZepId(zepProjectId, month)
                .map(project -> workTimeReportAssembler.buildEntry(employeeRef, project, attendances, employeeMonthTotalHours))
                .or(() -> {
                    Log.warnf("Skipping worktime entry for unknown project zepId=%s", zepProjectId);
                    return Optional.empty();
                });
    }

    private String requireZepUsername(UserRef user) {
        if (user.zepUsername() == null || user.zepUsername().value().isBlank()) {
            throw new WorkTimeValidationException("zep username missing for user: " + user.id().value());
        }
        return user.zepUsername().value();
    }
}

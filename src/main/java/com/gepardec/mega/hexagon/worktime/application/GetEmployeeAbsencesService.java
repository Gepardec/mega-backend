package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetEmployeeAbsencesUseCase;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeAbsenceZepPort;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeErrorCode;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeException;
import com.gepardec.mega.hexagon.worktime.domain.model.Absence;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
@Transactional
public class GetEmployeeAbsencesService implements GetEmployeeAbsencesUseCase {

    private final WorkTimeUserSnapshotPort workTimeUserSnapshotPort;
    private final WorkTimeAbsenceZepPort workTimeAbsenceZepPort;

    @Inject
    public GetEmployeeAbsencesService(
            WorkTimeUserSnapshotPort workTimeUserSnapshotPort,
            WorkTimeAbsenceZepPort workTimeAbsenceZepPort
    ) {
        this.workTimeUserSnapshotPort = workTimeUserSnapshotPort;
        this.workTimeAbsenceZepPort = workTimeAbsenceZepPort;
    }

    @Override
    public List<Absence> getAbsences(UserId employeeId, YearMonth month) {
        Objects.requireNonNull(employeeId, "employeeId must not be null");
        Objects.requireNonNull(month, "month must not be null");

        UserRef employee = workTimeUserSnapshotPort.findById(employeeId, month)
                .orElseThrow(() -> new WorkTimeException(WorkTimeErrorCode.USER_NOT_FOUND, "user not found: " + employeeId.value()));

        if (employee.zepUsername() == null || employee.zepUsername().value().isBlank()) {
            throw new WorkTimeException(WorkTimeErrorCode.VALIDATION_FAILED, "zep username missing for user: " + employee.id().value());
        }

        return workTimeAbsenceZepPort.fetchAbsencesForEmployee(employee.zepUsername(), month);
    }
}

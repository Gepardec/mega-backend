package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetEmployeeAbsencesUseCase;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeUserNotFoundException;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeValidationException;
import com.gepardec.mega.hexagon.worktime.domain.model.Absence;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeAbsenceZepPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeUserSnapshotPort;
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
                .orElseThrow(() -> new WorkTimeUserNotFoundException("user not found: " + employeeId.value()));

        if (employee.zepUsername() == null || employee.zepUsername().value().isBlank()) {
            throw new WorkTimeValidationException("zep username missing for user: " + employee.id().value());
        }

        return workTimeAbsenceZepPort.fetchAbsencesForEmployee(employee.zepUsername(), month);
    }
}

package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.application.port.outbound.MonthEndEmployeeAbsencePort;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.GetEmployeeAbsencesUseCase;
import com.gepardec.mega.hexagon.worktime.domain.model.Absence;
import com.gepardec.mega.hexagon.worktime.domain.model.AbsenceType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ApplicationScoped
public class MonthEndWorkTimeAbsenceAdapter implements MonthEndEmployeeAbsencePort {

    private static final Set<AbsenceType> NON_QUALIFYING_ABSENCE_TYPES =
            EnumSet.of(AbsenceType.HOME_OFFICE, AbsenceType.EXTERNAL_TRAINING);

    private final GetEmployeeAbsencesUseCase getEmployeeAbsencesUseCase;

    @Inject
    public MonthEndWorkTimeAbsenceAdapter(GetEmployeeAbsencesUseCase getEmployeeAbsencesUseCase) {
        this.getEmployeeAbsencesUseCase = getEmployeeAbsencesUseCase;
    }

    @Override
    public List<LocalDate> findQualifyingAbsentDays(UserId employeeId, YearMonth month) {
        Objects.requireNonNull(employeeId, "employeeId must not be null");
        Objects.requireNonNull(month, "month must not be null");

        return getEmployeeAbsencesUseCase.getAbsences(employeeId, month).stream()
                .filter(absence -> !NON_QUALIFYING_ABSENCE_TYPES.contains(absence.type()))
                .map(Absence::date)
                .distinct()
                .sorted()
                .toList();
    }
}

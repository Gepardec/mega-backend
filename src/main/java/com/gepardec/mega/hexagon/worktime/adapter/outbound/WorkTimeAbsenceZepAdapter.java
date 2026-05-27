package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeAbsenceZepPort;
import com.gepardec.mega.hexagon.worktime.domain.model.Absence;
import com.gepardec.mega.hexagon.worktime.domain.model.AbsenceType;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.service.AbsenceService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
public class WorkTimeAbsenceZepAdapter implements WorkTimeAbsenceZepPort {

    private final AbsenceService absenceService;

    @Inject
    public WorkTimeAbsenceZepAdapter(AbsenceService absenceService) {
        this.absenceService = absenceService;
    }

    @Override
    public List<Absence> fetchAbsencesForEmployee(ZepUsername zepUsername, YearMonth month) {
        Objects.requireNonNull(zepUsername, "zepUsername must not be null");
        Objects.requireNonNull(month, "month must not be null");

        return absenceService.getZepAbsencesByEmployeeNameForDateRange(zepUsername.value(), month).stream()
                .flatMap(zepAbsence -> toAbsences(zepAbsence, month))
                .toList();
    }

    private Stream<Absence> toAbsences(ZepAbsence zepAbsence, YearMonth month) {
        Optional<AbsenceType> type = resolveType(zepAbsence);
        if (type.isEmpty()) {
            return Stream.empty();
        }
        return expandDates(zepAbsence, month).map(date -> new Absence(date, type.get()));
    }

    private Optional<AbsenceType> resolveType(ZepAbsence zepAbsence) {
        if (zepAbsence.absenceReason() == null) {
            return Optional.empty();
        }
        return AbsenceType.fromZepCode(zepAbsence.absenceReason().name());
    }

    private Stream<LocalDate> expandDates(ZepAbsence zepAbsence, YearMonth month) {
        if (zepAbsence.startDate() == null || zepAbsence.endDate() == null) {
            return Stream.empty();
        }

        LocalDate start = zepAbsence.startDate().isBefore(month.atDay(1))
                ? month.atDay(1)
                : zepAbsence.startDate();
        LocalDate end = zepAbsence.endDate().isAfter(month.atEndOfMonth())
                ? month.atEndOfMonth()
                : zepAbsence.endDate();

        if (start.isAfter(end)) {
            return Stream.empty();
        }

        return start.datesUntil(end.plusDays(1));
    }
}

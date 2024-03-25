package com.gepardec.mega.domain.mapper.zep;

import com.gepardec.mega.domain.utils.DateUtils;
import de.provantis.zep.BeschaeftigungszeitListeType;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.Range;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ExitDateUtil {
    public boolean isActive(BeschaeftigungszeitListeType employmentPeriods) {
        if (employmentPeriods == null) {
            return false;
        }

        final LocalDate now = LocalDate.now();
        var employments = employmentPeriods.getBeschaeftigungszeit();
        return hasOpenEmployments(employments, now) || hasEmploymentEndInTheFuture(employments, now);
    }

    public Optional<LocalDate> getNewestExitDate(BeschaeftigungszeitListeType employmentPeriods) {
        if (employmentPeriods == null) {
            return Optional.empty();
        }

        final LocalDate MAX_DATE_EXCLUSIVE = LocalDate.now();

        var periods = mapBeschaeftigungszeitListeTypeToRangeList(employmentPeriods);
        if (periods == null || periods.isEmpty()) {
            return Optional.empty();
        }

        // vergangenes bis-Datum, das am nähsten zu JETZT liegt
        return periods
                .stream()
                .map(Range::getMaximum)
                .filter(MAX_DATE_EXCLUSIVE::isAfter)
                .max(LocalDate::compareTo);
    }

    private boolean hasOpenEmployments(final List<de.provantis.zep.BeschaeftigungszeitType> employments, final LocalDate now) {
        return employments.stream()
                .filter(employment -> isDateLessOrEqual(employment.getStartdatum(), now))
                .anyMatch(employment -> employment.getEnddatum() == null);
    }

    private boolean hasEmploymentEndInTheFuture(final List<de.provantis.zep.BeschaeftigungszeitType> employments, final LocalDate now) {
        return employments.stream()
                .filter(employment -> isDateLessOrEqual(employment.getStartdatum(), now))
                .anyMatch(employment -> isDateGreaterOrEqual(employment.getEnddatum(), now));
    }

    private boolean isDateLessOrEqual(final String date, final LocalDate now) {
        return DateUtils.parseDate(date).compareTo(now) <= 0;
    }

    private boolean isDateGreaterOrEqual(final String date, final LocalDate now) {
        return DateUtils.parseDate(date).compareTo(now) >= 0;
    }

    private List<Range<LocalDate>> mapBeschaeftigungszeitListeTypeToRangeList(
            BeschaeftigungszeitListeType employmentPeriods) {
        return employmentPeriods
                .getBeschaeftigungszeit()
                .stream()
                .filter(range -> range.getStartdatum() != null && range.getEnddatum() != null)
                .map(this::mapBeschaeftigungszeitTypeToRange)
                .collect(Collectors.toList());
    }

    private Range<LocalDate> mapBeschaeftigungszeitTypeToRange(de.provantis.zep.BeschaeftigungszeitType bt) {
        return Range.between(
                LocalDate.parse(bt.getStartdatum()),
                LocalDate.parse(bt.getEnddatum()),
                LocalDate::compareTo);
    }
}

package com.gepardec.mega.service.mapper;

import com.gepardec.mega.domain.model.DateRange;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.utils.DateUtils;
import de.provantis.zep.*;

import javax.enterprise.context.ApplicationScoped;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmployeeMapper {

    public Employee map(MitarbeiterType mitarbeiterType) {
        if (mitarbeiterType == null) {
            return null;
        }


        boolean active = hasEmployeeAndActiveEmployment(mitarbeiterType);
        LocalDate exitDate = null;

        if(!active) {
            List<DateRange> employmentPeriods = getEmploymentPeriods(mitarbeiterType);
            exitDate = determineNewestExitDateOfEmploymentPeriods(employmentPeriods);
        }

        return Employee.builder()
                .userId(mitarbeiterType.getUserId())
                .email(mitarbeiterType.getEmail())
                .title(mitarbeiterType.getTitel())
                .firstname(mitarbeiterType.getVorname())
                .lastname(mitarbeiterType.getNachname())
                .salutation(mitarbeiterType.getAnrede())
                .releaseDate(getCorrectReleaseDate(mitarbeiterType))
                .workDescription(mitarbeiterType.getPreisgruppe())
                .language(mitarbeiterType.getSprache())
                .regularWorkingHours(getRegularWorkinghours(mitarbeiterType.getRegelarbeitszeitListe()))
                .active(active)
                .exitDate(exitDate)
                .build();
    }

    public List<DateRange> getEmploymentPeriods(MitarbeiterType mitarbeiterType) {
        return Optional.ofNullable(mitarbeiterType.getBeschaeftigungszeitListe())
                .map(BeschaeftigungszeitListeType::getBeschaeftigungszeit)
                .stream()
                .flatMap(Collection::stream)
                .filter(range -> range.getStartdatum() != null && range.getEnddatum() != null)
                .map(zeitraum -> DateRange.of(LocalDate.parse(zeitraum.getStartdatum()), LocalDate.parse(zeitraum.getEnddatum())))
                .collect(Collectors.toList());
    }

    public LocalDate determineNewestExitDateOfEmploymentPeriods(List <DateRange> periods) {
        final LocalDate MAX_DATE_EXCLUSIVE = LocalDate.now();

        // vergangenes bis-Datum, das am nähsten zu JETZT liegt
        Optional<LocalDate> exitDate = Optional.ofNullable(periods).orElse(Collections.emptyList())
                .stream()
                .map(DateRange::getTo)
                .filter(to -> MAX_DATE_EXCLUSIVE.compareTo(to) > 0)
                .max(LocalDate::compareTo);


        return exitDate.orElse(null);
    }

    private String getCorrectReleaseDate(MitarbeiterType mitarbeiterType) {
        boolean isReleaseDateNull = mitarbeiterType.getFreigabedatum() == null || mitarbeiterType.getFreigabedatum().equalsIgnoreCase("null");
        boolean isCreatedDateNull = mitarbeiterType.getCreated() == null;
        if (isReleaseDateNull) {
            if (isCreatedDateNull) {
                return null;
            }
            return LocalDateTime.parse(mitarbeiterType.getCreated(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        return mitarbeiterType.getFreigabedatum();
    }

    private Map<DayOfWeek, Double> getRegularWorkinghours(RegelarbeitszeitListeTypeTs regelarbeitszeitListeTypeTs){
        if(regelarbeitszeitListeTypeTs == null) return new HashMap<>();

        List <RegelarbeitszeitType> regelarbeitszeitList = regelarbeitszeitListeTypeTs.getRegelarbeitszeit();
        RegelarbeitszeitType regelarbeitszeitType = regelarbeitszeitList.get(regelarbeitszeitList.size()-1);

        return new HashMap<>(Map.ofEntries(
                Map.entry(DayOfWeek.MONDAY, regelarbeitszeitType.getMontag()),
                Map.entry(DayOfWeek.TUESDAY, regelarbeitszeitType.getDienstag()),
                Map.entry(DayOfWeek.WEDNESDAY, regelarbeitszeitType.getMittwoch()),
                Map.entry(DayOfWeek.THURSDAY, regelarbeitszeitType.getDonnerstag()),
                Map.entry(DayOfWeek.FRIDAY, regelarbeitszeitType.getFreitag())
        ));
    }

    private boolean hasEmployeeAndActiveEmployment(final MitarbeiterType employee) {
        boolean active = false;
        if (employee.getBeschaeftigungszeitListe() != null) {
            final LocalDate now = LocalDate.now();
            final List<BeschaeftigungszeitType> employments = employee.getBeschaeftigungszeitListe().getBeschaeftigungszeit();
            active = hasOpenEmployments(employments, now) || hasEmploymentEndInTheFuture(employments, now);
        }

        return active;
    }

    private boolean hasOpenEmployments(final List<BeschaeftigungszeitType> employments, final LocalDate now) {
        return employments.stream()
                .filter(employment -> isDateLessOrEqual(employment.getStartdatum(), now))
                .anyMatch(employment -> employment.getEnddatum() == null);
    }

    private boolean hasEmploymentEndInTheFuture(final List<BeschaeftigungszeitType> employments, final LocalDate now) {
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
}

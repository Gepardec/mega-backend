package com.gepardec.mega.service.mapper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.EmploymentPeriod;
import com.gepardec.mega.domain.model.EmploymentPeriods;
import com.gepardec.mega.domain.model.RegularWorkingTime;
import com.gepardec.mega.domain.model.RegularWorkingTimes;
import de.provantis.zep.BeschaeftigungszeitListeType;
import de.provantis.zep.BeschaeftigungszeitType;
import de.provantis.zep.MitarbeiterType;
import de.provantis.zep.RegelarbeitszeitListeTypeTs;
import de.provantis.zep.RegelarbeitszeitType;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class EmployeeMapper {

    public Employee map(MitarbeiterType mitarbeiterType) {
        if (mitarbeiterType == null) {
            return null;
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
                .employmentPeriods(new EmploymentPeriods(mapEmploymentPeriods(mitarbeiterType)))
                .regularWorkingTimes(new RegularWorkingTimes(mapRegularWorkingHoursList(mitarbeiterType.getRegelarbeitszeitListe())))
                .build();
    }

    private List<EmploymentPeriod> mapEmploymentPeriods(MitarbeiterType mitarbeiterType) {
        return Optional.ofNullable(mitarbeiterType.getBeschaeftigungszeitListe())
                .map(BeschaeftigungszeitListeType::getBeschaeftigungszeit)
                .stream()
                .flatMap(Collection::stream)
                .map(this::mapEmploymentPeriod)
                .toList();
    }

    private EmploymentPeriod mapEmploymentPeriod(BeschaeftigungszeitType bt) {
        return new EmploymentPeriod(
                LocalDate.parse(bt.getStartdatum()),
                Optional.ofNullable(bt.getEnddatum()).map(LocalDate::parse).orElse(null)
        );
    }

    private String getCorrectReleaseDate(MitarbeiterType mitarbeiterType) {
        boolean isReleaseDateNull = mitarbeiterType.getFreigabedatum() == null
                || mitarbeiterType.getFreigabedatum().equalsIgnoreCase("null");
        boolean isCreatedDateNull = mitarbeiterType.getCreated() == null;
        if (isReleaseDateNull) {
            if (isCreatedDateNull) {
                return null;
            }
            return LocalDateTime.parse(
                            mitarbeiterType.getCreated(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    )
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        return mitarbeiterType.getFreigabedatum();
    }

    private List<RegularWorkingTime> mapRegularWorkingHoursList(RegelarbeitszeitListeTypeTs regelarbeitszeitListeTypeTs) {
        if (regelarbeitszeitListeTypeTs == null) {
            return List.of();
        }

        return regelarbeitszeitListeTypeTs.getRegelarbeitszeit().stream()
                .map(this::mapRegularWorkingHours)
                .toList();
    }

    private RegularWorkingTime mapRegularWorkingHours(RegelarbeitszeitType regelarbeitszeit) {
        return new RegularWorkingTime(
                Optional.ofNullable(regelarbeitszeit.getStartdatum()).map(LocalDate::parse).orElse(null),
                mapDays(regelarbeitszeit)
        );
    }

    private Map<DayOfWeek, Duration> mapDays(RegelarbeitszeitType regelarbeitszeit) {
        Map<DayOfWeek, Duration> regularWorkingHours = new EnumMap<>(DayOfWeek.class);
        regularWorkingHours.put(DayOfWeek.MONDAY, toDuration((long) (regelarbeitszeit.getMontag() * 60)));
        regularWorkingHours.put(DayOfWeek.TUESDAY, toDuration((long) (regelarbeitszeit.getDienstag() * 60)));
        regularWorkingHours.put(DayOfWeek.WEDNESDAY, toDuration((long) (regelarbeitszeit.getMittwoch() * 60)));
        regularWorkingHours.put(DayOfWeek.THURSDAY, toDuration((long) (regelarbeitszeit.getDonnerstag() * 60)));
        regularWorkingHours.put(DayOfWeek.FRIDAY, toDuration((long) (regelarbeitszeit.getFreitag() * 60)));
        regularWorkingHours.put(DayOfWeek.SATURDAY, toDuration(0L));
        regularWorkingHours.put(DayOfWeek.SUNDAY, toDuration(0L));
        return regularWorkingHours;
    }

    private static Duration toDuration(long regelarbeitszeitStunden) {
        return Duration.ofMinutes(Math.max(0L, regelarbeitszeitStunden));
    }
}

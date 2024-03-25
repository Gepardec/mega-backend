package com.gepardec.mega.domain.mapper.zep;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.utils.DateUtils;
import de.provantis.zep.MitarbeiterType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EmployeeMapper implements ZepMapper<Employee, MitarbeiterType> {

    RegularWorkingTimesMapper regularWorkingTimesMapper;

    ExitDateUtil exitDateUtil;

    @Inject
    public EmployeeMapper(RegularWorkingTimesMapper regularWorkingTimesMapper, ExitDateUtil exitDateUtil) {
        this.regularWorkingTimesMapper = regularWorkingTimesMapper;
        this.exitDateUtil = exitDateUtil;
    }

    @Override
    public Employee mapToDomain(MitarbeiterType mitarbeiterType) {
        if (mitarbeiterType == null) {
            return null;
        }

        var beschaeftigungszeitListe = mitarbeiterType.getBeschaeftigungszeitListe();
        Optional<LocalDate> exitDate = exitDateUtil.getNewestExitDate(beschaeftigungszeitListe);
        boolean active = exitDateUtil.isActive(beschaeftigungszeitListe);

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
                .regularWorkingHours(regularWorkingTimesMapper.mapToDomain(mitarbeiterType.getRegelarbeitszeitListe()))
                .active(active)
                .exitDate(exitDate.orElse(null))
                .build();
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


    private String getCorrectReleaseDate(MitarbeiterType mitarbeiterType) {
        if (!isReleaseDateNull(mitarbeiterType)) {
            return mitarbeiterType.getFreigabedatum();
        }

        return mitarbeiterType.getCreated() == null ? null : createdToReleaseDate(mitarbeiterType);

    }

    private static String createdToReleaseDate(MitarbeiterType mitarbeiterType) {
        final DateTimeFormatter createFormatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final DateTimeFormatter releaseFormatter =
                DateTimeFormatter.ISO_LOCAL_DATE;

        return LocalDateTime.parse(
                        mitarbeiterType.getCreated(),
                        createFormatter
                )
                .format(releaseFormatter);
    }

    private static boolean isReleaseDateNull(MitarbeiterType mitarbeiterType) {
        return mitarbeiterType.getFreigabedatum() == null
                || mitarbeiterType.getFreigabedatum().equalsIgnoreCase("null");
    }
}

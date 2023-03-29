package com.gepardec.mega.service.impl.employee;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.service.mapper.EmployeeMapper;
import de.provantis.zep.BeschaeftigungszeitListeType;
import de.provantis.zep.BeschaeftigungszeitType;
import de.provantis.zep.MitarbeiterType;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeMapperTest {

    private EmployeeMapper mapper;

    @BeforeEach
    void beforeEach() {
        mapper = new EmployeeMapper();
    }

    @Test
    void map_whenEmployeeIsNull_thenReturnsNull() {
        assertThat(mapper.map(null)).isNull();
    }

    @Test
    void map_whenBeschaeftingungszeitListeIsNull_thenEmployeeIsInactive() {
        final MitarbeiterType employee = new MitarbeiterType();

        final Employee actual = mapper.map(employee);
        assertThat(actual.isActive()).isFalse();
    }

    @Test
    void map_whenBeschaeftingungszeitListeIsEmpty_thenEmployeeIsInactive() {
        final MitarbeiterType employee = new MitarbeiterType();
        employee.setBeschaeftigungszeitListe(new BeschaeftigungszeitListeType());

        final Employee actual = mapper.map(employee);
        assertThat(actual.isActive()).isFalse();
    }

    @Test
    void map_whenEmployeeWasEmployedInThePastOnce_thenEmployeeIsInactive() {
        final MitarbeiterType employee = new MitarbeiterType();
        final BeschaeftigungszeitType closedEmployment = createBeschaeftigungszeitType(LocalDate.now().minusDays(2), LocalDate.now().minusDays(1));
        final BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(closedEmployment));
        employee.setBeschaeftigungszeitListe(employments);

        final Employee actual = mapper.map(employee);
        assertThat(actual.isActive()).isFalse();
    }

    @Test
    void map_whenEmployeeWasEmployedInThePastMultipleTimes_thenEmployeeIsInactive() {
        final MitarbeiterType employee = new MitarbeiterType();
        final BeschaeftigungszeitType closedEmploymentOne = createBeschaeftigungszeitType(LocalDate.now().minusDays(10), LocalDate.now()
                .minusDays(8));
        final BeschaeftigungszeitType closedEmploymentTwo = createBeschaeftigungszeitType(LocalDate.now().minusDays(7), LocalDate.now().minusDays(4));
        final BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(closedEmploymentOne, closedEmploymentTwo));
        employee.setBeschaeftigungszeitListe(employments);

        final Employee actual = mapper.map(employee);
        assertThat(actual.isActive()).isFalse();
    }

    @Test
    void map_whenEmployeeWillBeEmployedInTheFutureWithOpenEnd_thenEmployeeIsInactive() {
        final MitarbeiterType employee = new MitarbeiterType();
        final BeschaeftigungszeitType futureActiveEmployment = createBeschaeftigungszeitType(LocalDate.now().plusDays(1), null);
        final BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(futureActiveEmployment));
        employee.setBeschaeftigungszeitListe(employments);

        final Employee actual = mapper.map(employee);
        assertThat(actual.isActive()).isFalse();
    }

    @Test
    void map_whenEmployeeWillBeEmployedInTheFutureWithFixedEnd_thenEmployeeIsInactive() {
        final MitarbeiterType employee = new MitarbeiterType();
        final BeschaeftigungszeitType futureActiveEmployment = createBeschaeftigungszeitType(LocalDate.now().plusDays(1), LocalDate.now()
                .plusDays(2));
        final BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(futureActiveEmployment));
        employee.setBeschaeftigungszeitListe(employments);

        final Employee actual = mapper.map(employee);
        assertThat(actual.isActive()).isFalse();
    }

    @Test
    void map_whenEmployeeIsEmployedOneDayOnCurrentDay_thenEmployeeIsActive() {
        final MitarbeiterType employee = new MitarbeiterType();
        final LocalDate today = LocalDate.now();
        final BeschaeftigungszeitType futureActiveEmployment = createBeschaeftigungszeitType(today, today);
        final BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(futureActiveEmployment));
        employee.setBeschaeftigungszeitListe(employments);

        final Employee actual = mapper.map(employee);
        assertThat(actual.isActive()).isTrue();
    }

    @Test
    void map_whenEmployeeIsCurrentlyEmployedWithOpenEnd_thenEmployeeIsActive() {
        final MitarbeiterType employee = new MitarbeiterType();
        final BeschaeftigungszeitType activeEmployment = createBeschaeftigungszeitType(LocalDate.now().minusDays(10), null);
        final BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(activeEmployment));
        employee.setBeschaeftigungszeitListe(employments);

        final Employee actual = mapper.map(employee);
        assertThat(actual.isActive()).isTrue();
    }

    @Test
    void map_whenEmployeeIsCurrentlyEmployedWithFixedEndDate_thenEmployeeIsActive() {
        final MitarbeiterType employee = new MitarbeiterType();
        final BeschaeftigungszeitType activeEmployment = createBeschaeftigungszeitType(LocalDate.now().minusDays(10), LocalDate.now().plusDays(1));
        final BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(activeEmployment));
        employee.setBeschaeftigungszeitListe(employments);

        final Employee actual = mapper.map(employee);
        assertThat(actual.isActive()).isTrue();
    }

    @Test
    void map_whenEmployeeWasEmployedInThePastAndIsCurrentlyEmployed_thenEmployeeIsActive() {
        final MitarbeiterType employee = new MitarbeiterType();
        final BeschaeftigungszeitType closedEmployment = createBeschaeftigungszeitType(LocalDate.now().minusDays(10), LocalDate.now().minusDays(8));
        final BeschaeftigungszeitType activeEmployment = createBeschaeftigungszeitType(LocalDate.now().minusDays(7), LocalDate.now().plusDays(1));
        final BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(closedEmployment, activeEmployment));
        employee.setBeschaeftigungszeitListe(employments);

        final Employee actual = mapper.map(employee);
        assertThat(actual.isActive()).isTrue();
    }

    @Test
    void map_whenEmployee_thenMappedProperly() {
        final BeschaeftigungszeitType activeEmployment = createBeschaeftigungszeitType(LocalDate.now().minusDays(7), LocalDate.now().plusDays(1));
        final BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(List.of(activeEmployment));
        final MitarbeiterType employee = new MitarbeiterType();
        employee.setEmail("no-reply@gepardec.com");
        employee.setVorname("Max");
        employee.setNachname("Mustermann");
        employee.setTitel("Ing.");
        employee.setUserId("1");
        employee.setAnrede("Herr");
        employee.setPreisgruppe("ARCHITEKT");
        employee.setFreigabedatum("2020-01-01");
        employee.setBeschaeftigungszeitListe(employments);

        final Employee actual = mapper.map(employee);
        assertThat(employee).isNotNull();
        assertThat(actual.getUserId()).isEqualTo(employee.getUserId());
        assertThat(actual.getEmail()).isEqualTo(employee.getEmail());
        assertThat(actual.getFirstname()).isEqualTo(employee.getVorname());
        assertThat(actual.getLastname()).isEqualTo(employee.getNachname());
        assertThat(actual.getTitle()).isEqualTo(employee.getTitel());
        assertThat(actual.getSalutation()).isEqualTo(employee.getAnrede());
        assertThat(actual.getWorkDescription()).isEqualTo(employee.getPreisgruppe());
        assertThat(actual.getReleaseDate()).isEqualTo(employee.getFreigabedatum());
        assertThat(actual.isActive()).isTrue();
    }

    @Nested
    @DisplayName("Tests for EmploymentPeriods")
    class EmploymentPeriods {

        LocalDate _2015_01_01 = LocalDate.of(2015, 1, 1);
        LocalDate _2010_01_01 = LocalDate.of(2010, 1, 1);
        LocalDate _2023_01_01 = LocalDate.of(2023, 1, 1);
        LocalDate _2023_01_25 = LocalDate.of(2023, 1, 25);

        @Test
        void should_map_correctly() {

            // Given
            List<Pair<LocalDate, LocalDate>> dateRangeInput = List.of(
                    Pair.of(
                            _2023_01_01,
                            _2023_01_25
                    ),
                    Pair.of(
                            _2010_01_01,
                            _2015_01_01
                    )
            );

            BeschaeftigungszeitListeType beschaeftigungszeitListe = getBeschaeftigungszeitListeMock(dateRangeInput);

            final MitarbeiterType employee = createEmployee(beschaeftigungszeitListe);

            // When
            List<Range<LocalDate>> mapped = mapper.getEmploymentPeriods(employee);

            // Then
            List<Range<LocalDate>> expectedPeriods = List.of(
                    createRange(_2023_01_01, _2023_01_25),
                    createRange(_2010_01_01, _2015_01_01)
            );

            assertThat(mapped.size()).isEqualTo(expectedPeriods.size());
            assertThat(mapped).containsAll(expectedPeriods);
        }
        @Test
        void should_ignoreRangeWithoutEnd() {

            // Given
            List<Pair<LocalDate, LocalDate>> dateRangeInput = List.of(
                    Pair.of(
                            _2023_01_01,
                            _2023_01_25
                    ),
                    Pair.of(
                            _2010_01_01,
                            null
                    )
            );


            BeschaeftigungszeitListeType beschaeftigungszeitListe = getBeschaeftigungszeitListeMock(dateRangeInput);

            final MitarbeiterType employee = createEmployee(beschaeftigungszeitListe);

            // When
            List<Range<LocalDate>> mapped = mapper.getEmploymentPeriods(employee);

            // Then
            List<Range<LocalDate>> expectedPeriods = List.of(
                    createRange(_2023_01_01, _2023_01_25)
            );

            assertThat(mapped.size()).isEqualTo(expectedPeriods.size());
            assertThat(mapped).containsAll(expectedPeriods);
        }

        @Test
        void should_mapToEmptyList_when_beschaeftigungszeitListeIsNull() {

            // Given
            final MitarbeiterType employee = createEmployee(null);

            // When
            List<Range<LocalDate>> mapped = mapper.getEmploymentPeriods(employee);


            assertThat(mapped).isNotNull();
            assertThat(mapped).isEmpty();
        }

        private MitarbeiterType createEmployee(BeschaeftigungszeitListeType beschaeftigungszeitListe) {
            final MitarbeiterType employee = new MitarbeiterType();
            employee.setBeschaeftigungszeitListe(beschaeftigungszeitListe);
            return employee;
        }

        private BeschaeftigungszeitListeType getBeschaeftigungszeitListeMock(List<Pair<LocalDate, LocalDate>> dateRangeInput) {
            BeschaeftigungszeitListeType beschaeftigungszeitListeMock = Mockito.mock(BeschaeftigungszeitListeType.class);

            List<BeschaeftigungszeitType> beschaeftigungszeit = dateRangeInput.stream().map(x -> {
                BeschaeftigungszeitType bt = new BeschaeftigungszeitType();
                bt.setStartdatum(Optional.ofNullable(x.getLeft()).map(LocalDate::toString).orElse(null));
                bt.setEnddatum(Optional.ofNullable(x.getRight()).map(LocalDate::toString).orElse(null));

                return bt;
            }).collect(Collectors.toList());


            // Mocking needed because no setter methods provided
            Mockito.when(beschaeftigungszeitListeMock.getBeschaeftigungszeit()).thenReturn(beschaeftigungszeit);

            return beschaeftigungszeitListeMock;
        }
    }

    @Nested
    @DisplayName("Tests for determing the exit date of given employment periods")
    class ExitDateOfEmploymentPeriods {

        @Test
        void should_beNull_ifNoNull_isGiven() {

            // Given
            List <Range<LocalDate>> periods = null;

            // When
            LocalDate exitDate = mapper.determineNewestExitDateOfEmploymentPeriods(periods);

            // Then
            assertThat(exitDate).isNull();
        }

        @Test
        void should_beNull_ifEmptyList_isGiven() {

            // Given
            List <Range<LocalDate>> periods = new ArrayList<>();

            // When
            LocalDate exitDate = mapper.determineNewestExitDateOfEmploymentPeriods(periods);

            // Then
            assertThat(exitDate).isNull();
        }

        @Test
        void should_beNull_ifPeriodsInFuture() {

            // Given
            LocalDate now = LocalDate.now();
            List <Range<LocalDate>> periods = new ArrayList<>();

            periods.add(createRange(now.plusMonths(2), now.plusMonths(5)));
            periods.add(createRange(now.plusDays(2), now.plusMonths(5)));
            periods.add(createRange(now.plusYears(2), now.plusMonths(5)));


            // When
            LocalDate exitDate = mapper.determineNewestExitDateOfEmploymentPeriods(periods);

            // Then
            assertThat(exitDate).isNull();
        }

        @Test
        void should_determineTheClosestExitDate_when_multipleEmploymentsInThePast_areGiven() {

            // Given
            LocalDate now = LocalDate.now();
            LocalDate yesterday = now.minusDays(1);
            List <Range<LocalDate>> periods = new ArrayList<>();

            periods.add(createRange(now.minusYears(10), now.minusYears(8)));
            periods.add(createRange(now.minusYears(1), yesterday));
            periods.add(createRange(now.minusYears(5), now.minusYears(4)));

            // When
            LocalDate exitDate = mapper.determineNewestExitDateOfEmploymentPeriods(periods);

            // Then
            assertThat(exitDate).isEqualTo(yesterday);
        }

        @Test
        void should_returnNull_ifEmployeeHasResignedButTerminationDateInFuture() {

            // Given
            LocalDate now = LocalDate.now();
            LocalDate firstDayOfWork = now.minusYears(1);
            LocalDate lastDayOfWork = now.plusMonths(2);
            List <Range<LocalDate>> periods = new ArrayList<>();

            periods.add(createRange(firstDayOfWork, lastDayOfWork));

            // When
            LocalDate exitDate = mapper.determineNewestExitDateOfEmploymentPeriods(periods);

            // Then
            assertThat(exitDate).isNull();
        }

        @Test
        void should_mapExitDate_correctly() {

            // Given
            LocalDate now = LocalDate.now();
            LocalDate yesterday = now.minusDays(1);

            final MitarbeiterType employee = new MitarbeiterType();

            BeschaeftigungszeitListeType employments = createBeschaeftigungszeitListeType(
                    List.of(
                            createBeschaeftigungszeitType(now.minusYears(10), now.minusYears(8)),
                            createBeschaeftigungszeitType(now.minusYears(1), yesterday),
                            createBeschaeftigungszeitType(now.minusYears(5), now.minusYears(4))

                    ));
            employee.setBeschaeftigungszeitListe(employments);

            // When
            Employee mapped = mapper.map(employee);

            // Then
            assertThat(mapped.getExitDate()).isEqualTo(yesterday);
        }
    }

    private BeschaeftigungszeitType createBeschaeftigungszeitType(final LocalDate start, final LocalDate end) {
        final BeschaeftigungszeitType beschaeftigung = new BeschaeftigungszeitType();
        beschaeftigung.setStartdatum((start != null) ? DateUtils.formatDate(start) : null);
        beschaeftigung.setEnddatum((end != null) ? DateUtils.formatDate(end) : null);
        return beschaeftigung;
    }

    private BeschaeftigungszeitListeType createBeschaeftigungszeitListeType(List<BeschaeftigungszeitType> employments) {
        final BeschaeftigungszeitListeType beschaeftigungszeitListeType = new BeschaeftigungszeitListeType();
        beschaeftigungszeitListeType.getBeschaeftigungszeit().addAll(employments);
        return beschaeftigungszeitListeType;
    }

    private Range<LocalDate> createRange(LocalDate from, LocalDate to) {
        return Range.between(from, to, LocalDate::compareTo);
    }
}

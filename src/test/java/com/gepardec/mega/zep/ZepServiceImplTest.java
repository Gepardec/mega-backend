package com.gepardec.mega.zep;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.service.mapper.EmployeeMapper;
import com.gepardec.mega.zep.impl.ZepSoapServiceImpl;
import com.gepardec.mega.zep.mapper.ProjectEntryMapper;
import de.provantis.zep.BeschaeftigungszeitListeType;
import de.provantis.zep.BeschaeftigungszeitType;
import de.provantis.zep.MitarbeiterListeType;
import de.provantis.zep.MitarbeiterType;
import de.provantis.zep.ProjektListeType;
import de.provantis.zep.ProjektMitarbeiterListeType;
import de.provantis.zep.ProjektMitarbeiterType;
import de.provantis.zep.ProjektType;
import de.provantis.zep.ReadMitarbeiterRequestType;
import de.provantis.zep.ReadMitarbeiterResponseType;
import de.provantis.zep.ReadProjekteRequestType;
import de.provantis.zep.ReadProjekteResponseType;
import de.provantis.zep.ResponseHeaderType;
import de.provantis.zep.UpdateMitarbeiterRequestType;
import de.provantis.zep.UpdateMitarbeiterResponseType;
import de.provantis.zep.ZepSoapPortType;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ZepServiceImplTest {

    private final ProjectEntryMapper projectEntryMapper = new ProjectEntryMapper();

    @InjectSpy
    EmployeeMapper employeeMapper;

    ZepSoapPortType zepSoapPortType;

    @InjectMock
    ZepSoapProvider zepSoapProvider;

    @Inject
    Logger logger;

    @InjectMock
    UserContext userContext;

    ZepSoapServiceImpl zepService;

    private ProjektMitarbeiterListeType projektMitarbeiterListeType;

    private YearMonth payrollMonth;

    private static Stream<Arguments> whenFilterProjectEmployeeMatchesVonBis_shouldBeIncluded() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, "2020-12-31"),
                Arguments.of("2020-01-01", null),
                Arguments.of("2020-01-01", "2020-12-31"),
                Arguments.of("2020-12-01", "2020-12-31"),
                Arguments.of("2020-11-01", "2020-12-31"),
                Arguments.of("2020-01-01", "2020-12-15"),
                Arguments.of("2020-12-15", "2021-12-31"),
                Arguments.of("2020-12-15", "2021-12-16")
        );
    }

    private static Stream<Arguments> whenFilterProjectEmployeeNotMatchesVonBis_shouldNotBeIncluded() {
        return Stream.of(
                Arguments.of("2020-01-01", "2020-11-30"),
                Arguments.of("2021-01-01", "2021-12-31")
        );
    }

    @BeforeEach
    void setUp() {
        zepSoapPortType = mock(ZepSoapPortType.class);

        zepService = new ZepSoapServiceImpl(employeeMapper, logger, zepSoapPortType, zepSoapProvider, projectEntryMapper);

        final ReadProjekteResponseType readProjekteResponseType = new ReadProjekteResponseType();
        final ProjektListeType projektListeType = new ProjektListeType();
        final ProjektType projektType = new ProjektType();
        projektType.setProjektNr("ÖGK-RGKKCC-2020");
        projektType.setStartDatum(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        projektType.setVoreinstFakturierbarkeit(1);
        projektMitarbeiterListeType = new ProjektMitarbeiterListeType();

        projektType.setProjektmitarbeiterListe(projektMitarbeiterListeType);
        projektListeType.getProjekt().add(projektType);
        readProjekteResponseType.setProjektListe(projektListeType);

        payrollMonth = YearMonth.of(2020, 12);

        lenient().when(zepSoapPortType.readProjekte(Mockito.any(ReadProjekteRequestType.class))).thenReturn(readProjekteResponseType);
    }

    @Test
    void getEmployee() {
        Mockito.when(zepSoapPortType.readMitarbeiter(Mockito.any(ReadMitarbeiterRequestType.class))).thenReturn(createReadMitarbeiterResponseType(
                List.of(createMitarbeiterType(0))
        ));

        when(userContext.getUser()).thenReturn(User.builder().personioId(123).build());

        final Employee employee = zepService.getEmployee("0");
        assertThat(employee).isNotNull();
        assertThat(employee.getUserId()).isEqualTo("0");

        verify(zepSoapPortType).readMitarbeiter(Mockito.argThat(
                argument -> argument.getReadMitarbeiterSearchCriteria() != null && argument.getReadMitarbeiterSearchCriteria().getUserId().equals("0")
        ));
    }

    @Test
    void getEmployee_releaseDateFromZepNullString_releaseDateMappedToNull() {
        MitarbeiterType mitarbeiter = createMitarbeiterType(0);
        mitarbeiter.setFreigabedatum(null);

        when(zepSoapPortType.readMitarbeiter(any())).thenReturn(createReadMitarbeiterResponseType(
                List.of(mitarbeiter)
        ));

        when(userContext.getUser()).thenReturn(User.builder().personioId(123).build());

        final Employee employee = zepService.getEmployee("0");

        assertThat(employee).isNotNull();
        assertThat(employee.getReleaseDate()).isNull();
    }

    @Test
    void getEmployeesMitarbeiterZepResponseNull() {
        Mockito.when(zepSoapPortType.readMitarbeiter(Mockito.any(ReadMitarbeiterRequestType.class))).thenReturn(null);

        final List<Employee> employee = zepService.getEmployees();

        assertAll(
                () -> assertThat(employee).isNotNull(),
                () -> assertThat(employee).isEmpty()
        );
    }

    @Test
    void getEmployeesMitarbeiterZepResponseMitarbeiterListeNull() {
        Mockito.when(zepSoapPortType.readMitarbeiter(Mockito.any(ReadMitarbeiterRequestType.class))).thenReturn(new ReadMitarbeiterResponseType());

        final List<Employee> employee = zepService.getEmployees();

        assertAll(
                () -> assertThat(employee).isNotNull(),
                () -> assertThat(employee).isEmpty()
        );

    }

    @Test
    void getEmployees() {
        Mockito.when(zepSoapPortType.readMitarbeiter(Mockito.any(ReadMitarbeiterRequestType.class))).thenReturn(createReadMitarbeiterResponseType(
                List.of(createMitarbeiterType(0), createMitarbeiterType(1), createMitarbeiterType(2))
        ));

        when(userContext.getUser()).thenReturn(User.builder().personioId(123).build());

        final List<Employee> employee = zepService.getEmployees();

        assertAll(
                () -> assertThat(employee).isNotNull(),
                () -> assertThat(employee).hasSize(3)
        );

        verify(zepSoapPortType).readMitarbeiter(Mockito.argThat(
                argument -> argument.getReadMitarbeiterSearchCriteria() == null
        ));
    }

    @Test
    void updateEmployeesReleaseDateException() {
        Mockito.when(zepSoapPortType.updateMitarbeiter(Mockito.any(UpdateMitarbeiterRequestType.class)))
                .thenReturn(createUpaUpdateMitarbeiterResponseType(createResponseHeaderType("1337")));

        assertThatThrownBy(() -> zepService.updateEmployeesReleaseDate("0", "2020-01-01"))
                .isInstanceOf(ZepServiceException.class)
                .hasMessage("updateEmployeeReleaseDate failed with code: 1337");

        verify(zepSoapPortType).updateMitarbeiter(Mockito.argThat(
                argument -> argument.getMitarbeiter().getUserId().equals("0") && argument.getMitarbeiter().getFreigabedatum().equals("2020-01-01")
        ));
    }

    @Test
    void updateEmployeesReleaseDateResponseNull() {
        Mockito.when(zepSoapPortType.updateMitarbeiter(Mockito.any(UpdateMitarbeiterRequestType.class))).thenReturn(null);

        zepService.updateEmployeesReleaseDate("0", "2020-01-01");

        verify(zepSoapPortType).updateMitarbeiter(Mockito.argThat(
                argument -> argument.getMitarbeiter().getUserId().equals("0") && argument.getMitarbeiter().getFreigabedatum().equals("2020-01-01")
        ));
    }

    @Test
    void updateEmployeesReleaseDateResponseHeaderNull() {
        Mockito.when(zepSoapPortType.updateMitarbeiter(Mockito.any(UpdateMitarbeiterRequestType.class))).thenReturn(createUpaUpdateMitarbeiterResponseType(null));

        zepService.updateEmployeesReleaseDate("0", "2020-01-01");

        verify(zepSoapPortType).updateMitarbeiter(Mockito.argThat(
                argument -> argument.getMitarbeiter().getUserId().equals("0") && argument.getMitarbeiter().getFreigabedatum().equals("2020-01-01")
        ));
    }

    @Test
    void updateEmployeesReleaseDate() {
        Mockito.when(zepSoapPortType.updateMitarbeiter(Mockito.any(UpdateMitarbeiterRequestType.class)))
                .thenReturn(createUpaUpdateMitarbeiterResponseType(createResponseHeaderType("0")));

        zepService.updateEmployeesReleaseDate("0", "2020-01-01");

        verify(zepSoapPortType).updateMitarbeiter(Mockito.argThat(
                argument -> argument.getMitarbeiter().getUserId().equals("0") && argument.getMitarbeiter().getFreigabedatum().equals("2020-01-01")
        ));
    }

    private MitarbeiterType createMitarbeiterType(final int userId) {
        final MitarbeiterType mitarbeiter = new MitarbeiterType();
        final String name = "Max_" + userId;

        mitarbeiter.setEmail(name + "@gepardec.com");
        mitarbeiter.setVorname(name);
        mitarbeiter.setNachname(name + "_Nachname");
        mitarbeiter.setTitel("Ing.");
        mitarbeiter.setUserId(String.valueOf(userId));
        mitarbeiter.setAnrede("Herr");
        mitarbeiter.setPreisgruppe("ARCHITEKT");
        mitarbeiter.setFreigabedatum("2020-01-01");
        mitarbeiter.setBeschaeftigungszeitListe(createBeschaeftigungszeitListe());

        return mitarbeiter;
    }

    private BeschaeftigungszeitListeType createBeschaeftigungszeitListe() {
        var beschaeftigungszeitTyp = new BeschaeftigungszeitType();
        beschaeftigungszeitTyp.setStartdatum("2021-02-01");
        var beschaeftigungszeitListeTyp = new BeschaeftigungszeitListeType();
        beschaeftigungszeitListeTyp.getBeschaeftigungszeit().add(beschaeftigungszeitTyp);

        return beschaeftigungszeitListeTyp;
    }

    private ReadMitarbeiterResponseType createReadMitarbeiterResponseType(final List<MitarbeiterType> mitarbeiterType) {
        final ReadMitarbeiterResponseType readMitarbeiterResponseType = new ReadMitarbeiterResponseType();
        readMitarbeiterResponseType.setMitarbeiterListe(new MitarbeiterListeType());
        readMitarbeiterResponseType.getMitarbeiterListe().getMitarbeiter().addAll(mitarbeiterType);
        return readMitarbeiterResponseType;
    }

    private ResponseHeaderType createResponseHeaderType(final String returnCode) {
        final ResponseHeaderType responseHeaderType = new ResponseHeaderType();
        responseHeaderType.setReturnCode(returnCode);
        return responseHeaderType;
    }

    private UpdateMitarbeiterResponseType createUpaUpdateMitarbeiterResponseType(final ResponseHeaderType responseHeaderType) {
        final UpdateMitarbeiterResponseType updateMitarbeiterResponseType = new UpdateMitarbeiterResponseType();
        updateMitarbeiterResponseType.setResponseHeader(responseHeaderType);
        return updateMitarbeiterResponseType;
    }

    @ParameterizedTest
    @MethodSource("whenFilterProjectEmployeeMatchesVonBis_shouldBeIncluded")
    void whenFilterProjectEmployeeMatchesVonBis_shouldBeIncluded(final String von, final String bis) {
        // Given
        final ProjektMitarbeiterType projektMitarbeiterType = new ProjektMitarbeiterType();
        projektMitarbeiterType.setVon(von);
        projektMitarbeiterType.setBis(bis);
        projektMitarbeiterType.setIstProjektleiter(1);

        projektMitarbeiterListeType.getProjektmitarbeiter().add(projektMitarbeiterType);

        // When
        final List<Project> projectsForMonthYear = zepService.getProjectsForMonthYear(payrollMonth);

        // Then
        assertThat(projectsForMonthYear).hasSize(1);
        assertThat(projectsForMonthYear.getFirst().getEmployees()).hasSize(1);
        assertThat(projectsForMonthYear.getFirst().getLeads()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("whenFilterProjectEmployeeNotMatchesVonBis_shouldNotBeIncluded")
    void whenFilterProjectEmployeeNotMatchesVonBis_shouldNotBeIncluded(final String von, final String bis) {
        // Given
        final ProjektMitarbeiterType projektMitarbeiterType = new ProjektMitarbeiterType();
        projektMitarbeiterType.setVon(von);
        projektMitarbeiterType.setBis(bis);
        projektMitarbeiterType.setIstProjektleiter(1);

        projektMitarbeiterListeType.getProjektmitarbeiter().add(projektMitarbeiterType);

        // When
        final List<Project> projectsForMonthYear = zepService.getProjectsForMonthYear(payrollMonth);

        // Then
        assertThat(projectsForMonthYear).hasSize(1);
        assertThat(projectsForMonthYear.getFirst().getEmployees()).isEmpty();
        assertThat(projectsForMonthYear.getFirst().getLeads()).isEmpty();
    }

    @Test
    void whenMultipleAndOneMatches_shouldBeIncluded() {
        // Given
        final ProjektMitarbeiterType projektMitarbeiterType1 = new ProjektMitarbeiterType();
        projektMitarbeiterType1.setVon(null);
        projektMitarbeiterType1.setBis("2020-09-30");
        projektMitarbeiterType1.setIstProjektleiter(1);

        final ProjektMitarbeiterType projektMitarbeiterType2 = new ProjektMitarbeiterType();
        projektMitarbeiterType2.setVon("2020-12-01");
        projektMitarbeiterType2.setBis("2020-12-31");
        projektMitarbeiterType2.setIstProjektleiter(1);

        projektMitarbeiterListeType.getProjektmitarbeiter().add(projektMitarbeiterType1);
        projektMitarbeiterListeType.getProjektmitarbeiter().add(projektMitarbeiterType2);

        // When
        final List<Project> projectsForMonthYear = zepService.getProjectsForMonthYear(payrollMonth);

        // Then
        assertThat(projectsForMonthYear).hasSize(1);
        assertThat(projectsForMonthYear.getFirst().getEmployees()).hasSize(1);
        assertThat(projectsForMonthYear.getFirst().getLeads()).hasSize(1);
    }

    @Test
    void whenMultipleAndNotOneMatches_shouldNotBeIncluded() {
        // Given
        final ProjektMitarbeiterType projektMitarbeiterType1 = new ProjektMitarbeiterType();
        projektMitarbeiterType1.setVon(null);
        projektMitarbeiterType1.setBis("2020-09-30");
        projektMitarbeiterType1.setIstProjektleiter(1);

        final ProjektMitarbeiterType projektMitarbeiterType2 = new ProjektMitarbeiterType();
        projektMitarbeiterType2.setVon("2021-01-01");
        projektMitarbeiterType2.setBis("2021-12-31");
        projektMitarbeiterType2.setIstProjektleiter(1);

        projektMitarbeiterListeType.getProjektmitarbeiter().add(projektMitarbeiterType1);
        projektMitarbeiterListeType.getProjektmitarbeiter().add(projektMitarbeiterType2);

        // When
        final List<Project> projectsForMonthYear = zepService.getProjectsForMonthYear(payrollMonth);

        // Then
        assertThat(projectsForMonthYear).hasSize(1);
        assertThat(projectsForMonthYear.getFirst().getEmployees()).isEmpty();
        assertThat(projectsForMonthYear.getFirst().getLeads()).isEmpty();
    }
}

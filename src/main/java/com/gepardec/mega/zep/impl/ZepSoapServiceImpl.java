package com.gepardec.mega.zep.impl;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.BillabilityPreset;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.MonthlyBillInfo;
import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectHoursSummary;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.service.mapper.EmployeeMapper;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.ZepSoapProvider;
import com.gepardec.mega.zep.mapper.AbsenceTimeMapper;
import com.gepardec.mega.zep.mapper.ProjectEntryMapper;
import com.gepardec.mega.zep.mapper.ProjectTimeMapper;
import de.provantis.zep.FehlzeitType;
import de.provantis.zep.KategorieListeType;
import de.provantis.zep.KategorieType;
import de.provantis.zep.MitarbeiterType;
import de.provantis.zep.ProjektListeType;
import de.provantis.zep.ProjektMitarbeiterListeType;
import de.provantis.zep.ProjektMitarbeiterType;
import de.provantis.zep.ProjektNrListeType;
import de.provantis.zep.ProjektType;
import de.provantis.zep.ProjektzeitType;
import de.provantis.zep.ReadFehlzeitRequestType;
import de.provantis.zep.ReadFehlzeitResponseType;
import de.provantis.zep.ReadFehlzeitSearchCriteriaType;
import de.provantis.zep.ReadMitarbeiterRequestType;
import de.provantis.zep.ReadMitarbeiterSearchCriteriaType;
import de.provantis.zep.ReadProjekteRequestType;
import de.provantis.zep.ReadProjekteResponseType;
import de.provantis.zep.ReadProjekteSearchCriteriaType;
import de.provantis.zep.ReadProjektzeitenRequestType;
import de.provantis.zep.ReadProjektzeitenResponseType;
import de.provantis.zep.ReadProjektzeitenSearchCriteriaType;
import de.provantis.zep.ResponseHeaderType;
import de.provantis.zep.UpdateMitarbeiterRequestType;
import de.provantis.zep.UpdateMitarbeiterResponseType;
import de.provantis.zep.UserIdListeType;
import de.provantis.zep.ZepSoapPortType;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@RequestScoped
@Soap
public class ZepSoapServiceImpl implements ZepService {


    private static final Range<Integer> PROJECT_LEAD_RANGE = Range.of(1, 2);

    private final EmployeeMapper employeeMapper;

    private final Logger logger;

    private final de.provantis.zep.ZepSoapPortType zepSoapPortType;

    private final ZepSoapProvider zepSoapProvider;

    private final ProjectEntryMapper projectEntryMapper;

    @Inject
    public ZepSoapServiceImpl(final EmployeeMapper employeeMapper,
                              final Logger logger,
                              final ZepSoapPortType zepSoapPortType,
                              final ZepSoapProvider zepSoapProvider,
                              final ProjectEntryMapper projectEntryMapper) {
        this.employeeMapper = employeeMapper;
        this.logger = logger;
        this.zepSoapPortType = zepSoapPortType;
        this.zepSoapProvider = zepSoapProvider;
        this.projectEntryMapper = projectEntryMapper;
    }

    @Override
    public Employee getEmployee(final String userId) {
        final ReadMitarbeiterSearchCriteriaType readMitarbeiterSearchCriteriaType = new ReadMitarbeiterSearchCriteriaType();
        readMitarbeiterSearchCriteriaType.setUserId(userId);

        return getEmployeeInternal(readMitarbeiterSearchCriteriaType).stream().findFirst().orElse(null);
    }

    @CacheResult(cacheName = "employee")
    @Override
    public List<Employee> getEmployees() {
        return getEmployeeInternal(null);
    }

    @CacheInvalidate(cacheName = "employee")
    @Override
    public void updateEmployeesReleaseDate(final String userId, final String releaseDate) {
        logger.info("start update user {} with releaseDate {}", userId, releaseDate);

        final UpdateMitarbeiterRequestType umrt = new UpdateMitarbeiterRequestType();
        umrt.setRequestHeader(zepSoapProvider.createRequestHeaderType());

        final MitarbeiterType mitarbeiter = new MitarbeiterType();
        mitarbeiter.setUserId(userId);
        mitarbeiter.setFreigabedatum(releaseDate);
        umrt.setMitarbeiter(mitarbeiter);

        final UpdateMitarbeiterResponseType updateMitarbeiterResponseType = zepSoapPortType.updateMitarbeiter(umrt);

        String returnCode = Optional.ofNullable(updateMitarbeiterResponseType)
                .flatMap(response -> Optional.ofNullable(response.getResponseHeader()))
                .map(ResponseHeaderType::getReturnCode)
                .orElse(null);

        logger.info("finish update user {} with response {}", userId, returnCode);

        if (StringUtils.isNotBlank(returnCode) && Integer.parseInt(returnCode) != 0) {
            throw new ZepServiceException("updateEmployeeReleaseDate failed with code: " + returnCode);
        }
    }

    @CacheResult(cacheName = "fehlzeitentype")
    @Override
    public List<AbsenceTime> getAbsenceForEmployee(Employee employee, YearMonth payrollMonth) {
        final ReadFehlzeitRequestType fehlzeitenRequest = new ReadFehlzeitRequestType();
        fehlzeitenRequest.setRequestHeader(zepSoapProvider.createRequestHeaderType());

        final Optional<ReadFehlzeitSearchCriteriaType> searchCriteria = getSearchCriteria(employee, payrollMonth, this::createAbsenceSearchCriteria);

        if (searchCriteria.isEmpty()) {
            return List.of();
        }

        fehlzeitenRequest.setReadFehlzeitSearchCriteria(searchCriteria.get());
        ReadFehlzeitResponseType fehlzeitResponseType = zepSoapPortType.readFehlzeit(fehlzeitenRequest);

        if (fehlzeitResponseType != null
                && fehlzeitResponseType.getFehlzeitListe() != null
                && fehlzeitResponseType.getFehlzeitListe().getFehlzeit() != null) {
            List<FehlzeitType> fehlzeit = fehlzeitResponseType.getFehlzeitListe().getFehlzeit();
            return AbsenceTimeMapper.mapList(fehlzeit);
        }

        return Collections.emptyList();
    }

    @CacheResult(cacheName = "projektzeittype")
    @Override
    public List<ProjectTime> getBillableForEmployee(Employee employee, YearMonth payrollMonth) {
        ReadProjektzeitenResponseType readProjektzeitenResponseType = readProjektzeitenWithSearchCriteria(employee, payrollMonth, this::createProjectTimeSearchCriteria);

        if (readProjektzeitenResponseType != null
                && readProjektzeitenResponseType.getProjektzeitListe() != null
                && readProjektzeitenResponseType.getProjektzeitListe().getProjektzeiten() != null) {
            List<ProjektzeitType> projektzeit = readProjektzeitenResponseType.getProjektzeitListe().getProjektzeiten();
            return ProjectTimeMapper.mapList(projektzeit);
        }

        return Collections.emptyList();
    }

    @Override
    public MonthlyBillInfo getMonthlyBillInfoForEmployee(PersonioEmployee personioEmployee, Employee employee, YearMonth payrollMonth) {
        throw new NotImplementedException("This method is not provided in SOAP, use REST instead"); // not provided due to using REST
    }


    @CacheResult(cacheName = "projectentry")
    @Override
    public List<ProjectEntry> getProjectTimes(Employee employee, YearMonth payrollMonth) {
        ReadProjektzeitenResponseType projectTimeResponse = readProjektzeitenWithSearchCriteria(employee, payrollMonth, this::createProjectTimeSearchCriteria);

        return Optional.ofNullable(projectTimeResponse)
                .flatMap(projectTimes -> Optional.ofNullable(projectTimes.getProjektzeitListe()))
                .stream()
                .flatMap(projectTimes -> projectEntryMapper.mapList(projectTimes.getProjektzeiten()).stream())
                .toList();
    }


    @Override
    public List<ProjectTime> getProjectTimesForEmployeePerProject(String projectID, YearMonth payrollMonth) {
        ReadProjektzeitenResponseType readProjektzeitenResponseType = readProjektzeitenWithSearchCriteria(projectID, payrollMonth, this::createProjectTimesForEmployeePerProjectSearchCriteria);

        if (readProjektzeitenResponseType != null
                && readProjektzeitenResponseType.getProjektzeitListe() != null
                && readProjektzeitenResponseType.getProjektzeitListe().getProjektzeiten() != null) {
            List<ProjektzeitType> projektzeit = readProjektzeitenResponseType.getProjektzeitListe().getProjektzeiten();
            return ProjectTimeMapper.mapList(projektzeit);
        }

        return Collections.emptyList();
    }

    @CacheResult(cacheName = "project")
    @Override
    public List<Project> getProjectsForMonthYear(final YearMonth payrollMonth) {
        final ReadProjekteResponseType readProjekteResponseType = getProjectsInternal(payrollMonth);

        ProjektListeType projektListe = readProjekteResponseType.getProjektListe();
        return projektListe.getProjekt().stream()
                .map(pt -> createProject(pt, payrollMonth))
                .toList();
    }

    @Override
    public List<ProjectHoursSummary> getAllProjectsForMonthAndEmployee(Employee employee, YearMonth payrollMonth) {
        throw new NotImplementedException("This method is not provided in SOAP, use REST instead"); // not provided due to using REST
    }

    @Override
    public double getDoctorsVisitingTimeForMonthAndEmployee(Employee employee, YearMonth payrollMonth) {
        throw new NotImplementedException("This method is not provided in SOAP, use REST instead"); // not provided due to using REST
    }


    @Override
    public Optional<Project> getProjectByName(final String projectName, final YearMonth payrollMonth) {
        final var readProjekteSearchCriteriaType = new ReadProjekteSearchCriteriaType();
        readProjekteSearchCriteriaType.setVon(payrollMonth.atDay(1).toString());
        readProjekteSearchCriteriaType.setBis(payrollMonth.atEndOfMonth().toString());
        readProjekteSearchCriteriaType.setProjektNr(projectName);

        final var readProjekteRequestType = new ReadProjekteRequestType();
        readProjekteRequestType.setRequestHeader(zepSoapProvider.createRequestHeaderType());
        readProjekteRequestType.setReadProjekteSearchCriteria(readProjekteSearchCriteriaType);

        var projects = zepSoapPortType.readProjekte(readProjekteRequestType).getProjektListe().getProjekt();
        if (projects.size() != 1) {
            return Optional.empty();
        }

        return Optional.of(createProject(projects.getFirst(), payrollMonth));
    }

    private <T, U, R> Optional<R> getSearchCriteria(final T par1, final U par2, final BiFunction<T, U, R> searchCriteriaFunction) {
        try {
            return Optional.of(searchCriteriaFunction.apply(par1, par2));
        } catch (DateTimeParseException e) {
            logger.error("invalid release date {0}", e);
            return Optional.empty();
        }
    }

    private <T, U> ReadProjektzeitenResponseType readProjektzeitenWithSearchCriteria(final T par1, final U par2, final BiFunction<T, U, ReadProjektzeitenSearchCriteriaType> searchCriteriaFunction) {
        final ReadProjektzeitenRequestType projektzeitenRequest = new ReadProjektzeitenRequestType();
        projektzeitenRequest.setRequestHeader(zepSoapProvider.createRequestHeaderType());

        Optional<ReadProjektzeitenSearchCriteriaType> searchCriteria = getSearchCriteria(par1, par2, searchCriteriaFunction);

        if (searchCriteria.isEmpty()) {
            return null;
        }

        projektzeitenRequest.setReadProjektzeitenSearchCriteria(searchCriteria.get());
        return zepSoapPortType.readProjektzeiten(projektzeitenRequest);
    }

    private ReadProjekteResponseType getProjectsInternal(final YearMonth payrollMonth) {
        final ReadProjekteSearchCriteriaType readProjekteSearchCriteriaType = new ReadProjekteSearchCriteriaType();
        readProjekteSearchCriteriaType.setVon(payrollMonth.atDay(1).toString());
        readProjekteSearchCriteriaType.setBis(payrollMonth.atEndOfMonth().toString());

        final ReadProjekteRequestType readProjekteRequestType = new ReadProjekteRequestType();
        readProjekteRequestType.setRequestHeader(zepSoapProvider.createRequestHeaderType());
        readProjekteRequestType.setReadProjekteSearchCriteria(readProjekteSearchCriteriaType);

        return zepSoapPortType.readProjekte(readProjekteRequestType);
    }

    private ReadProjektzeitenSearchCriteriaType createProjectTimeSearchCriteria(Employee employee, YearMonth payrollMonth) {
        ReadProjektzeitenSearchCriteriaType searchCriteria = new ReadProjektzeitenSearchCriteriaType();

        searchCriteria.setVon(payrollMonth.atDay(1).toString());
        searchCriteria.setBis(payrollMonth.atEndOfMonth().toString());

        UserIdListeType userIdListType = new UserIdListeType();
        userIdListType.getUserId().add(employee.getUserId());
        searchCriteria.setUserIdListe(userIdListType);
        return searchCriteria;
    }

    private ReadProjektzeitenSearchCriteriaType createProjectTimesForEmployeePerProjectSearchCriteria(String projectID, YearMonth payrollMonth) {
        ReadProjektzeitenSearchCriteriaType searchCriteria = new ReadProjektzeitenSearchCriteriaType();

        searchCriteria.setVon(payrollMonth.atDay(1).toString());
        searchCriteria.setBis(payrollMonth.atEndOfMonth().toString());

        ProjektNrListeType projectListType = new ProjektNrListeType();
        projectListType.getProjektNr().add(projectID);
        searchCriteria.setProjektNrListe(projectListType);

        return searchCriteria;
    }

    private ReadFehlzeitSearchCriteriaType createAbsenceSearchCriteria(Employee employee, YearMonth payrollMonth) {
        ReadFehlzeitSearchCriteriaType searchCriteria = new ReadFehlzeitSearchCriteriaType();

        searchCriteria.setStartdatum(payrollMonth.atDay(1).toString());
        searchCriteria.setEnddatum(payrollMonth.atEndOfMonth().toString());

        searchCriteria.setUserId(employee.getUserId());
        return searchCriteria;
    }


    private Project createProject(final ProjektType projektType, final YearMonth payrollMonth) {
        Optional<String> endDateString = Optional.ofNullable(projektType.getEndeDatum());
        LocalDate endDate = endDateString.map(LocalDate::parse)
                .orElseGet(() -> LocalDate.now().plusYears(5).with(TemporalAdjusters.lastDayOfYear()));

        return Project.builder()
                .zepId(projektType.getProjektId())
                .projectId(projektType.getProjektNr())
                .description(projektType.getBezeichnung())
                .startDate(LocalDate.parse(projektType.getStartDatum()))
                .endDate(endDate)
                .employees(createProjectEmployees(projektType.getProjektmitarbeiterListe(), payrollMonth))
                .leads(createProjectLeads(projektType.getProjektmitarbeiterListe(), payrollMonth))
                .categories(createCategories(projektType))
                .billabilityPreset(
                        BillabilityPreset.byZepId(projektType.getVoreinstFakturierbarkeit())
                                .orElseThrow(billabilityNotDeterminable(projektType.getProjektNr()))
                )
                .build();
    }

    private static Supplier<IllegalArgumentException> billabilityNotDeterminable(String projectName) {
        return () -> new IllegalArgumentException("BillabilityPreset could not be determined for project " + projectName);
    }

    private List<String> createProjectEmployees(final ProjektMitarbeiterListeType projektMitarbeiterListeType, final YearMonth payrollMonth) {
        return projektMitarbeiterListeType.getProjektmitarbeiter()
                .stream()
                .filter(e -> filterActiveEmployees(payrollMonth, e.getVon(), e.getBis()))
                .map(ProjektMitarbeiterType::getUserId)
                .toList();
    }

    private boolean filterActiveEmployees(final YearMonth payrollMonth, final String inProjectFrom, final String inProjectUntil) {
        final LocalDate firstOfMonth = payrollMonth.atDay(1);
        final LocalDate lastOfMonth = payrollMonth.atEndOfMonth();

        final LocalDate from = inProjectFrom != null ? DateUtils.parseDate(inProjectFrom) : LocalDate.MIN;
        final LocalDate until = inProjectUntil != null ? DateUtils.parseDate(inProjectUntil) : LocalDate.MAX;

        return (from.isBefore(firstOfMonth) || isBetween(from, firstOfMonth, lastOfMonth)) && (until.isAfter(lastOfMonth) || isBetween(until, firstOfMonth, lastOfMonth));
    }

    private boolean isBetween(final LocalDate from, final LocalDate firstOfMonth, final LocalDate lastOfMonth) {
        return !from.isBefore(firstOfMonth) && !from.isAfter(lastOfMonth);
    }

    private List<String> createProjectLeads(final ProjektMitarbeiterListeType projektMitarbeiterListeType, final YearMonth payrollMonth) {
        return projektMitarbeiterListeType.getProjektmitarbeiter()
                .stream()
                .filter(e -> filterActiveEmployees(payrollMonth, e.getVon(), e.getBis()))
                .filter(projektMitarbeiterType -> PROJECT_LEAD_RANGE.contains(projektMitarbeiterType.getIstProjektleiter()))
                .map(ProjektMitarbeiterType::getUserId)
                .toList();
    }

    private List<String> createCategories(final ProjektType projektType) {
        return Optional.ofNullable(projektType.getKategorieListe())
                .orElse(new KategorieListeType())
                .getKategorie()
                .stream()
                .map(KategorieType::getKurzform)
                .toList();
    }

    /**
     * @param readMitarbeiterSearchCriteriaType search for specific criterias in zep
     * @return list of employees
     */
    private List<Employee> getEmployeeInternal(final ReadMitarbeiterSearchCriteriaType readMitarbeiterSearchCriteriaType) {

        final ReadMitarbeiterRequestType readMitarbeiterRequestType = new ReadMitarbeiterRequestType();
        readMitarbeiterRequestType.setRequestHeader(zepSoapProvider.createRequestHeaderType());

        if (readMitarbeiterSearchCriteriaType != null) {
            readMitarbeiterRequestType.setReadMitarbeiterSearchCriteria(readMitarbeiterSearchCriteriaType);
        }

        return Optional.ofNullable(zepSoapPortType.readMitarbeiter(readMitarbeiterRequestType))
                .flatMap(readMitarbeiterResponse -> Optional.ofNullable(readMitarbeiterResponse.getMitarbeiterListe()))
                .stream()
                .flatMap(mitarbeiterListe -> mitarbeiterListe.getMitarbeiter().stream())
                .map(employeeMapper::map)
                .toList();
    }
}

package com.gepardec.mega.zep;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.service.mapper.EmployeeMapper;
import com.gepardec.mega.zep.mapper.ProjectEntryMapper;
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
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.gepardec.mega.domain.utils.DateUtils.getFirstDayOfCurrentMonth;
import static com.gepardec.mega.domain.utils.DateUtils.getLastDayOfCurrentMonth;

@RequestScoped
public class ZepServiceImpl implements ZepService {

    private static final String BILLABLE_TIME_FORMAT = "HH:mm";

    private static final Range<Integer> PROJECT_LEAD_RANGE = Range.between(1, 2);

    private final EmployeeMapper employeeMapper;

    private final Logger logger;

    private final ZepSoapPortType zepSoapPortType;

    private final ZepSoapProvider zepSoapProvider;

    private final ProjectEntryMapper projectEntryMapper;

    @Inject
    public ZepServiceImpl(final EmployeeMapper employeeMapper,
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

        return getEmployeeInternal(readMitarbeiterSearchCriteriaType).stream()
                .findFirst()
                .orElse(null);
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
    public List<FehlzeitType> getAbsenceForEmployee(Employee employee, LocalDate date) {
        final ReadFehlzeitRequestType fehlzeitenRequest = new ReadFehlzeitRequestType();
        fehlzeitenRequest.setRequestHeader(zepSoapProvider.createRequestHeaderType());

        final Optional<ReadFehlzeitSearchCriteriaType> searchCriteria = getSearchCriteria(employee, date, this::createAbsenceSearchCriteria);

        if (searchCriteria.isEmpty()) {
            return null;
        }

        fehlzeitenRequest.setReadFehlzeitSearchCriteria(searchCriteria.get());
        ReadFehlzeitResponseType fehlzeitResponseType = zepSoapPortType.readFehlzeit(fehlzeitenRequest);

        if (fehlzeitResponseType != null
                && fehlzeitResponseType.getFehlzeitListe() != null
                && fehlzeitResponseType.getFehlzeitListe().getFehlzeit() != null) {
            return fehlzeitResponseType.getFehlzeitListe().getFehlzeit();
        }

        return Collections.emptyList();
    }

    @CacheResult(cacheName = "projektzeittype")
    @Override
    public List<ProjektzeitType> getBillableForEmployee(Employee employee, LocalDate date) {
        ReadProjektzeitenResponseType readProjektzeitenResponseType = readProjektzeitenWithSearchCriteria(employee, date, this::createProjectTimeSearchCriteria);

        if (readProjektzeitenResponseType != null
                && readProjektzeitenResponseType.getProjektzeitListe() != null
                && readProjektzeitenResponseType.getProjektzeitListe().getProjektzeiten() != null) {
            return readProjektzeitenResponseType.getProjektzeitListe().getProjektzeiten();

        }
        return Collections.emptyList();
    }

    @Override
    public String getInternalTimesForEmployee(@Nonnull List<ProjektzeitType> projektzeitTypeList, @Nonnull Employee employee) {
        return getWorkingTimesForEmployee(projektzeitTypeList, employee, Predicate.not(ProjektzeitType::isIstFakturierbar));
    }

    @Override
    public String getBillableTimesForEmployee(@Nonnull List<ProjektzeitType> projektzeitTypeList, @Nonnull Employee employee) {
        return getWorkingTimesForEmployee(projektzeitTypeList, employee, ProjektzeitType::isIstFakturierbar);
    }

    @Override
    public String getTotalWorkingTimeForEmployee(@Nonnull List<ProjektzeitType> projektzeitTypeList, @Nonnull Employee employee) {
        return getWorkingTimesForEmployee(projektzeitTypeList, employee, $ -> true);
    }

    private String getWorkingTimesForEmployee(List<ProjektzeitType> projektzeitTypeList, Employee employee, Predicate<ProjektzeitType> billableFilter) {
        Duration totalBillable = projektzeitTypeList.stream()
                .filter(pzt -> pzt.getUserId().equals(employee.getUserId()))
                .filter(billableFilter)
                .map(pzt -> LocalTime.parse(pzt.getDauer()))
                .map(lt -> Duration.between(LocalTime.MIN, lt))
                .reduce(Duration.ZERO, Duration::plus);

        return DurationFormatUtils.formatDuration(totalBillable.toMillis(), BILLABLE_TIME_FORMAT);
    }

    @CacheResult(cacheName = "projectentry")
    @Override
    public List<ProjectEntry> getProjectTimes(Employee employee, LocalDate date) {
        ReadProjektzeitenResponseType projectTimeResponse = readProjektzeitenWithSearchCriteria(employee, date, this::createProjectTimeSearchCriteria);

        return Optional.ofNullable(projectTimeResponse)
                .flatMap(projectTimes -> Optional.ofNullable(projectTimes.getProjektzeitListe()))
                .stream()
                .flatMap(projectTimes -> projectEntryMapper.mapList(projectTimes.getProjektzeiten()).stream())
                .collect(Collectors.toList());
    }

    @CacheResult(cacheName = "projektzeittype")
    @Override
    public List<ProjektzeitType> getProjectTimesForEmployeePerProject(String projectID, LocalDate curDate) {
        ReadProjektzeitenResponseType readProjektzeitenResponseType = readProjektzeitenWithSearchCriteria(projectID, curDate, this::createProjectTimesForEmployeePerProjectSearchCriteria);

        if (readProjektzeitenResponseType != null
                && readProjektzeitenResponseType.getProjektzeitListe() != null
                && readProjektzeitenResponseType.getProjektzeitListe().getProjektzeiten() != null) {
            return readProjektzeitenResponseType.getProjektzeitListe().getProjektzeiten();
        }

        return Collections.emptyList();
    }

    @CacheResult(cacheName = "project")
    @Override
    public List<Project> getProjectsForMonthYear(final LocalDate monthYear) {
        final ReadProjekteResponseType readProjekteResponseType = getProjectsInternal(monthYear);

        ProjektListeType projektListe = readProjekteResponseType.getProjektListe();
        return projektListe.getProjekt()
                .stream()
                .map(pt -> createProject(pt, monthYear))
                .collect(Collectors.toList());
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

    private ReadProjekteResponseType getProjectsInternal(final LocalDate monthYear) {
        final ReadProjekteSearchCriteriaType readProjekteSearchCriteriaType = new ReadProjekteSearchCriteriaType();
        readProjekteSearchCriteriaType.setVon(DateTimeFormatter.ISO_LOCAL_DATE.format(monthYear.with(TemporalAdjusters.firstDayOfMonth())));
        readProjekteSearchCriteriaType.setBis(DateTimeFormatter.ISO_LOCAL_DATE.format(monthYear.with(TemporalAdjusters.lastDayOfMonth())));

        final ReadProjekteRequestType readProjekteRequestType = new ReadProjekteRequestType();
        readProjekteRequestType.setRequestHeader(zepSoapProvider.createRequestHeaderType());
        readProjekteRequestType.setReadProjekteSearchCriteria(readProjekteSearchCriteriaType);

        return zepSoapPortType.readProjekte(readProjekteRequestType);
    }

    private ReadProjektzeitenSearchCriteriaType createProjectTimeSearchCriteria(Employee employee, LocalDate date) {
        ReadProjektzeitenSearchCriteriaType searchCriteria = new ReadProjektzeitenSearchCriteriaType();

        searchCriteria.setVon(getFirstDayOfCurrentMonth(date));
        searchCriteria.setBis(getLastDayOfCurrentMonth(date));

        UserIdListeType userIdListType = new UserIdListeType();
        userIdListType.getUserId().add(employee.getUserId());
        searchCriteria.setUserIdListe(userIdListType);
        return searchCriteria;
    }

    private ReadProjektzeitenSearchCriteriaType createProjectTimesForEmployeePerProjectSearchCriteria(String projectID, LocalDate curDate) {
        ReadProjektzeitenSearchCriteriaType searchCriteria = new ReadProjektzeitenSearchCriteriaType();

        searchCriteria.setVon(DateTimeFormatter.ISO_LOCAL_DATE.format(curDate.with(TemporalAdjusters.firstDayOfMonth())));
        searchCriteria.setBis(DateTimeFormatter.ISO_LOCAL_DATE.format(curDate.with(TemporalAdjusters.lastDayOfMonth())));

        ProjektNrListeType projectListType = new ProjektNrListeType();
        projectListType.getProjektNr().add(projectID);
        searchCriteria.setProjektNrListe(projectListType);

        return searchCriteria;
    }

    private ReadFehlzeitSearchCriteriaType createAbsenceSearchCriteria(Employee employee, LocalDate date) {
        ReadFehlzeitSearchCriteriaType searchCriteria = new ReadFehlzeitSearchCriteriaType();

        searchCriteria.setStartdatum(getFirstDayOfCurrentMonth(date));
        searchCriteria.setEnddatum(getLastDayOfCurrentMonth(date));

        searchCriteria.setUserId(employee.getUserId());
        return searchCriteria;
    }

    private Project createProject(final ProjektType projektType, final LocalDate monthYear) {
        Optional<String> endDateString = Optional.ofNullable(projektType.getEndeDatum());
        LocalDate endDate = endDateString.isPresent() ?
                LocalDate.parse(endDateString.get()) :
                LocalDate.now()
                        .plusYears(5)
                        .with(TemporalAdjusters.lastDayOfYear());

        return Project.builder()
                .zepId(projektType.getProjektId())
                .projectId(projektType.getProjektNr())
                .description(projektType.getBezeichnung())
                .startDate(LocalDate.parse(projektType.getStartDatum()))
                .endDate(endDate)
                .employees(createProjectEmployees(projektType.getProjektmitarbeiterListe(), monthYear))
                .leads(createProjectLeads(projektType.getProjektmitarbeiterListe(), monthYear))
                .categories(createCategories(projektType))
                .build();
    }

    private List<String> createProjectEmployees(final ProjektMitarbeiterListeType projektMitarbeiterListeType, final LocalDate monthYear) {
        return projektMitarbeiterListeType.getProjektmitarbeiter()
                .stream()
                .filter(e -> filterActiveEmployees(monthYear, e.getVon(), e.getBis()))
                .map(ProjektMitarbeiterType::getUserId)
                .collect(Collectors.toList());
    }

    private boolean filterActiveEmployees(final LocalDate monthYear, final String inProjectFrom, final String inProjectUntil) {
        final LocalDate firstOfMonth = monthYear.with(TemporalAdjusters.firstDayOfMonth());
        final LocalDate lastOfMonth = monthYear.with(TemporalAdjusters.lastDayOfMonth());

        final LocalDate from = inProjectFrom != null ? DateUtils.parseDate(inProjectFrom) : LocalDate.MIN;
        final LocalDate until = inProjectUntil != null ? DateUtils.parseDate(inProjectUntil) : LocalDate.MAX;

        return (from.isBefore(firstOfMonth) || isBetween(from, firstOfMonth, lastOfMonth)) && (until.isAfter(lastOfMonth) || isBetween(until, firstOfMonth, lastOfMonth));
    }

    private boolean isBetween(final LocalDate from, final LocalDate firstOfMonth, final LocalDate lastOfMonth) {
        return !from.isBefore(firstOfMonth) && !from.isAfter(lastOfMonth);
    }

    private List<String> createProjectLeads(final ProjektMitarbeiterListeType projektMitarbeiterListeType, final LocalDate monthYear) {
        return projektMitarbeiterListeType.getProjektmitarbeiter()
                .stream()
                .filter(e -> filterActiveEmployees(monthYear, e.getVon(), e.getBis()))
                .filter(projektMitarbeiterType -> PROJECT_LEAD_RANGE.contains(projektMitarbeiterType.getIstProjektleiter()))
                .map(ProjektMitarbeiterType::getUserId)
                .collect(Collectors.toList());
    }

    private List<String> createCategories(final ProjektType projektType) {
        return Optional.ofNullable(projektType.getKategorieListe()).orElse(new KategorieListeType())
                .getKategorie()
                .stream()
                .map(KategorieType::getKurzform)
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
    }
}

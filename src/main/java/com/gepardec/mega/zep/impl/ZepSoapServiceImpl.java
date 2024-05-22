package com.gepardec.mega.zep.impl;

import com.gepardec.mega.domain.model.*;
import com.gepardec.mega.db.entity.common.PaymentMethodType;
import com.gepardec.mega.domain.model.Bill;
import com.gepardec.mega.domain.model.BillabilityPreset;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.service.mapper.EmployeeMapper;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.ZepSoapProvider;
import com.gepardec.mega.zep.mapper.AbsenceTimeMapper;
import com.gepardec.mega.zep.mapper.ProjectEntryMapper;
import com.gepardec.mega.zep.mapper.ProjectTimeMapper;
import de.provantis.zep.*;
import de.provantis.zep.BelegListeType;
import de.provantis.zep.BelegType;
import de.provantis.zep.BelegbetragType;
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
import de.provantis.zep.ReadBelegAnhangRequestType;
import de.provantis.zep.ReadBelegAnhangResponseType;
import de.provantis.zep.ReadBelegAnhangSearchCriteriaType;
import de.provantis.zep.ReadBelegRequestType;
import de.provantis.zep.ReadBelegResponseType;
import de.provantis.zep.ReadBelegSearchCriteriaType;
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
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.gepardec.mega.domain.utils.DateUtils.formatDate;
import static com.gepardec.mega.domain.utils.DateUtils.getFirstDayOfCurrentMonth;
import static com.gepardec.mega.domain.utils.DateUtils.getLastDayOfCurrentMonth;

@RequestScoped
@Soap
public class ZepSoapServiceImpl implements ZepService {


    private static final Range<Integer> PROJECT_LEAD_RANGE = Range.between(1, 2);

    private final EmployeeMapper employeeMapper;

    private final Logger logger;

    private final de.provantis.zep.ZepSoapPortType zepSoapPortType;

    private final ZepSoapProvider zepSoapProvider;

    private final ProjectEntryMapper projectEntryMapper;

    private final MonthlyReportService monthlyReportService;

    @Inject
    public ZepSoapServiceImpl(final EmployeeMapper employeeMapper,
                              final Logger logger,
                              final ZepSoapPortType zepSoapPortType,
                              final ZepSoapProvider zepSoapProvider,
                              final ProjectEntryMapper projectEntryMapper,
                              final MonthlyReportService monthlyReportService) {
        this.employeeMapper = employeeMapper;
        this.logger = logger;
        this.zepSoapPortType = zepSoapPortType;
        this.zepSoapProvider = zepSoapProvider;
        this.projectEntryMapper = projectEntryMapper;
        this.monthlyReportService = monthlyReportService;
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
    public List<AbsenceTime> getAbsenceForEmployee(Employee employee, LocalDate date) {
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
            List<FehlzeitType> fehlzeit = fehlzeitResponseType.getFehlzeitListe().getFehlzeit();
            return AbsenceTimeMapper.mapList(fehlzeit);
        }

        return Collections.emptyList();
    }

    @CacheResult(cacheName = "projektzeittype")
    @Override
    public List<ProjectTime> getBillableForEmployee(Employee employee, LocalDate date) {
        ReadProjektzeitenResponseType readProjektzeitenResponseType = readProjektzeitenWithSearchCriteria(employee, date, this::createProjectTimeSearchCriteria);

        if (readProjektzeitenResponseType != null
                && readProjektzeitenResponseType.getProjektzeitListe() != null
                && readProjektzeitenResponseType.getProjektzeitListe().getProjektzeiten() != null) {
            List<ProjektzeitType> projektzeit = readProjektzeitenResponseType.getProjektzeitListe().getProjektzeiten();
            return ProjectTimeMapper.mapList(projektzeit);
        }

        return Collections.emptyList();
    }


    @CacheResult(cacheName = "projectentry")
    @Override
    public List<ProjectEntry> getProjectTimes(Employee employee, LocalDate date) {
        ReadProjektzeitenResponseType projectTimeResponse = readProjektzeitenWithSearchCriteria(employee, date, this::createProjectTimeSearchCriteria);

        return Optional.ofNullable(projectTimeResponse)
                .flatMap(projectTimes -> Optional.ofNullable(projectTimes.getProjektzeitListe()))
                .stream()
                .flatMap(projectTimes -> projectEntryMapper.mapList(projectTimes.getProjektzeiten()).stream())
                .toList();
    }


    @Override
    public List<ProjectTime> getProjectTimesForEmployeePerProject(String projectID, LocalDate curDate) {
        ReadProjektzeitenResponseType readProjektzeitenResponseType = readProjektzeitenWithSearchCriteria(projectID, curDate, this::createProjectTimesForEmployeePerProjectSearchCriteria);

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
    public List<Project> getProjectsForMonthYear(final LocalDate monthYear) {
        final ReadProjekteResponseType readProjekteResponseType = getProjectsInternal(monthYear);

        ProjektListeType projektListe = readProjekteResponseType.getProjektListe();
        return projektListe.getProjekt().stream()
                .map(pt -> createProject(pt, monthYear))
                .toList();
    }


    @Override
    public List<Bill> getBillsForEmployeeByMonth(final Employee employee, YearMonth yearMonth) {
        String fromDate = getFirstDayOfCurrentMonth(LocalDate.now());
        String toDate = formatDate(getLastDayOfCurrentMonth(fromDate));

        if(yearMonth != null){
            fromDate = formatDate(yearMonth.atDay(1));
            toDate = formatDate(getLastDayOfCurrentMonth(fromDate));
        }

        final ReadBelegResponseType readBelegResponseType = getBillsInternal(employee, fromDate, toDate);

        BelegListeType billList = readBelegResponseType.getBelegListe();
        return billList.getBeleg().stream()
                .map(this::createBill)
                .toList();
    }

    @Override
    public List<ProjectHoursSummary> getAllProjectsForMonthAndEmployee(Employee employee, YearMonth yearMonth) {
        throw new NotImplementedException("This method is not provided in SOAP, use REST instead"); // not provided due to using REST
    }


    @Override
    public Optional<Project> getProjectByName(final String projectName, final LocalDate monthYear) {
        final var readProjekteSearchCriteriaType = new ReadProjekteSearchCriteriaType();
        readProjekteSearchCriteriaType.setVon(DateTimeFormatter.ISO_LOCAL_DATE.format(monthYear.with(TemporalAdjusters.firstDayOfMonth())));
        readProjekteSearchCriteriaType.setBis(DateTimeFormatter.ISO_LOCAL_DATE.format(monthYear.with(TemporalAdjusters.lastDayOfMonth())));
        readProjekteSearchCriteriaType.setProjektNr(projectName);

        final var readProjekteRequestType = new ReadProjekteRequestType();
        readProjekteRequestType.setRequestHeader(zepSoapProvider.createRequestHeaderType());
        readProjekteRequestType.setReadProjekteSearchCriteria(readProjekteSearchCriteriaType);

        var projects = zepSoapPortType.readProjekte(readProjekteRequestType).getProjektListe().getProjekt();
        if (projects.size() != 1) {
            return Optional.empty();
        }

        return Optional.of(createProject(projects.get(0), monthYear));
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

    private ReadBelegResponseType getBillsInternal(final Employee employee, final String from, final String to) {
        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);
        LocalDate midOfCurrentMonth = LocalDate.now().withDayOfMonth(14);

        String dateForSearchCriteriaFrom = "";
        String dateForSearchCriteriaTo ="";


        if(from == null && to == null) {
            if (now.isAfter(midOfCurrentMonth) && monthlyReportService.isMonthConfirmedFromEmployee(employee, firstOfPreviousMonth)) {
                dateForSearchCriteriaFrom = getFirstDayOfCurrentMonth(now);
                dateForSearchCriteriaTo = getLastDayOfCurrentMonth(now);
            }
        } else {
            dateForSearchCriteriaFrom = from;
            dateForSearchCriteriaTo = to;
        }

        final ReadBelegSearchCriteriaType readBelegSearchCriteriaType = createBillSearchCriteria(employee, dateForSearchCriteriaFrom, dateForSearchCriteriaTo);

        final ReadBelegRequestType readBelegRequestType = new ReadBelegRequestType();
        readBelegRequestType.setRequestHeader(zepSoapProvider.createRequestHeaderType());
        readBelegRequestType.setReadBelegSearchCriteria(readBelegSearchCriteriaType);

        return zepSoapPortType.readBeleg(readBelegRequestType);
    }

    private ReadBelegAnhangResponseType getAttachmentForBill(int billNumber) {
        final ReadBelegAnhangSearchCriteriaType readBelegAnhangSearchCriteriaType = createAttachmentSearchCriteria(billNumber);

        final ReadBelegAnhangRequestType readBelegAnhangRequestType = new ReadBelegAnhangRequestType();
        readBelegAnhangRequestType.setRequestHeader(zepSoapProvider.createRequestHeaderType());
        readBelegAnhangRequestType.setReadBelegAnhangSearchCriteria(readBelegAnhangSearchCriteriaType);

        return zepSoapPortType.readBelegAnhang(readBelegAnhangRequestType);
    }

    private ReadBelegAnhangSearchCriteriaType createAttachmentSearchCriteria(int billNumber) {
        ReadBelegAnhangSearchCriteriaType searchCriteria = new ReadBelegAnhangSearchCriteriaType();
        searchCriteria.setBelegNr(billNumber);
        return searchCriteria;
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

    private ReadBelegSearchCriteriaType createBillSearchCriteria(Employee employee, String from, String to){
        ReadBelegSearchCriteriaType searchCriteria = new ReadBelegSearchCriteriaType();

        //setUserIdListe needs this special parameter below, could be more than one id but in this case only one is useful
        UserIdListeType userIdListe = new UserIdListeType();
        userIdListe.setUserId(List.of(employee.getUserId()));

        searchCriteria.setUserIdListe(userIdListe);
        searchCriteria.setVon(from);
        searchCriteria.setBis(to);

        return searchCriteria;
    }

    private Bill createBill(final BelegType belegType) {
        List<BelegbetragType> amountList = belegType.getBelegbetragListe().getBelegbetrag();
        ReadBelegAnhangResponseType readBelegAnhangResponseType = getAttachmentForBill(belegType.getBelegNr());

        //because it is not possible to store a byte[] in json
        String attachmentBase64String = null;
        String attachmentFilename = null;

        if(readBelegAnhangResponseType.getAnhang().getInhalt() != null){
            byte[] attachmentBase64 = readBelegAnhangResponseType.getAnhang().getInhalt();
            attachmentBase64String = Base64.encodeBase64String(attachmentBase64);
            attachmentFilename = readBelegAnhangResponseType.getAnhang().getName();
        }

        // would be different if there is more than one tax rate on one bill
        // -> is not our case, if it would be one should consider changing structure of Bill-Object and iterate over all entries of amountList
        double bruttoValue = 0.0;
        if(amountList.size() == 1){
            bruttoValue = amountList.get(0).getBetrag() * amountList.get(0).getMenge();
        }

        return Bill.builder()
                .billDate(DateUtils.parseDate(belegType.getDatum()))
                .bruttoValue(bruttoValue)
                .billType(belegType.getBelegart())
                .paymentMethodType(PaymentMethodType.getByName(belegType.getZahlungsart()).orElse(null)) //can actually never be null, because it is required in ZEP -> Optional<...> due to Enum
                .projectName(belegType.getProjektNr())
                .attachmentBase64(attachmentBase64String)
                .attachmentFileName(attachmentFilename)
                .build();
    }

    private Project createProject(final ProjektType projektType, final LocalDate monthYear) {
        Optional<String> endDateString = Optional.ofNullable(projektType.getEndeDatum());
        LocalDate endDate = endDateString.map(LocalDate::parse)
                .orElseGet(() -> LocalDate.now().plusYears(5).with(TemporalAdjusters.lastDayOfYear()));

        return Project.builder()
                .zepId(projektType.getProjektId())
                .projectId(projektType.getProjektNr())
                .description(projektType.getBezeichnung())
                .startDate(LocalDate.parse(projektType.getStartDatum()))
                .endDate(endDate)
                .employees(createProjectEmployees(projektType.getProjektmitarbeiterListe(), monthYear))
                .leads(createProjectLeads(projektType.getProjektmitarbeiterListe(), monthYear))
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

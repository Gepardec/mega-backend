package com.gepardec.mega.zep;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.service.impl.employee.EmployeeMapper;
import com.gepardec.mega.zep.mapper.ProjectTimeMapper;
import de.provantis.zep.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.gepardec.mega.domain.utils.DateUtils.getFirstDayOfFollowingMonth;
import static com.gepardec.mega.domain.utils.DateUtils.getLastDayOfFollowingMonth;

@RequestScoped
public class ZepServiceImpl implements ZepService {

    private final EmployeeMapper employeeMapper;
    private final Logger logger;
    private final ZepSoapPortType zepSoapPortType;
    private final ZepSoapProvider zepSoapProvider;
    private final ProjectTimeMapper projectTimeMapper;

    @Inject
    public ZepServiceImpl(final EmployeeMapper employeeMapper,
                          final Logger logger,
                          final ZepSoapPortType zepSoapPortType,
                          final ZepSoapProvider zepSoapProvider,
                          final ProjectTimeMapper projectTimeMapper) {
        this.employeeMapper = employeeMapper;
        this.logger = logger;
        this.zepSoapPortType = zepSoapPortType;
        this.zepSoapProvider = zepSoapProvider;
        this.projectTimeMapper = projectTimeMapper;
    }

    @Override
    public Employee getEmployee(final String userId) {
        final ReadMitarbeiterSearchCriteriaType readMitarbeiterSearchCriteriaType = new ReadMitarbeiterSearchCriteriaType();
        readMitarbeiterSearchCriteriaType.setUserId(userId);

        return getEmployeeInternal(readMitarbeiterSearchCriteriaType).stream().findFirst().orElse(null);
    }

    @Override
    public List<Employee> getEmployees() {
        return getEmployeeInternal(null);
    }

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

        final AtomicReference<String> returnCode = new AtomicReference<>(null);
        Optional.ofNullable(updateMitarbeiterResponseType).flatMap(response -> Optional.ofNullable(response.getResponseHeader()))
                .ifPresent((header) -> returnCode.set(header.getReturnCode()));

        logger.info("finish update user {} with response {}", userId, returnCode.get());

        if (StringUtils.isNotEmpty(returnCode.get()) && Integer.parseInt(returnCode.get()) != 0) {
            throw new ZepServiceException("updateEmployeeReleaseDate failed with code: " + returnCode.get());
        }
    }

    @Override
    public List<ProjectEntry> getProjectTimes(Employee employee) {
        final ReadProjektzeitenRequestType projektzeitenRequest = new ReadProjektzeitenRequestType();
        projektzeitenRequest.setRequestHeader(zepSoapProvider.createRequestHeaderType());

        final ReadProjektzeitenSearchCriteriaType searchCriteria;
        try {
            searchCriteria = createProjectTimeSearchCriteria(employee);
        } catch (DateTimeParseException e) {
            logger.error("invalid release date {0}", e);
            return null;
        }
        projektzeitenRequest.setReadProjektzeitenSearchCriteria(searchCriteria);

        ReadProjektzeitenResponseType projectTimeResponse = zepSoapPortType.readProjektzeiten(projektzeitenRequest);

        return projectTimeMapper.mapToEntryList(projectTimeResponse.getProjektzeitListe().getProjektzeiten());
    }

    @Override
    public Map<User, List<String>> getProjectsForUsersAndYear(final LocalDate monthYear, final List<User> users) {
        final ReadProjekteResponseType readProjekteResponseType = getProjectsInternal(monthYear);
        final Map<User, List<String>> projectsForUsers = new HashMap<>();

        readProjekteResponseType.getProjektListe().getProjekt().forEach(p -> p.getProjektmitarbeiterListe().getProjektmitarbeiter()
                .stream().filter(pm -> users.stream().anyMatch(u -> u.userId().equals(pm.getUserId())))
                .forEach(pm -> {
                    final User user = users.stream().filter(u -> u.userId().equals(pm.getUserId())).findFirst().orElse(null);
                    if (projectsForUsers.containsKey(user)) {
                        projectsForUsers.get(user).add(p.getProjektNr());
                    } else {
                        projectsForUsers.put(user, new ArrayList<>(Collections.singletonList(pm.getUserId())));
                    }
                }));

        return projectsForUsers;
    }

    @Override
    public Map<String, List<User>> getProjectLeadsForProjectsAndYear(final LocalDate monthYear, final List<User> users) {
        final ReadProjekteResponseType readProjekteResponseType = getProjectsInternal(monthYear);
        final Map<String, List<User>> projectLeadsForProjects = new HashMap<>();

        readProjekteResponseType.getProjektListe().getProjekt().forEach(p -> p.getProjektmitarbeiterListe().getProjektmitarbeiter()
                .forEach(pm -> {
                    if (pm.getIstProjektleiter() == 1) {
                        final User user = users.stream().filter(u -> u.userId().equals(pm.getUserId())).findFirst().orElse(null);
                        if (user != null) {
                            if (!projectLeadsForProjects.containsKey(p.getProjektNr())) {
                                projectLeadsForProjects.put(p.getProjektNr(), new ArrayList<>(Collections.singletonList(user)));
                            } else {
                                projectLeadsForProjects.get(p.getProjektNr()).add(user);
                            }
                        }
                    }
                }));

        return projectLeadsForProjects;
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

    private ReadProjektzeitenSearchCriteriaType createProjectTimeSearchCriteria(Employee employee) {
        ReadProjektzeitenSearchCriteriaType searchCriteria = new ReadProjektzeitenSearchCriteriaType();

        final String releaseDate = employee.releaseDate();
        searchCriteria.setVon(getFirstDayOfFollowingMonth(releaseDate));
        searchCriteria.setBis(getLastDayOfFollowingMonth(releaseDate));

        UserIdListeType userIdListType = new UserIdListeType();
        userIdListType.getUserId().add(employee.userId());
        searchCriteria.setUserIdListe(userIdListType);
        return searchCriteria;
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

        final ReadMitarbeiterResponseType readMitarbeiterResponseType = zepSoapPortType.readMitarbeiter(readMitarbeiterRequestType);
        final List<Employee> result = new ArrayList<>();

        Optional.ofNullable(readMitarbeiterResponseType).flatMap(readMitarbeiterResponse -> Optional
                .ofNullable(readMitarbeiterResponse.getMitarbeiterListe())).ifPresent(mitarbeiterListe ->
                result.addAll(mitarbeiterListe.getMitarbeiter().stream().map(employeeMapper::map).collect(Collectors.toList()))
        );

        return result;
    }
}

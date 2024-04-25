package com.gepardec.mega.service.impl;

import com.gepardec.mega.application.configuration.ApplicationConfig;
import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.UserRepository;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil;
import com.gepardec.mega.rest.mapper.EmployeeMapper;
import com.gepardec.mega.rest.model.EmployeeDto;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.ProjectService;
import com.gepardec.mega.service.api.StepEntryService;
import com.gepardec.mega.service.api.SyncService;
import com.gepardec.mega.service.mapper.SyncServiceMapper;
import com.gepardec.mega.zep.ZepService;
import de.provantis.zep.FehlzeitType;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Thomas Herzog <herzog.thomas81@gmail.com>
 * @since 10/3/2020
 */
@Dependent
@Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
public class SyncServiceImpl implements SyncService {

    @Inject
    Logger log;

    @Inject
    EmployeeService employeeService;

    @Inject
    ZepService zepService;

    @Inject
    ProjectService projectService;

    @Inject
    UserRepository userRepository;

    @Inject
    ApplicationConfig applicationConfig;
    @Inject
    StepEntryService stepEntryService;

    @Inject
    SyncServiceMapper mapper;

    @Inject
    EmployeeMapper employeeMapper;

    @Inject
    Logger logger;

    @Override
    public void syncEmployees() {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Started user sync: {}", Instant.ofEpochMilli(stopWatch.getStartTime()));

        final List<Project> projects = projectService.getProjectsForMonthYear(DateUtils.getFirstDayOfCurrentMonth());
        log.info("Loaded projects (for employee generation): {}", projects.size());

        final List<Employee> employees = employeeService.getAllActiveEmployees();
        log.info("Loaded employees: {}", employees.size());

        final List<User> users = userRepository.listAll();
        log.info("Existing users: {}", users.size());

        createNonExistentUsers(employees, users, projects);
        updateModifiedUsers(employees, users, projects);
        deactivateDeletedOrInactiveUsers(employees, users);

        log.info("User sync took: {}ms", stopWatch.getTime());
        log.info("Finished user sync: {}", Instant.ofEpochMilli(stopWatch.getStartTime() + stopWatch.getTime()));
    }

    @Override
    public List<EmployeeDto> syncUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth() {
        //to avoid having a look at external employees filter
        List<Employee> activeAndInternalEmployees = employeeService.getAllActiveEmployees()
                                                                   .stream()
                                                                   .filter(e -> !e.getUserId().startsWith("e"))
                                                                   .toList();
        List<EmployeeDto> updatedEmployees = new ArrayList<>();
        List<Employee> absentEmployees = new ArrayList<>();

        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);
        //use this firstOfPreviousMonth.getYear() because of january and december
        LocalDate lastOfPreviousMonth = DateUtils.getLastDayOfMonth(firstOfPreviousMonth.getYear(), firstOfPreviousMonth.getMonth().getValue());

        //to avoid getting employees more often then once
        List<Employee> activeAndInternalAndNotReleasedEmployees = activeAndInternalEmployees.stream()
                                                                  .filter(e -> DateUtils.parseDate(e.getReleaseDate()).isBefore(lastOfPreviousMonth))
                                                                  .toList();

        for (var employee : activeAndInternalAndNotReleasedEmployees) {
            //considering all absence types besides HomeOffice and External training days
            List<FehlzeitType> absences = zepService.getAbsenceForEmployee(employee, firstOfPreviousMonth).stream()
                    .filter(absence -> !AbsenceType.getAbsenceTypesWhereWorkingTimeNeeded().stream()
                            .map(AbsenceType::getAbsenceName).toList()
                            .contains(absence.getFehlgrund()))
                    .toList();
            boolean allAbsent = true;

            for (LocalDate day = firstOfPreviousMonth; !day.isAfter(lastOfPreviousMonth); day = day.plusDays(1)) {
                if (OfficeCalendarUtil.isWorkingDay(day)) {
                    boolean isAbsent = isAbsent(day, absences);
                    if (!isAbsent) {
                        allAbsent = false;
                        break;
                    }
                }
            }

            // only add employee who was absent the whole month
            if(allAbsent){
                absentEmployees.add(employee);
            }
        }

        // set status from OPEN to DONE for step_id 1 -> employee doesn't need to confirm times manually
        absentEmployees.forEach(employee -> {
            stepEntryService.setOpenAndAssignedStepEntriesDone(employee, 1L,  firstOfPreviousMonth, lastOfPreviousMonth);
            stepEntryService.updateStepEntryReasonForStepWithStateDone(employee, 1L, firstOfPreviousMonth, lastOfPreviousMonth, "Aufgrund von Abwesenheiten wurde der Monat automatisch bestätigt.");
            updatedEmployees.add(employeeMapper.mapToDto(zepService.getEmployee(employee.getUserId())));
        });

        logger.info("updated {} employee(s)!", updatedEmployees.size());
        return updatedEmployees;
    }

    private boolean isAbsent(LocalDate day, List<FehlzeitType> absences) {
        for(var absence : absences){
            LocalDate startDate = LocalDate.parse(absence.getStartdatum());
            LocalDate endDate = LocalDate.parse(absence.getEnddatum());
            if(day.equals(startDate) ||
                    day.equals(endDate) ||
                    (day.isAfter(startDate) && day.isBefore(endDate))) {
                return true;
            }
        }
        return false;
    }

    private void createNonExistentUsers(final List<Employee> employees, final List<User> users, final List<Project> projects) {
        final List<User> notExistentUsers = filterNotExistingEmployeesAndMapToUser(employees, users, projects);
        if (!notExistentUsers.isEmpty()) {
            notExistentUsers.forEach(userRepository::persist);
        }
        log.info("Created users: {}", notExistentUsers.size());
    }

    private void updateModifiedUsers(final List<Employee> employees, final List<User> users, final List<Project> project) {
        final List<User> modifiedUsers = filterModifiedEmployeesAndUpdateUsers(employees, users, project);
        if (!modifiedUsers.isEmpty()) {
            modifiedUsers.forEach(userRepository::update);
        }
        log.info("Updated users: {}", modifiedUsers.size());
    }

    private void deactivateDeletedOrInactiveUsers(final List<Employee> employees, final List<User> users) {
        final List<User> removedUsers = filterUserNotMappedToEmployeesAndMarkUserDeactivated(employees, users);
        if (!removedUsers.isEmpty()) {
            removedUsers.forEach(userRepository::update);
        }
        log.info("Deleted users: {}", removedUsers.size());
    }

    private List<User> filterNotExistingEmployeesAndMapToUser(final List<Employee> employees, final List<User> users, final List<Project> projects) {
        final Map<String, User> zepIdToUser = mapZepIdToUser(users);
        final Locale defaultLocale = applicationConfig.getDefaultLocale();
        return employees.stream()
                .filter(zepEmployee -> !zepIdToUser.containsKey(zepEmployee.getUserId()))
                .map(employee -> mapper.mapEmployeeToNewUser(employee, projects, defaultLocale))
                .collect(Collectors.toList());
    }

    private List<User> filterUserNotMappedToEmployeesAndMarkUserDeactivated(final List<Employee> employees, final List<User> users) {
        final Map<String, Employee> zepIdToEmployee = mapZepIdToEmployee(employees);
        return users.stream()
                .filter(user -> !zepIdToEmployee.containsKey(user.getZepId()))
                .map(mapper::mapToDeactivatedUser)
                .collect(Collectors.toList());
    }

    private List<User> filterModifiedEmployeesAndUpdateUsers(final List<Employee> employees, final List<User> users, final List<Project> projects) {
        final Map<String, User> zepIdToUser = mapZepIdToUser(users);
        final Map<User, Employee> existingUserToEmployee = employees.stream()
                .filter(zepEmployee -> zepIdToUser.containsKey(zepEmployee.getUserId()))
                .collect(Collectors.toMap(employee -> zepIdToUser.get(employee.getUserId()), Function.identity()));
        final Locale defaultLocale = applicationConfig.getDefaultLocale();
        return existingUserToEmployee.entrySet().stream()
                .map(entry -> mapper.mapEmployeeToUser(entry.getKey(), entry.getValue(), projects, defaultLocale))
                .collect(Collectors.toList());
    }

    private Map<String, User> mapZepIdToUser(final List<User> users) {
        return users.stream()
                .collect(Collectors.toMap(User::getZepId, Function.identity()));
    }

    private Map<String, Employee> mapZepIdToEmployee(final List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.toMap(Employee::getUserId, Function.identity()));
    }
}

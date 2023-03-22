package com.gepardec.mega.service.impl;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.ZepServiceException;
import com.google.common.collect.Iterables;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmployeeServiceImpl implements EmployeeService {

    private final Logger logger;

    private final ZepService zepService;

    private final ManagedExecutor managedExecutor;

    private final Integer employeeUpdateParallelExecutions;

    @Inject
    public EmployeeServiceImpl(final Logger logger,
                               final ZepService zepService,
                               final ManagedExecutor managedExecutor,
                               @ConfigProperty(name = "mega.employee.update.parallel.executions", defaultValue = "10") final Integer employeeUpdateParallelExecutions) {
        this.logger = logger;
        this.zepService = zepService;
        this.managedExecutor = managedExecutor;
        this.employeeUpdateParallelExecutions = employeeUpdateParallelExecutions;
    }

    @Override
    public Employee getEmployee(String userId) {
        return zepService.getEmployee(userId);
    }

    @Override
    public List<Employee> getAllActiveEmployees() {
        return zepService.getEmployees().stream()
                .filter(Employee::isActive)
                .filter(employee -> Objects.nonNull(employee.getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Employee> getAllEmployeesConsideringExitDate(YearMonth selectedYearMonth) {

        return zepService.getEmployees().stream()
                .filter(checkEmployeeExitDate(selectedYearMonth))
                .filter(employee -> Objects.nonNull(employee.getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public void updateEmployeeReleaseDate(String userId, String releaseDate) {
        zepService.updateEmployeesReleaseDate(userId, releaseDate);
    }

    @Override
    public List<String> updateEmployeesReleaseDate(List<Employee> employees) {
        final List<String> failedUserIds = new LinkedList<>();

        /*
        workaround until we can configure the managed executor in quarkus environment.
        at the moment, employees list is partitioned by 10 and therefore 10 requests to zep are started at a time.
         */
        Iterables.partition(Optional.ofNullable(employees).orElseThrow(() -> new ZepServiceException("no employees to update")), employeeUpdateParallelExecutions).forEach((partition) -> {
            try {
                CompletableFuture.allOf(partition.stream().map((employee) -> CompletableFuture.runAsync(() -> updateEmployeeReleaseDate(employee.getUserId(), employee.getReleaseDate()), managedExecutor)
                        .handle((aVoid, throwable) -> {
                            Optional.ofNullable(throwable).ifPresent((t) -> {
                                logger.error(String.format("error updating %s", employee.getUserId()), t);
                                failedUserIds.add(employee.getUserId());
                            });
                            return null;
                        })).toArray(CompletableFuture[]::new)).get();
            } catch (ExecutionException e) {
                logger.error("error updating employees", e);
                failedUserIds.addAll(getUserIds(partition));
            } catch (InterruptedException e) {
                logger.error("error updating employees", e);
                failedUserIds.addAll(getUserIds(partition));
                Thread.currentThread().interrupt();
            }
        });

        return failedUserIds;
    }

    private List<String> getUserIds(final List<Employee> employees) {
        return employees.stream().map(Employee::getUserId).collect(Collectors.toList());
    }

    private Predicate<? super Employee> checkEmployeeExitDate(YearMonth selectedYearMonth) {
        return employee -> {
            LocalDate exitDate = employee.getExitDate();

            if(employee.isActive() || exitDate == null || selectedYearMonth == null) {
                return true;
            }

            YearMonth exitYearMonth = YearMonth.of(exitDate.getYear(), exitDate.getMonthValue());

            // EXIT: 02/2023
            // SELECTED: 01/2023 -> TRUE
            // SELECTED: 02/2023 -> TRUE
            // SELECTED: 03/2023 -> FALSE, employee doesn't exist anymore
            return selectedYearMonth.compareTo(exitYearMonth) <= 0;
        };
    }
}

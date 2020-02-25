package com.gepardec.mega.service.impl;


import com.gepardec.mega.service.model.Employee;
import com.gepardec.mega.util.EmployeeTestUtil;
import com.gepardec.mega.zep.exception.ZepServiceException;
import com.gepardec.mega.zep.service.api.ZepService;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {

    @Mock
    private Logger logger;

    @Mock
    private ZepService zepService;

    @Mock
    private ManagedExecutor managedExecutor;

    private EmployeeServiceImpl beanUnderTest;

    @BeforeEach
    void setUp() {
        beanUnderTest = new EmployeeServiceImpl(logger, zepService, managedExecutor, 10);
    }

    @Test
    void testGetEmployee() {
        Mockito.when(zepService.getEmployee(Mockito.any())).thenReturn(EmployeeTestUtil.createEmployee(0));

        final Employee employee = beanUnderTest.getEmployee("someuserid");
        Assertions.assertNotNull(employee);
        Assertions.assertEquals("0", employee.getUserId());
        Assertions.assertEquals("Thomas_0", employee.getFirstName());
    }

    @Test
    void testGetEmployees() {
        final Employee employee0 = EmployeeTestUtil.createEmployee(0);
        final Employee employee1 = EmployeeTestUtil.createEmployee(1);
        employee1.setActive(false);

        Mockito.when(zepService.getEmployees()).thenReturn(Arrays.asList(employee0, employee1));

        final List<Employee> employees = beanUnderTest.getAllActiveEmployees();
        Assertions.assertNotNull(employees);
        Assertions.assertFalse(employees.isEmpty());
        Assertions.assertEquals(1, employees.size());
        Assertions.assertEquals("0", employees.get(0).getUserId());
        Assertions.assertEquals("Thomas_0", employees.get(0).getFirstName());
    }

    @Test
    void testUpdateEmployeesReleaseDate_EmployeesNull() {
        Assertions.assertThrows(ZepServiceException.class, () -> beanUnderTest.updateEmployeesReleaseDate(null));
    }

    @Test
    void testUpdateEmployeesReleaseDate_EmployeesEmpty() {
        Assertions.assertTrue(beanUnderTest.updateEmployeesReleaseDate(new ArrayList<>()).isEmpty());
    }

    @Test
    void testUpdateEmployeesReleaseDate_EmployeesNotEmpty_EmployeeError() {
        Mockito.doThrow(new ZepServiceException()).when(zepService).updateEmployeesReleaseDate(Mockito.any(), Mockito.any());
        Mockito.doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(managedExecutor).execute(Mockito.any());

        final List<String> result = beanUnderTest.updateEmployeesReleaseDate(EmployeeTestUtil.createEmployees(1));
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("0", result.get(0));
    }

    @Test
    void testUpdateEmployeesReleaseDate_EmployeesNotEmpty_ThreadingError() {
        AtomicInteger count = new AtomicInteger();

        Mockito.doAnswer(invocation -> {
            count.getAndIncrement();
            ((Runnable) invocation.getArgument(0)).run();
            if (count.get() == 1) {
                throw new ExecutionException(new IllegalStateException());
            } else {
                return null;
            }
        }).when(managedExecutor).execute(Mockito.any());

        final List<String> result = beanUnderTest.updateEmployeesReleaseDate(EmployeeTestUtil.createEmployees(40));
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(10, result.size());
        Assertions.assertEquals("0", result.get(0));
        Assertions.assertEquals("9", result.get(9));
    }

    @Test
    void testUpdateEmployeesReleaseDate_EmployeesNotEmpty_EmployeOk() {
        Mockito.doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(managedExecutor).execute(Mockito.any());

        final List<String> result = beanUnderTest.updateEmployeesReleaseDate(EmployeeTestUtil.createEmployees(1));
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }


}
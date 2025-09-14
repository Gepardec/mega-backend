package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.dto.ZepLanguage;
import com.gepardec.mega.zep.rest.dto.ZepSalutation;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@QuarkusTest
class EmployeeMapperTest {

    @Inject
    EmployeeMapper employeeMapper;

    @InjectMock
    UserContext userContext;

    @InjectMock
    Logger logger;

    @BeforeEach
    void setup() {
        when(userContext.getUser()).thenReturn(User.builder().personioId(123).build());
    }

    @Test
    void mapZepEmployeeToEmployee() {
        //TODO: Add regularWorkingTimes + workDescription
        ZepEmployee zepEmployee = ZepEmployee.builder()
                .username("000-duser")
                .email("demo@gepardec.com")
                .title("RE")
                .firstname("Demo")
                .lastname("User")
                .salutation(ZepSalutation.builder().name("Mr.").build())
                .releaseDate(LocalDate.of(2022, 12, 1))
                .language(ZepLanguage.builder().id("de").build())
                .build();

        Employee employee = employeeMapper.map(zepEmployee);

        assertThat(employee.getUserId()).isEqualTo(zepEmployee.username());
        assertThat(employee.getEmail()).isEqualTo(zepEmployee.email());
        assertThat(employee.getTitle()).isEqualTo(zepEmployee.title());
        assertThat(employee.getFirstname()).isEqualTo(zepEmployee.firstname());
        assertThat(employee.getLastname()).isEqualTo(zepEmployee.lastname());
        assertThat(employee.getSalutation()).isEqualTo(zepEmployee.salutation().name());
        assertThat(employee.getReleaseDate()).isEqualTo(zepEmployee.releaseDate().toString());
        assertThat(employee.getLanguage()).isEqualTo(zepEmployee.language().id());
        assertThat(employee.getPersonioId()).isEqualTo(123);
    }

    @Test
    void mapZepEmployeesToEmployees() {
        ZepEmployee[] zepEmployeesArr = {
                ZepEmployee.builder()
                        .username("000")
                        .email("blubb@blah.com")
                        .build(),
                ZepEmployee.builder()
                        .username("001")
                        .email("foo@bar.com")
                        .build(),
                ZepEmployee.builder()
                        .username("002")
                        .email("bar@foo.com")
                        .build(),
        };
        List<ZepEmployee> zepEmployees = List.of(zepEmployeesArr);

        List<Employee> employees = employeeMapper.mapList(zepEmployees);
        Iterator<ZepEmployee> zepEmployeesIterator = zepEmployees.iterator();
        employees.forEach(employee -> {
            ZepEmployee zepEmployee = zepEmployeesIterator.next();
            assertThat(employee.getUserId()).isEqualTo(zepEmployee.username());
            assertThat(employee.getEmail()).isEqualTo(zepEmployee.email());
        });
    }

    @Test
    void map_whenZepEmployeeIsNull_returnsNullAndLogsMessage() {
        assertThat(employeeMapper.map(null)).isNull();

        Mockito.verify(logger).info("ZEP REST implementation -- While trying to map ZepEmployee to Employee, ZepEmployee was null");
    }
}

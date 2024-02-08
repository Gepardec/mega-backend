package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.entity.ZepLanguage;
import com.gepardec.mega.zep.rest.entity.ZepSalutation;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class EmployeeMapperTest {

    @Inject
    Mapper<Employee, ZepEmployee> employeeMapper;

    @Test
    public void getActiveWhen_3DatesActive() {
        List<ZepEmploymentPeriod> employmentPeriods = List.of(
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2018, 2, 1, 12, 32, 12))
                        .build(),
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2022, 12, 2, 23, 17, 4))
                        .build(),
                ZepEmploymentPeriod.builder()
                        .endDate(null)
                        .build());

        boolean active = EmployeeMapper.getActiveOfZepEmploymentPeriods(employmentPeriods);
        assertThat(active).isTrue();

    }
    @Test
    public void getInactiveWhen_lastDateInactive() {
        List<ZepEmploymentPeriod> employmentPeriods = List.of(
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2018, 2, 1, 12, 32, 12))
                        .build(),
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2022, 12, 2, 23, 17, 4))
                        .build(),
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2023, 11, 12, 3, 1, 2))
                        .build());

        boolean active = EmployeeMapper.getActiveOfZepEmploymentPeriods(employmentPeriods);
        assertThat(active).isFalse();
    }
    @Test
    public void getActiveWhen_3Dates_firstDateInactive() {
        List<ZepEmploymentPeriod> employmentPeriods = List.of(
                ZepEmploymentPeriod.builder()
                        .endDate(null)
                        .build(),
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2022, 12, 2, 23, 17, 4))
                        .build(),
                ZepEmploymentPeriod.builder()
                        .endDate(LocalDateTime.of(2018, 2, 1, 12, 32, 12))
                        .build());

        boolean active = EmployeeMapper.getActiveOfZepEmploymentPeriods(employmentPeriods);
        assertThat(active).isTrue();
    }
    @Test
    public void getInactiveWhen_AllNull() {
        List<ZepEmploymentPeriod> employmentPeriods =  List.of();

        boolean active = EmployeeMapper.getActiveOfZepEmploymentPeriods(employmentPeriods);
        assertThat(active).isFalse();
    }

    @Test
    public void mapZepEmployeeToEmployee() {
//    List<ZepEmploymentPeriod> employmentPeriods = List.of(
//            ZepEmploymentPeriod.builder()
//                    .startDate(LocalDateTime.of(2010,1,2, 23, 32, 48))
//                    .endDate(LocalDateTime.of(2022, 11, 3, 12, 1, 23))
//                    .id(10)
//                    .build(),
//            ZepEmploymentPeriod.builder()
//                    .startDate(LocalDateTime.of(2022,12,22, 23, 32, 48))
//                    .endDate(LocalDateTime.of(2023, 2, 24, 8, 4, 2))
//                    .id(11)
//                    .build(),
//            ZepEmploymentPeriod.builder()
//                    .startDate(LocalDateTime.of(2024,1,2, 14, 52, 43))
//                    .endDate(null)
//                    .id(12)
//                    .build()
//    );
        boolean employeeActive = true;

            //TODO: Add regularWorkingHours + workDescription
        ZepEmployee zepEmployee = ZepEmployee.builder()
                .username("000-duser")
                .email("demo@gepardec.com")
                .title("RE")
                .firstname("Demo")
                .lastname("User")
                .salutation(ZepSalutation.builder().name("Mr.").build())
                .releaseDate(LocalDate.of(2022, 12,1))
                .language(new ZepLanguage("de", "German"))
                .build();

        Employee employee = employeeMapper.map(zepEmployee);

        assertThat(employee.getUserId()).isEqualTo(zepEmployee.getUsername());
        assertThat(employee.getEmail()).isEqualTo(zepEmployee.getEmail());
        assertThat(employee.getTitle()).isEqualTo(zepEmployee.getTitle());
        assertThat(employee.getFirstname()).isEqualTo(zepEmployee.getFirstname());
        assertThat(employee.getLastname()).isEqualTo(zepEmployee.getLastname());
        assertThat(employee.getSalutation()).isEqualTo(zepEmployee.getSalutation().getName());
        assertThat(employee.getReleaseDate()).isEqualTo(zepEmployee.getReleaseDate().toString());
        assertThat(employee.getLanguage()).isEqualTo(zepEmployee.getLanguage().getId());
    }

    @Test
    public void mapZepEmployeesToEmployees() {
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
                            assertThat(employee.getUserId()).isEqualTo(zepEmployee.getUsername());
                            assertThat(employee.getEmail()).isEqualTo(zepEmployee.getEmail());
                        });
    }
}

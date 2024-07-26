package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployeeType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@QuarkusTest
class ProjectEmployeesMapperTest {

    @Inject
    ProjectEmployeesMapper mapper;

    @Test
    void map_whenZepProjectEmployeesContainsUserAndLead_thenReturnMultivaluedMap() {
        assertThat(mapper.map(createListOfUserAndLead()).size()).isEqualTo(2);
        assertThat(mapper.map(createListOfUserAndLead()).get("user").size()).isEqualTo(2);
        assertThat(mapper.map(createListOfUserAndLead()).get("lead").size()).isOne();
    }

    @Test
    void map_whenZepProjectEmployeesContainsUser_thenReturnMultivaluedMap() {
        assertThat(mapper.map(createListOfUser()).size()).isOne();
    }

    private List<ZepProjectEmployee> createListOfUserAndLead() {
        List<ZepProjectEmployee> employeeList = new ArrayList<>();

        employeeList.add(
                ZepProjectEmployee.builder()
                        .username("001-maxMustermann")
                        .build()
        );

        employeeList.add(
                ZepProjectEmployee.builder()
                        .username("002-mariaMusterfrau")
                        .type(ZepProjectEmployeeType.builder().id(1).build())
                        .build()
        );

        return employeeList;
    }

    private List<ZepProjectEmployee> createListOfUser() {
        List<ZepProjectEmployee> employeeList = new ArrayList<>();

        employeeList.add(
                ZepProjectEmployee.builder()
                        .username("002-mariaMusterfrau")
                        .type(ZepProjectEmployeeType.builder().id(0).build())
                        .build()
        );

        return employeeList;
    }
}

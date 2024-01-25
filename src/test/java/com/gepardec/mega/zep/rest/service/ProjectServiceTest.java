package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepProject;
import com.gepardec.mega.zep.rest.entity.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.entity.ZepProjectEmployeeType;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hibernate.type.AnyType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ProjectServiceTest {
    @RestClient
    @InjectMock
    ZepProjectRestClient zepProjectRestClient;

    @Inject
    ProjectService projectService;

    @Test
    public void getSingleFullZepProject() {
        String responseJson = "{ \"data\": [{\n" +
                "      \"id\": 1,\n" +
                "      \"name\": \"MEGA\",\n" +
                "      \"description\": \"MEGA\",\n" +
                "      \"start_date\": \"2020-12-01T00:00:00.000000Z\",\n" +
                "      \"end_date\": \"2026-01-20T00:00:00.000000Z\",\n" +
                "      \"status\": \"active\",\n" +
                "      \"comments\": \"Comment MEGA\",\n" +
                "      \"cost_object\": null,\n" +
                "      \"cost_object_identifier\": null,\n" +
                "      \"created\": \"2020-12-05T08:56:09.000000Z\",\n" +
                "      \"modified\": \"2021-06-03T21:20:23.000000Z\",\n" +
                "      \"keywords\": [\n" +
                "        \"intern\"\n" +
                "      ],\n" +
                "      \"reference_order\": null,\n" +
                "      \"reference_commission\": null,\n" +
                "      \"reference_procurement\": null,\n" +
                "      \"reference_object\": null,\n" +
                "      \"language\": null,\n" +
                "      \"currency\": \"EUR\",\n" +
                "      \"url\": null,\n" +
                "      \"location_address\": null,\n" +
                "      \"location_city\": null,\n" +
                "      \"location_state\": null,\n" +
                "      \"location_country\": null,\n" +
                "      \"revenue_account\": null,\n" +
                "      \"customer_id\": \"001\",\n" +
                "      \"customer_contact_id\": 1,\n" +
                "      \"customer_project_reference\": \"B187\",\n" +
                "      \"customer_billing_address_id\": null,\n" +
                "      \"customer_shipping_address_id\": null,\n" +
                "      \"has_multiple_customers\": null,\n" +
                "      \"department_id\": 1,\n" +
                "      \"billing_type\": 1,\n" +
                "      \"billing_tasks\": 0,\n" +
                "      \"plan_hours\": \"55.5\",\n" +
                "      \"plan_hours_per_day\": null,\n" +
                "      \"plan_can_exceed\": true,\n" +
                "      \"plan_warning_percent\": \"80.00\",\n" +
                "      \"plan_warning_percent_2\": \"60.00\",\n" +
                "      \"plan_warning_percent_3\": \"40.00\",\n" +
                "      \"plan_wage\": null,\n" +
                "      \"plan_expenses\": null,\n" +
                "      \"plan_expenses_travel\": null,\n" +
                "      \"plan_hours_done\": \"100.00\",\n" +
                "      \"plan_hours_invoiced\": \"100.00\",\n" +
                "      \"tasks_count\": 1,\n" +
                "      \"employees_count\": 1,\n" +
                "      \"activities_count\": 0\n" +
                "    },\n" +
                "      {\"id\": 2}\n" +
                "],\n" +
                "  \"links\": {\n" +
                "    \"first\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects?page=1\",\n" +
                "    \"last\": null,\n" +
                "    \"prev\": null,\n" +
                "    \"next\": null" +
                "}" +
                "}";


        Response response = Response.ok().entity(responseJson).build();
        when(zepProjectRestClient.getProjectByStartEnd(Mockito.any(), Mockito.any(), Mockito.anyInt())).thenReturn(response);

        String employeeResponseJson = "{\"data\": [\n" +
                "    {\n" +
                "      \"username\": \"001-duser\",\n" +
                "      \"lead\": true,\n" +
                "      \"type\": {\n" +
                "        \"id\": \"0\",\n" +
                "        \"name\": \"Project member\"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"username\": \"007-jbond\",\n" +
                "      \"lead\": false,\n" +
                "      \"type\": {\n" +
                "        \"id\": \"0\",\n" +
                "        \"name\": \"Project member\"\n" +
                "      }\n" +
                "    }],\n" +
                "  \"links\": {\n" +
                "    \"first\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/54\\/employees?page=1\",\n" +
                "    \"last\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/54\\/employees?page=1\",\n" +
                "    \"prev\": null,\n" +
                "    \"next\": null\n" +
                "  }\n" +
                "}";
        String employeeResponseJson2 = "{\"data\": [\n" +
                "    {\n" +
                "      \"username\": \"008-hworld\"\n" +
                "    }],\n" +
                "  \"links\": {\n" +
                "    \"first\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/54\\/employees?page=1\",\n" +
                "    \"last\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/54\\/employees?page=1\",\n" +
                "    \"prev\": null,\n" +
                "    \"next\": null\n" +
                "  }\n" +
                "}";

        Response responseEmployees = Response.ok().entity(employeeResponseJson).build();
        when(zepProjectRestClient.getProjectEmployees(eq(1))).thenReturn(responseEmployees);
        Response responseEmployees2 = Response.ok().entity(employeeResponseJson2).build();
        when(zepProjectRestClient.getProjectEmployees(eq(2))).thenReturn(responseEmployees2);

        ZepProjectEmployeeType type = new ZepProjectEmployeeType(0, "Project member");
        List<ZepProjectEmployee> projectEmployees1 = List.of(
                ZepProjectEmployee.builder()
                        .username("001-duser")
                        .lead(true)
                        .type(type)
                        .build(),
                ZepProjectEmployee.builder()
                        .username("007-jbond")
                        .lead(false)
                        .type(type)
                        .build()
        );

        List<ZepProjectEmployee> projectEmployees2 = List.of(
                ZepProjectEmployee.builder()
                        .username("008-hworld")
                        .build()
        );

        ZepProject referenceZepProject = ZepProject.builder()
                .id(1)
                .name("MEGA")
                .description("MEGA")
                .startDate(LocalDateTime.of(2020, 12, 1, 0,0,0))
                .endDate(LocalDateTime.of(2026, 1, 20, 0,0,0))
                .status("active")
                .comments("Comment MEGA")
                .costObject(null)
                .costObjectIdentifier(null)
                .created(LocalDateTime.of(2020, 12, 5, 8, 56, 9))
                .modified(LocalDateTime.of(2021, 6, 3, 21, 20, 23))
                .keywords(List.of("intern"))
                .referenceOrder(null)
                .referenceObject(null)
                .referenceCommission(null)
                .referenceProcurement(null)
                .customerId("001")
                .currency("EUR")
                .customerContactId(1)
                .customerProjectReference("B187")
                .customerBillingAddressId(null)
                .customerShippingAddressId(null)
                .hasMultipleCustomers(null)
                .departmentId(1)
                .billingType(1)
                .billingTasks(0)
                .planHours("55.5")
                .planHoursPerDay(null)
                .planCanExceed(true)
                .planWarningPercent(80.0)
                .planWarningPercent2(60.0)
                .planWarningPercent3(40.0)
                .planWage(null)
                .planExpenses(null)
                .planExpensesTravel(null)
                .planHoursDone(100.00)
                .planHoursInvoiced(100.00)
                .tasksCount(1)
                .employeesCount(1)
                .activitiesCount(0)
                .employees(projectEmployees1)
                .build();

        ZepProject referenceZepProject2 = ZepProject.builder().id(2).employees(projectEmployees2).build();

        List<ZepProject> zepProject = projectService.getProjectsForMonthYear(LocalDate.of(2020, 1, 1));
        assertThat(zepProject.get(0)).usingRecursiveComparison().isEqualTo(referenceZepProject);
        assertThat(zepProject.get(1)).usingRecursiveComparison().isEqualTo(referenceZepProject2);
    }

    @Test
    public void getPaginatedJsons_thenReturnListOfProjects() {
        List<String> responseJsons = List.of(
                "{ \"data\": [{\"id\": 1\n}],\n" +
                        "\"links\": {" +
                        "   \"next\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/54\\/employees?page=2\" \n" +
                        "} \n" +
                        "}",
                "{ \"data\": [{\"id\": 2\n}],\n" +
                        "\"links\": {" +
                        "   \"next\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/54\\/employees?page=3\" \n" +
                        "} \n" +
                        "}",
                "{ \"data\": [{\"id\": 3\n}],\n" +
                        "\"links\": {" +
                        "   \"next\": null \n" +
                        "} \n" +
                        "}"
        );

        when(zepProjectRestClient.getProjectByStartEnd(Mockito.any(), Mockito.any(), eq(1)))
                .thenReturn(Response.ok().entity(responseJsons.get(0)).build());
        when(zepProjectRestClient.getProjectByStartEnd(Mockito.any(), Mockito.any(), eq(2)))
                .thenReturn(Response.ok().entity(responseJsons.get(1)).build());
        when(zepProjectRestClient.getProjectByStartEnd(Mockito.any(), Mockito.any(), eq(3)))
                .thenReturn(Response.ok().entity(responseJsons.get(2)).build());

        String employeeResponseJson =
                "{\"data\": [\n" +
                "    {\n" +
                "      \"username\": \"001-duser\"\n" +
                "    }],\n" +
                "  \"links\": {\n" +
                "    \"next\": null\n" +
                "  }\n" +
                "}";

        IntStream.range(1, 4).forEach(
                i -> when(zepProjectRestClient.getProjectEmployees(eq(i)))
                    .thenReturn(Response.ok().entity(employeeResponseJson).build()));

        List<ZepProjectEmployee> zepProjectEmployees = List.of(ZepProjectEmployee.builder().username("001-duser").build());
        List<ZepProject> zepProjectsReference = List.of(
                ZepProject.builder().id(1).employees(zepProjectEmployees).build(),
                ZepProject.builder().id(2).employees(zepProjectEmployees).build(),
                ZepProject.builder().id(3).employees(zepProjectEmployees).build()
        );

        List<ZepProject> zepProjects = projectService.getProjectsForMonthYear(LocalDate.of(2024,1,25));
        assertThat(zepProjects).usingRecursiveComparison().isEqualTo(zepProjectsReference);



    }
}

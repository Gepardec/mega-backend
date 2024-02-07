package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.entity.*;
import com.gepardec.mega.zep.rest.entity.builder.ZepProjectEmployeeTypeBuilder;
import com.gepardec.mega.zep.util.Paginator;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ProjectServiceTest {

    @InjectMock
    @RestClient
    ZepProjectRestClient zepProjectRestClient;

    @Inject
    ProjectService projectService;

    @BeforeEach
    public void setup() {
        this.getPaginatedProjectsMock();
    }

    private void getPaginatedProjectsMock() {
        List<String> responseJsons = getPages("/zep/rest/testresponses/projectPaginated");
        System.out.println(responseJsons);

        when(zepProjectRestClient.getProjects( eq(1)))
                .thenReturn(Response.ok().entity(responseJsons.get(0)).build());
        when(zepProjectRestClient.getProjects(eq(2)))
                .thenReturn(Response.ok().entity(responseJsons.get(1)).build());
        when(zepProjectRestClient.getProjects(eq(3)))
                .thenReturn(Response.ok().entity(responseJsons.get(2)).build());
    }

    @Test
    public void getSingleFullZepProject() {

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
                .build();


        List<ZepProject> zepProject = projectService.getProjectsForMonthYear(LocalDate.of(2024, 1, 1));
        assertThat(zepProject.get(0)).usingRecursiveComparison().isEqualTo(referenceZepProject);
    }

    @Test
    public void getPaginatedJsons_thenReturnListOfProjects() {
        String[] names = {"MEGA", "gema", "EGA", "SUPERMEGA", "mega", "ega"};


        List<ZepProject> projectList = Paginator.retrieveAll(page -> zepProjectRestClient.getProjects(page), ZepProject.class);
        List<String> projectNames = projectList.stream()
                .map(ZepProject::getName)
                .toList();
        Arrays.stream(names)
                .forEach(name -> assertThat(projectNames.contains(name)).isTrue());
    }

    @Test
    public void getProjectByName() {
        Optional<ZepProject> project = projectService.getProjectByName("mega", LocalDate.of(2022, 1, 2));
        assertThat(project.get().getId()).isEqualTo(5);
    }
    @Test
    public void getProjectByName_whenNoProjectOfName() {
        Optional<ZepProject> project = projectService.getProjectByName("Coffee robot for office",
                LocalDate.of(2022, 1, 2));
        assertThat(project.isEmpty()).isTrue();

    }

    public static List<String> getPages(String resourcesPath) {
        File folder = getFileOfResourcesPath(resourcesPath);

        if (!folder.isDirectory()) {
            throw new RuntimeException("No directory found at " + folder.getPath());
        }

        File[] files = Objects.requireNonNull(folder.listFiles());
        return Arrays.stream(files)
                .map(ProjectServiceTest::readFile)
                .collect(Collectors.toList());

    }

    public static Optional<String> getSingleFile(String resourcesPath) {
        File file = getFileOfResourcesPath(resourcesPath);
        if (!file.isFile()) {
            return Optional.empty();
        }
        return Optional.of(readFile(file));
    }

    private static String readFile(File f) {
        try {
            return FileUtils.readFileToString(f, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getFileOfResourcesPath(String resourcesPath) {
        String path = ProjectServiceTest.class.getResource(resourcesPath).getPath();
        return new File(path);
    }


}

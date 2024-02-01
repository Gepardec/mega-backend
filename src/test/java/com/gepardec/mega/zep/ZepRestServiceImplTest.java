package com.gepardec.mega.zep;

import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.rest.ZepRestService;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.service.RegularWorkingTimesService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

@QuarkusTest
public class ZepRestServiceImplTest {
    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Inject
    ZepRestService zepRestService;

//    @Inject
//    ZepEmployeeRestClient zepEmployeeRestClient;

    @Test
    public void test() {

        regularWorkingTimesService.getRegularWorkingTimesByUsername("001-hwirnsberger");
    }

    @Test
    public void integrationTest_getProjectTimesForEmployeePerProject(){
        List<ProjectTime> projectTimes = zepRestService.getProjectTimesForEmployeePerProject("ITSV-VAEB-2018", LocalDate.of(2018, 12, 12));
        for (ProjectTime projectTime : projectTimes) {
            System.out.println(projectTime.getProjectNr());
        }
        System.out.println(projectTimes.size());
    }
}

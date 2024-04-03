package com.gepardec.mega.zep;

import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.impl.Rest;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.service.RegularWorkingTimesService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

@QuarkusTest
public class ZepRestServiceImplTest {
    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Inject @Rest
    ZepService zepRestService;

//    @Inject
//    ZepEmployeeRestClient zepEmployeeRestClient;

    @Test
    @Disabled("To be deleted")
    public void ſ() {

        regularWorkingTimesService.getRegularWorkingTimesByUsername("001-hwirnsberger");
    }

    @Test
    @Disabled("Local test")
    public void integrationTest_getProjectTimesForEmployeePerProject(){
        List<ProjectTime> projectTimes = zepRestService.getProjectTimesForEmployeePerProject("ITSV-VAEB-2018", LocalDate.of(2018, 12, 12));
        for (ProjectTime projectTime : projectTimes) {
            System.out.println(projectTime.getUserId() + ": " + projectTime.getDuration() + " " + projectTime.getBillable());
        }
        System.out.println(projectTimes.size());
    }
}

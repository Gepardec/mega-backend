package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import com.gepardec.mega.zep.util.Paginator;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class EmploymentPeriodService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestService;

    public List<ZepEmploymentPeriod> getZepEmploymentPeriodsByEmployeeName(String employeeName) {
        return Paginator.retrieveAll(
                page -> zepEmployeeRestService.getEmploymentPeriodByUserName(employeeName, page),
                ZepEmploymentPeriod.class
        );
    }

}

package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepAttendanceRestClient;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import com.gepardec.mega.zep.util.Paginator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class AttendanceService {

    @RestClient
    ZepAttendanceRestClient zepAttendanceRestClient;

    @Inject
    Paginator paginator;

    public List<ZepAttendance> getAttendance(String username, LocalDate date) {
        String startDate = date.withDayOfMonth(1).toString();
        String endDate = date.withDayOfMonth(date.lengthOfMonth()).toString();
        List<ZepAttendance> list = paginator.retrieveAll(zepAttendanceRestClient::getAttendance, startDate, endDate, username, 1, ZepAttendance.class);
        return list;
//        zepAttendanceRestClient.getAttendance(startDate, endDate, username);
    }
}


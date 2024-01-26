package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepAttendanceRestClient;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.util.Paginator;
import com.gepardec.mega.zep.util.ZepRestUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AttendanceService {
    final int BILLABLE_TYPE_BORDER = 2;


    @RestClient
    private ZepAttendanceRestClient zepAttendanceRestClient;

    public List<ZepAttendance> getBillableAttendancesForUser(String username) {
        List<ZepAttendance> attendances = Paginator.retrieveAll(
                (page) -> zepAttendanceRestClient.getAttendancesByUsername(username, page),
                ZepAttendance.class);
        return attendances.stream()
                .filter(attendance -> attendance.getBillable() <= BILLABLE_TYPE_BORDER)
                .collect(Collectors.toList());

    }


    public List<ZepAttendance> getAttendance(String username, LocalDate date) {
        String startDate = date.withDayOfMonth(1).toString();
        String endDate = date.withDayOfMonth(date.lengthOfMonth()).toString();
        return Paginator.retrieveAll(
            page -> zepAttendanceRestClient.getAttendance(startDate, endDate, username, page),
            ZepAttendance.class);
    }
}


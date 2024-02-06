package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepAttendanceRestClient;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import com.gepardec.mega.zep.util.Paginator;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AttendanceService {
    final int BILLABLE_TYPE_BORDER = 2;
    @RestClient
    private ZepAttendanceRestClient zepAttendanceRestClient;

    public List<ZepAttendance> getBillableAttendancesForUserAndMonth(String username, LocalDate date) {
        List<ZepAttendance> attendances = this.getAttendanceForUserAndMonth(username, date);
        return attendances.stream()
                .filter(attendance -> attendance.getBillable() <= BILLABLE_TYPE_BORDER)
                .collect(Collectors.toList());

    }
    //Return the attendances for a user for a given month. The month in which the date is located determines the month to be queried.
    public List<ZepAttendance> getAttendanceForUserAndMonth(String username, LocalDate date) {
        String startDate = date.withDayOfMonth(1).toString();
        String endDate = date.withDayOfMonth(date.lengthOfMonth()).toString();
        return Paginator.retrieveAll(
            page -> zepAttendanceRestClient.getAttendance(startDate, endDate, username, page),
            ZepAttendance.class);

    }
    public List<ZepAttendance> getAttendanceForUserProjectAndMonth(String username, LocalDate date, Integer projectId) {
        String startDate = date.withDayOfMonth(1).toString();
        String endDate = date.withDayOfMonth(date.lengthOfMonth()).toString();

        List<ZepAttendance> attendances = Paginator.retrieveAll(
                page -> zepAttendanceRestClient.getAttendance(startDate, endDate, username, page),
                ZepAttendance.class);
        return attendances.stream()
                .filter(attendance -> attendance.getProjectId() == projectId)
                .collect(Collectors.toList());
    }
}


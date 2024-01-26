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
        List<ZepAttendance> attendances = this.getPaginatedAttendances(username);
        return attendances.stream()
                .filter(attendance -> attendance.getBillable() <= BILLABLE_TYPE_BORDER)
                .collect(Collectors.toList());

    }

    private List<ZepAttendance> getPaginatedAttendances(String name) {
        List<ZepAttendance> attendances = new ArrayList<>();
        int page = 1;
        fillWithAttendances(attendances, name, page);
        return attendances;
    }

    void fillWithAttendances(List<ZepAttendance> attendances, String username, int page) {
        String next = "";
        ZepAttendance[] attendancesArr = {};
        try (Response resp = zepAttendanceRestClient.getAttendancesByUsername(username, page)) {
            String output = resp.readEntity(String.class);
            attendancesArr = (ZepAttendance[]) ZepRestUtil.parseJson(output, "/data", ZepAttendance[].class);
            next = (String) ZepRestUtil.parseJson(output, "/links/next", String.class);
        }

        attendances.addAll(List.of(attendancesArr));
        if (next != null) {
            fillWithAttendances(attendances, username, page + 1);
        }
    }

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


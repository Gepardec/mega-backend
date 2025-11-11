package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepAttendanceRestClient;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.util.ResponseParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
public class AttendanceService {
    @RestClient
    private ZepAttendanceRestClient zepAttendanceRestClient;

    @Inject
    Logger logger;

    @Inject
    ResponseParser responseParser;

    public List<ZepAttendance> getBillableAttendancesForUserAndMonth(String username, YearMonth payrollMonth) {
        try {
            List<ZepAttendance> attendances = this.getAttendanceForUserAndMonth(username, payrollMonth);
            return attendances.stream()
                    .filter(ZepAttendance::billable)
                    .toList();
        } catch (ZepServiceException e) {
            String message = "Error retrieving billable attendances for user \"%s\" from ZEP: No /data field in response".formatted(username);
            logger.warn(message, e);
        }
        return List.of();

    }

    //Return the attendances for a user for a given month. The month in which the date is located determines the month to be queried.
    public List<ZepAttendance> getAttendanceForUserAndMonth(String username, YearMonth payrollMonth) {
        String startDate = payrollMonth.atDay(1).toString();
        String endDate = payrollMonth.atEndOfMonth().toString();

        try {
            return responseParser.retrieveAll(
                    page -> zepAttendanceRestClient.getAttendance(startDate, endDate, username, page),
                    ZepAttendance.class);
        } catch (ZepServiceException e) {
            String message = "Error retrieving attendances for user \"%s\" from ZEP: No /data field in response".formatted(username);
            logger.warn(message, e);
        }
        return List.of();
    }

    public List<ZepAttendance> getAttendanceForUserProjectAndMonth(String username, YearMonth payrollMonth, Integer projectId) {
        String startDate = payrollMonth.atDay(1).toString();
        String endDate = payrollMonth.atEndOfMonth().toString();

        try {
            return responseParser.retrieveAll(
                    page -> zepAttendanceRestClient.getAttendanceForUserAndProject(startDate, endDate, username, projectId, page),
                    ZepAttendance.class);

        } catch (ZepServiceException e) {
            String message = """
                    Error retrieving billable attendances for user "%s" and project "%d" from ZEP: \
                    No /data field in response"""
                    .formatted(username, projectId);
            logger.warn(message, e);
        }
        return List.of();
    }
}


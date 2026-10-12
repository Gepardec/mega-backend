package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeBookingZepPort;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.zep.rest.service.AttendanceService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
public class WorkTimeBookingZepAdapter implements WorkTimeBookingZepPort {
    private final AttendanceService attendanceService;
    private final WorkTimeBookingZepMapper mapper;

    @Inject
    public WorkTimeBookingZepAdapter(AttendanceService attendanceService, WorkTimeBookingZepMapper mapper) {
        this.attendanceService = attendanceService;
        this.mapper = mapper;
    }

    @Override
    public List<WorkTimeBooking> fetchBookingsForEmployee(String zepUsername, YearMonth month) {
        return attendanceService.getAttendanceForUserAndMonth(zepUsername, month).stream()
                .map(mapper::toDomain)
                .toList();
    }
}

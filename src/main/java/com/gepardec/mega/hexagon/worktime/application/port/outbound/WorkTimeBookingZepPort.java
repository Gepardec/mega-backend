package com.gepardec.mega.hexagon.worktime.application.port.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;

import java.time.YearMonth;
import java.util.List;

public interface WorkTimeBookingZepPort {
    List<WorkTimeBooking> fetchBookingsForEmployee(String zepUsername, YearMonth month);
}

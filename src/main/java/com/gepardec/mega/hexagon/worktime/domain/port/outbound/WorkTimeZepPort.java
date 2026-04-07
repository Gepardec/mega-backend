package com.gepardec.mega.hexagon.worktime.domain.port.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import io.smallrye.mutiny.Uni;

import java.time.YearMonth;
import java.util.List;

public interface WorkTimeZepPort {

    Uni<List<WorkTimeAttendance>> fetchAttendancesForEmployee(String zepEmployeeId, YearMonth month);

    Uni<List<String>> fetchProjectMembershipForMonth(Integer zepProjectId, YearMonth month);
}

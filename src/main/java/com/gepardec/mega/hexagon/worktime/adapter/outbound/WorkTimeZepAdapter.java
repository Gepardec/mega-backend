package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeZepPort;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.service.AttendanceService;
import com.gepardec.mega.zep.rest.service.ProjectService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class WorkTimeZepAdapter implements WorkTimeZepPort {

    private final AttendanceService attendanceService;
    private final ProjectService projectService;

    @Inject
    public WorkTimeZepAdapter(AttendanceService attendanceService, ProjectService projectService) {
        this.attendanceService = attendanceService;
        this.projectService = projectService;
    }

    @Override
    public Uni<List<WorkTimeAttendance>> fetchAttendancesForEmployee(String zepEmployeeId, YearMonth month) {
        return attendanceService.getAttendanceForUserAndMonthAsync(zepEmployeeId, month)
                .map(attendances -> attendances.stream()
                        .filter(attendance -> attendance.employeeId() != null)
                        .filter(attendance -> attendance.projectId() != null)
                        .map(this::toWorkTimeAttendance)
                        .toList());
    }

    @Override
    public Uni<List<String>> fetchProjectMembershipForMonth(Integer zepProjectId, YearMonth month) {
        return projectService.getProjectEmployeesForIdAsync(zepProjectId, month)
                .map(projectEmployees -> projectEmployees.stream()
                        .map(ZepProjectEmployee::username)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList());
    }

    private WorkTimeAttendance toWorkTimeAttendance(ZepAttendance attendance) {
        double duration = attendance.duration() == null ? 0.0d : attendance.duration();
        return Boolean.TRUE.equals(attendance.billable())
                ? new WorkTimeAttendance(attendance.employeeId(), attendance.projectId(), duration, 0.0d)
                : new WorkTimeAttendance(attendance.employeeId(), attendance.projectId(), 0.0d, duration);
    }
}

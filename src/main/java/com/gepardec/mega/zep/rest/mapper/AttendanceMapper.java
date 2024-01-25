package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.BillabilityPreset;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.mapper.MapperUtil;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AttendanceMapper implements Mapper<ProjectTime, ZepAttendance> {

    @Override
    public ProjectTime map(ZepAttendance zepAttendance) {
        if (zepAttendance == null) {
            return null;
        }

        boolean isBillable = zepAttendance.getBillable() <= BillabilityPreset.BILLABLE_FIXED.getZepId();
        Boolean locationPrRelevant = null;
        if (zepAttendance.getWorkLocationIsProjectRelevant() != null) {
            locationPrRelevant = zepAttendance.getWorkLocationIsProjectRelevant() != -1;
        }

        String id = zepAttendance.getId() == null? null : "" + zepAttendance.getId();
        String startTime = zepAttendance.getFrom() == null ? null : "" + zepAttendance.getFrom();
        String endTime = zepAttendance.getTo() == null ? null : "" + zepAttendance.getTo();
        String duration = zepAttendance.getDuration() == null ? null : "" + zepAttendance.getDuration();
        String projectNr = zepAttendance.getProjectId() == null ? null : "" + zepAttendance.getProjectId();
        String task = zepAttendance.getProjectTaskId() == null ? null : "" + zepAttendance.getProjectTaskId();
        Integer km = zepAttendance.getKm() == null ? null : Integer.parseInt(zepAttendance.getKm());
        Integer amountPassengers = zepAttendance.getPassengers() == null ?
                null : Integer.parseInt(zepAttendance.getPassengers());
        Integer ticketNr = zepAttendance.getTicketId() == null ? null : Integer.parseInt(zepAttendance.getTicketId());
        String created = zepAttendance.getCreated() == null ? null : "" + zepAttendance.getCreated();
        String modified = zepAttendance.getModified() == null ? null : "" + zepAttendance.getModified();

        return ProjectTime.builder()
                .id(id)
                .userId(zepAttendance.getEmployeeId())
                .date(zepAttendance.getDate())
                .startTime(startTime)
                .endTime(endTime)
                .duration(duration)
                .isBillable(isBillable)
                .isLocationRelevantToProject(locationPrRelevant)
                .location(zepAttendance.getWorkLocation())
                .comment(zepAttendance.getNote())
                .projectNr(projectNr)
                .processNr(zepAttendance.getTicketId())
                .task(task)
                .startLocation(zepAttendance.getStart())
                .endLocation(zepAttendance.getDestination())
                .km(km)
                .amountPassengers(amountPassengers)
                .vehicle(zepAttendance.getVehicle())
                .ticketNr(ticketNr)
                .subtaskNr(zepAttendance.getSubtaskId())
                .travelDirection(zepAttendance.getDirectionOfTravel())
                .isPrivateVehicle(zepAttendance.isPrivate())
                .created(created)
                .modified(modified)
                .build();
    }
}

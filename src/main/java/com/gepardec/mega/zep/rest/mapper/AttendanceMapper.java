package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.BillabilityPreset;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.mapper.MapperUtil;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class AttendanceMapper implements Mapper<ProjectTime, ZepAttendance> {

    @Inject
    Logger logger;

    @Override
    public ProjectTime map(ZepAttendance zepAttendance) {
        if (zepAttendance == null) {
            logger.info("ZEP REST implementation -- While trying to map ZepAttendance to ProjectTime, ZepAttendance was null");
            return null;
        }

        try {

            Boolean isBillable = zepAttendance.billable() <= BillabilityPreset.BILLABLE_FIXED.getZepId();
            Boolean locationPrRelevant = null;
            if (zepAttendance.workLocationIsProjectRelevant() != null) {
                locationPrRelevant = zepAttendance.workLocationIsProjectRelevant() != -1;
            }

            String duration = null;
            if (zepAttendance.duration() != null ) {
                BigDecimal bigDecimal = BigDecimal.valueOf(zepAttendance.duration());
                int hours = bigDecimal.intValue();
                int minutes = bigDecimal
                        .subtract(BigDecimal.valueOf(hours))
                        .multiply(BigDecimal.valueOf(60)).intValue();
                duration = "" + LocalTime.of(hours, minutes);
            }


            String id = zepAttendance.id() == null? null : "" + zepAttendance.id();
            String startTime = zepAttendance.from() == null ? null : "" + zepAttendance.from();
            String endTime = zepAttendance.to() == null ? null : "" + zepAttendance.to();
            String projectNr = zepAttendance.projectId() == null ? null : "" + zepAttendance.projectId();
            String task = zepAttendance.projectTaskId() == null ? null : "" + zepAttendance.projectTaskId();
            Integer km = zepAttendance.km() == null ? null : Integer.parseInt(zepAttendance.km());
            Integer amountPassengers = zepAttendance.passengers() == null ?
                    null : Integer.parseInt(zepAttendance.passengers());
            Integer ticketNr = zepAttendance.ticketId() == null ? null : Integer.parseInt(zepAttendance.ticketId());
            String created = zepAttendance.created() == null ? null : "" + zepAttendance.created();
            String modified = zepAttendance.modified() == null ? null : "" + zepAttendance.modified();
            boolean privateVehicle = zepAttendance.isPrivate() != null && zepAttendance.isPrivate() == 1;

            return ProjectTime.builder()
                    .id(id)
                    .userId(zepAttendance.employeeId())
                    .date(zepAttendance.date())
                    .startTime(startTime)
                    .endTime(endTime)
                    .duration(duration)
                    .isBillable(isBillable)
                    .isLocationRelevantToProject(locationPrRelevant)
                    .location(zepAttendance.workLocation())
                    .comment(zepAttendance.note())
                    .projectNr(projectNr)
                    .processNr(zepAttendance.ticketId())
                    .task(task)
                    .startLocation(zepAttendance.start())
                    .endLocation(zepAttendance.destination())
                    .km(km)
                    .amountPassengers(amountPassengers)
                    .vehicle(zepAttendance.vehicle())
                    .ticketNr(ticketNr)
                    .subtaskNr(zepAttendance.subtaskId())
                    .travelDirection(zepAttendance.directionOfTravel())
                    .isPrivateVehicle(privateVehicle)
                    .created(created)
                    .modified(modified)
                    .build();

        }catch (Exception e){
            throw new ZepServiceException("Error while mapping ZepAttendance to ProjectTime", e);
        }
    }
}

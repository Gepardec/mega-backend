package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.Vehicle;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepAttendanceDirectionOfTravel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WorkTimeBookingZepMapper {
    default WorkTimeBooking toDomain(ZepAttendance attendance) {
        try {
            Task task = toTask(attendance.activity());
            return Task.isJourney(task)
                    ? toJourneyBooking(attendance, task)
                    : toProjectBooking(attendance, task);
        } catch (RuntimeException exception) {
            throw new ZepServiceException("While mapping ZepAttendance to WorkTimeBooking", exception);
        }
    }

    @Mapping(target = "from", source = "attendance", qualifiedByName = "toFrom")
    @Mapping(target = "to", source = "attendance", qualifiedByName = "toTo")
    @Mapping(target = "task", source = "task")
    @Mapping(target = "workingLocation", source = "attendance.workLocation", qualifiedByName = "toWorkingLocation")
    @Mapping(target = "workLocationProjectRelevant", source = "attendance.workLocationIsProjectRelevant")
    @Mapping(target = "process", source = "attendance.projectTaskId")
    ProjectBooking toProjectBooking(ZepAttendance attendance, Task task);

    @Mapping(target = "from", source = "attendance", qualifiedByName = "toFrom")
    @Mapping(target = "to", source = "attendance", qualifiedByName = "toTo")
    @Mapping(target = "task", source = "task")
    @Mapping(target = "workingLocation", source = "attendance.workLocation", qualifiedByName = "toWorkingLocation")
    @Mapping(target = "workLocationProjectRelevant", source = "attendance.workLocationIsProjectRelevant")
    @Mapping(target = "direction", source = "attendance.directionOfTravel", qualifiedByName = "toJourneyDirection")
    @Mapping(target = "vehicle", source = "attendance.vehicle", qualifiedByName = "toVehicle")
    JourneyBooking toJourneyBooking(ZepAttendance attendance, Task task);

    default Task toTask(String activity) {
        return Task.fromString(activity)
                .orElseThrow(() -> new IllegalArgumentException("Activity '" + activity + "' is not mapped"));
    }

    @Named("toFrom")
    default LocalDateTime toFrom(ZepAttendance attendance) {
        return LocalDateTime.of(attendance.date(), attendance.from());
    }

    @Named("toTo")
    default LocalDateTime toTo(ZepAttendance attendance) {
        return LocalDateTime.of(attendance.date(), attendance.to());
    }

    @Named("toWorkingLocation")
    default WorkingLocation toWorkingLocation(String workLocation) {
        return WorkingLocation.fromZepOrt(
                workLocation == null ? WorkingLocation.MAIN.getZepOrt() : workLocation);
    }

    @Named("toJourneyDirection")
    default JourneyDirection toJourneyDirection(ZepAttendanceDirectionOfTravel direction) {
        if (direction == null) {
            return JourneyDirection.TO;
        }
        return JourneyDirection.fromString(direction.id())
                .orElseThrow(() -> new IllegalArgumentException("Journey direction is not mapped"));
    }

    @Named("toVehicle")
    default Vehicle toVehicle(String vehicle) {
        return Vehicle.forId(vehicle)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle '" + vehicle + "' is not mapped"));
    }
}

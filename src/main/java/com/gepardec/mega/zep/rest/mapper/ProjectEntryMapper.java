package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.monthlyreport.*;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepAttendanceDirectionOfTravel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectEntryMapper implements Mapper<com.gepardec.mega.domain.model.monthlyreport.ProjectEntry, ZepAttendance> {

    @Inject
    Logger logger;

    @Override
    public com.gepardec.mega.domain.model.monthlyreport.ProjectEntry map(ZepAttendance zepAttendance) {
        if (zepAttendance == null) {
            logger.info("ZEP REST implementation -- While trying to map zepAttendance to ProjectEntry, zepAttendance was null");
            return null;
        }

        try{

            Task task = toTask(zepAttendance.activity());
            LocalDateTime from = LocalDateTime.of(zepAttendance.date(), zepAttendance.from());
            LocalDateTime to = LocalDateTime.of(zepAttendance.date(), zepAttendance.to());
            WorkingLocation workingLocation = toWorkingLocation(zepAttendance.workLocation());
            String process = Integer.toString(zepAttendance.projectTaskId());

            if (Task.isJourney(task)) {
                JourneyDirection journeyDirection = toJourneyDirection(zepAttendance.directionOfTravel());
                Vehicle vehicle = toVehicle(zepAttendance.vehicle());
                return JourneyTimeEntry.builder()
                        .fromTime(from)
                        .toTime(to)
                        .task(task)
                        .journeyDirection(journeyDirection)
                        .workingLocation(workingLocation)
                        .vehicle(vehicle)
                        .build();
            } else {
                return ProjectTimeEntry.builder()
                        .fromTime(from)
                        .toTime(to)
                        .task(task)
                        .workingLocation(workingLocation)
                        .process(process)
                        .build();
            }
        }catch (Exception e){
            throw new ZepServiceException("While trying to map ZepAttendance to ProjectEntry, an error occurred", e);
        }

    
    }

    @Override
    public List<com.gepardec.mega.domain.model.monthlyreport.ProjectEntry> mapList(List<ZepAttendance> zepAttendances) {
        if (zepAttendances == null) {
            return null;
        }
        return zepAttendances.stream()
                .map(this::map)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ProjectEntry::getFromTime))
                .toList();
    }

    private Task toTask(String activity) {
        return Task.fromString(activity)
                .orElseThrow(() -> new IllegalArgumentException("Tätigkeit '" + activity + "' is not mapped in Enum Task"));
    }

    private WorkingLocation toWorkingLocation(final String ort) {
        return WorkingLocation.fromZepOrt(ort == null ? WorkingLocation.MAIN.getZepOrt() : ort);
    }

    private Vehicle toVehicle(final String vehicle) {
        return Vehicle.forId(vehicle)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle '" + vehicle + "' is not mapped in Enum Vehicle"));
    }

    private JourneyDirection toJourneyDirection(final ZepAttendanceDirectionOfTravel direction) {
        if(direction == null) {
            return JourneyDirection.TO;
        }
        return JourneyDirection.fromString(direction.id())
                .orElseThrow(() -> new IllegalArgumentException("JourneyDirection '" + direction + "' is not mapped in Enum JourneyDirection"));
    }

}

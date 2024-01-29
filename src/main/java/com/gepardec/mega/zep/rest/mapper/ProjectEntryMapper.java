package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.domain.model.monthlyreport.*;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectEntryMapper implements Mapper<com.gepardec.mega.domain.model.monthlyreport.ProjectEntry, ZepAttendance> {
    @Override
    public com.gepardec.mega.domain.model.monthlyreport.ProjectEntry map(ZepAttendance zepAttendance) {
        if (zepAttendance == null) {
            return null;
        }
        Task task = toTask(zepAttendance.getActivity());
        LocalDateTime from = LocalDateTime.of(zepAttendance.getDate(), zepAttendance.getFrom());
        LocalDateTime to = LocalDateTime.of(zepAttendance.getDate(), zepAttendance.getTo());
        WorkingLocation workingLocation = toWorkingLocation(zepAttendance.getWorkLocation());
        String process = Integer.toString(zepAttendance.getProjectTaskId());

        if (Task.isJourney(task)) {
            JourneyDirection journeyDirection = toJourneyDirection(zepAttendance.getDirectionOfTravel());
            Vehicle vehicle = toVehicle(zepAttendance.getVehicle());
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
                .collect(Collectors.toList());
    }

    private Task toTask(String activity) {
        return Task.fromString(activity)
                .orElseThrow(() -> new IllegalArgumentException("Tätigkeit '" + activity + "' is not mapped in Enum Task"));
    }

    private WorkingLocation toWorkingLocation(final String ort) {
        return WorkingLocation.fromZepOrt(ort);
    }

    private Vehicle toVehicle(final String vehicle) {
        return Vehicle.forId(vehicle)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle '" + vehicle + "' is not mapped in Enum Vehicle"));
    }

    private JourneyDirection toJourneyDirection(final String direction) {
        return JourneyDirection.fromString(direction)
                .orElseThrow(() -> new IllegalArgumentException("JourneyDirection '" + direction + "' is not mapped in Enum JourneyDirection"));
    }

}

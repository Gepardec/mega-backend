package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.Vehicle;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import org.instancio.Instancio;

import java.time.LocalDateTime;
import java.util.List;

import static org.instancio.Select.field;

final class WarningTestBookingBuilder {
    private WarningTestBookingBuilder() {
    }

    static ProjectBuilder projectBookingBuilder() {
        return new ProjectBuilder();
    }

    static JourneyBuilder journeyBookingBuilder() {
        return new JourneyBuilder();
    }

    static WorkTimeBookings bookings(WorkTimeBooking... bookings) {
        return new WorkTimeBookings(List.of(bookings));
    }

    static final class ProjectBuilder {
        private LocalDateTime from;
        private LocalDateTime to;
        private Task task;
        private WorkingLocation workingLocation;
        private boolean projectRelevant;
        private String process;
        private boolean billable = true;
        private String note = "Test";
        private Integer projectZepId = 1;

        ProjectBuilder fromTime(LocalDateTime value) {
            from = value;
            return this;
        }

        ProjectBuilder toTime(LocalDateTime value) {
            to = value;
            return this;
        }

        ProjectBuilder task(Task value) {
            task = value;
            return this;
        }

        ProjectBuilder workingLocation(WorkingLocation value) {
            workingLocation = value;
            return this;
        }

        ProjectBuilder workLocationIsProjectRelevant(Boolean value) {
            projectRelevant = Boolean.TRUE.equals(value);
            return this;
        }

        ProjectBuilder process(String value) {
            process = value;
            return this;
        }

        ProjectBuilder billable(boolean value) {
            billable = value;
            return this;
        }

        ProjectBuilder note(String value) {
            note = value;
            return this;
        }

        ProjectBuilder projectZepId(Integer value) {
            projectZepId = value;
            return this;
        }

        ProjectBooking build() {
            return Instancio.of(ProjectBooking.class)
                    .set(field(ProjectBooking::from), from)
                    .set(field(ProjectBooking::to), to)
                    .set(field(ProjectBooking::task), task)
                    .set(field(ProjectBooking::workingLocation), workingLocation)
                    .set(field(ProjectBooking::workLocationProjectRelevant), projectRelevant)
                    .set(field(ProjectBooking::process), process)
                    .set(field(ProjectBooking::billable), billable)
                    .set(field(ProjectBooking::note), note)
                    .set(field(ProjectBooking::projectZepId), projectZepId)
                    .create();
        }
    }

    static final class JourneyBuilder {
        private LocalDateTime from;
        private LocalDateTime to;
        private Task task;
        private WorkingLocation workingLocation;
        private boolean projectRelevant;
        private JourneyDirection direction;
        private Vehicle vehicle;
        private String start;
        private String destination;
        private boolean billable = true;
        private String note = "Test";
        private Integer projectZepId = 1;

        JourneyBuilder fromTime(LocalDateTime value) {
            from = value;
            return this;
        }

        JourneyBuilder toTime(LocalDateTime value) {
            to = value;
            return this;
        }

        JourneyBuilder task(Task value) {
            task = value;
            return this;
        }

        JourneyBuilder workingLocation(WorkingLocation value) {
            workingLocation = value;
            return this;
        }

        JourneyBuilder workLocationIsProjectRelevant(Boolean value) {
            projectRelevant = Boolean.TRUE.equals(value);
            return this;
        }

        JourneyBuilder journeyDirection(JourneyDirection value) {
            direction = value;
            return this;
        }

        JourneyBuilder vehicle(Vehicle value) {
            vehicle = value;
            return this;
        }

        JourneyBuilder start(String value) {
            start = value;
            return this;
        }

        JourneyBuilder destination(String value) {
            destination = value;
            return this;
        }

        JourneyBuilder billable(boolean value) {
            billable = value;
            return this;
        }

        JourneyBuilder note(String value) {
            note = value;
            return this;
        }

        JourneyBuilder projectZepId(Integer value) {
            projectZepId = value;
            return this;
        }

        JourneyBooking build() {
            return Instancio.of(JourneyBooking.class)
                    .set(field(JourneyBooking::from), from)
                    .set(field(JourneyBooking::to), to)
                    .set(field(JourneyBooking::task), task)
                    .set(field(JourneyBooking::workingLocation), workingLocation)
                    .set(field(JourneyBooking::workLocationProjectRelevant), projectRelevant)
                    .set(field(JourneyBooking::direction), direction)
                    .set(field(JourneyBooking::vehicle), vehicle)
                    .set(field(JourneyBooking::start), start)
                    .set(field(JourneyBooking::destination), destination)
                    .set(field(JourneyBooking::billable), billable)
                    .set(field(JourneyBooking::note), note)
                    .set(field(JourneyBooking::projectZepId), projectZepId)
                    .create();
        }
    }
}

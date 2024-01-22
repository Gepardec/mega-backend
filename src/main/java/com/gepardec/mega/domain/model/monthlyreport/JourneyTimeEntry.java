package com.gepardec.mega.domain.model.monthlyreport;

import java.time.LocalDateTime;

public class JourneyTimeEntry implements ProjectEntry {
    private LocalDateTime fromTime;

    private LocalDateTime toTime;

    private Task task;

    private WorkingLocation workingLocation;

    private JourneyDirection journeyDirection;

    private Vehicle vehicle;

    public JourneyTimeEntry() {

    }

    public JourneyTimeEntry(LocalDateTime fromTime, LocalDateTime toTime, Task task, WorkingLocation workingLocation, JourneyDirection journeyDirection, Vehicle vehicle) {
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.task = task;
        this.workingLocation = workingLocation;
        this.journeyDirection = journeyDirection;
        this.vehicle = vehicle;
    }

    public static JourneyTimeEntryBuilder builder() {
        return JourneyTimeEntryBuilder.aJourneyTimeEntry();
    }

    public LocalDateTime getFromTime() {
        return fromTime;
    }

    public void setFromTime(LocalDateTime fromTime) {
        this.fromTime = fromTime;
    }

    public LocalDateTime getToTime() {
        return toTime;
    }

    public void setToTime(LocalDateTime toTime) {
        this.toTime = toTime;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public WorkingLocation getWorkingLocation() {
        return workingLocation;
    }

    public void setWorkingLocation(WorkingLocation workingLocation) {
        this.workingLocation = workingLocation;
    }

    public JourneyDirection getJourneyDirection() {
        return journeyDirection;
    }

    public void setJourneyDirection(JourneyDirection journeyDirection) {
        this.journeyDirection = journeyDirection;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public static final class JourneyTimeEntryBuilder {
        private LocalDateTime fromTime;
        private LocalDateTime toTime;
        private Task task;
        private WorkingLocation workingLocation;
        private JourneyDirection journeyDirection;
        private Vehicle vehicle;

        private JourneyTimeEntryBuilder() {
        }

        public static JourneyTimeEntryBuilder aJourneyTimeEntry() {
            return new JourneyTimeEntryBuilder();
        }

        public JourneyTimeEntryBuilder fromTime(LocalDateTime fromTime) {
            this.fromTime = fromTime;
            return this;
        }

        public JourneyTimeEntryBuilder toTime(LocalDateTime toTime) {
            this.toTime = toTime;
            return this;
        }

        public JourneyTimeEntryBuilder task(Task task) {
            this.task = task;
            return this;
        }

        public JourneyTimeEntryBuilder workingLocation(WorkingLocation workingLocation) {
            this.workingLocation = workingLocation;
            return this;
        }

        public JourneyTimeEntryBuilder journeyDirection(JourneyDirection journeyDirection) {
            this.journeyDirection = journeyDirection;
            return this;
        }

        public JourneyTimeEntryBuilder vehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        public JourneyTimeEntry build() {
            JourneyTimeEntry journeyTimeEntry = new JourneyTimeEntry();
            journeyTimeEntry.setFromTime(fromTime);
            journeyTimeEntry.setToTime(toTime);
            journeyTimeEntry.setTask(task);
            journeyTimeEntry.setWorkingLocation(workingLocation);
            journeyTimeEntry.setJourneyDirection(journeyDirection);
            journeyTimeEntry.setVehicle(vehicle);
            return journeyTimeEntry;
        }
    }
}
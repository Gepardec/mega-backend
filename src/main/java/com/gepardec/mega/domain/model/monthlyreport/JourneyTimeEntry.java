package com.gepardec.mega.domain.model.monthlyreport;

import java.time.LocalDateTime;

public class JourneyTimeEntry implements ProjectEntry {
    private final LocalDateTime fromTime;

    private final LocalDateTime toTime;

    private final Task task;

    private final WorkingLocation workingLocation;

    private final Boolean workLocationIsProjectRelevant;

    private final JourneyDirection journeyDirection;

    private final Vehicle vehicle;

    private JourneyTimeEntry(Builder builder) {
        this.fromTime = builder.fromTime;
        this.toTime = builder.toTime;
        this.task = builder.task;
        this.workingLocation = builder.workingLocation;
        this.workLocationIsProjectRelevant = builder.workLocationIsProjectRelevant;
        this.journeyDirection = builder.journeyDirection;
        this.vehicle = builder.vehicle;
    }

    public LocalDateTime getFromTime() {
        return fromTime;
    }

    public LocalDateTime getToTime() {
        return toTime;
    }

    public Task getTask() {
        return task;
    }

    public WorkingLocation getWorkingLocation() {
        return workingLocation;
    }

    public Boolean getWorkLocationIsProjectRelevant() {
        return workLocationIsProjectRelevant;
    }

    public JourneyDirection getJourneyDirection() {
        return journeyDirection;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public static Builder builder() {
        return Builder.aJourneyTimeEntry();
    }

    public static final class Builder {
        private LocalDateTime fromTime;
        private LocalDateTime toTime;
        private Task task;
        private WorkingLocation workingLocation;
        private Boolean workLocationIsProjectRelevant;
        private JourneyDirection journeyDirection;
        private Vehicle vehicle;

        private Builder() {

        }

        public static Builder aJourneyTimeEntry() {
            return new Builder();
        }

        public Builder fromTime(LocalDateTime fromTime) {
            this.fromTime = fromTime;
            return this;
        }

        public Builder toTime(LocalDateTime toTime) {
            this.toTime = toTime;
            return this;
        }

        public Builder task(Task task) {
            this.task = task;
            return this;
        }

        public Builder workingLocation(WorkingLocation workingLocation) {
            this.workingLocation = workingLocation;
            return this;
        }

        public Builder workLocationIsProjectRelevant(Boolean workLocationIsProjectRelevant) {
            this.workLocationIsProjectRelevant = workLocationIsProjectRelevant;
            return this;
        }

        public Builder journeyDirection(JourneyDirection journeyDirection) {
            this.journeyDirection = journeyDirection;
            return this;
        }

        public Builder vehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        public JourneyTimeEntry build() {
           return new JourneyTimeEntry(this);
        }
    }
}
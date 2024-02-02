package com.gepardec.mega.domain.model.monthlyreport;

import java.time.LocalDateTime;

public class ProjectTimeEntry implements ProjectEntry {

    private final LocalDateTime fromTime;

    private final LocalDateTime toTime;

    private final Task task;

    private final WorkingLocation workingLocation;

    private final String process;

    private ProjectTimeEntry(Builder builder) {
        this.fromTime = builder.fromTime;
        this.toTime = builder.toTime;
        this.task = builder.task;
        this.workingLocation = builder.workingLocation;
        this.process = builder.process;
    }

    public static Builder builder() {
        return Builder.aProjectTimeEntry();
    }

    @Override
    public LocalDateTime getFromTime() {
        return fromTime;
    }

    @Override
    public LocalDateTime getToTime() {
        return toTime;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public WorkingLocation getWorkingLocation() {
        return workingLocation;
    }

    public String getProcess() {
        return process;
    }

    public static final class Builder {
        private LocalDateTime fromTime;
        private LocalDateTime toTime;
        private Task task;
        private WorkingLocation workingLocation;
        private String process;

        private Builder() {
        }

        public static Builder aProjectTimeEntry() {
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

        public Builder process(String process) {
            this.process = process;
            return this;
        }

        public ProjectTimeEntry build() {
            return new ProjectTimeEntry(this);
        }
    }
}

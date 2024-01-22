package com.gepardec.mega.domain.model.monthlyreport;

import java.time.LocalDateTime;

public class ProjectTimeEntry implements ProjectEntry {

    private LocalDateTime fromTime;

    private LocalDateTime toTime;

    private Task task;

    private WorkingLocation workingLocation;

    private String process;

    public ProjectTimeEntry() {

    }

    public ProjectTimeEntry(LocalDateTime fromTime, LocalDateTime toTime, Task task, WorkingLocation workingLocation, String process) {
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.task = task;
        this.workingLocation = workingLocation;
        this.process = process;
    }

    public static ProjectTimeEntryBuilder builder() {
        return ProjectTimeEntryBuilder.aProjectTimeEntry();
    }

    @Override
    public LocalDateTime getFromTime() {
        return fromTime;
    }

    public void setFromTime(LocalDateTime fromTime) {
        this.fromTime = fromTime;
    }

    @Override
    public LocalDateTime getToTime() {
        return toTime;
    }

    public void setToTime(LocalDateTime toTime) {
        this.toTime = toTime;
    }

    @Override
    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public WorkingLocation getWorkingLocation() {
        return workingLocation;
    }

    public void setWorkingLocation(WorkingLocation workingLocation) {
        this.workingLocation = workingLocation;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public static final class ProjectTimeEntryBuilder {
        private LocalDateTime fromTime;
        private LocalDateTime toTime;
        private Task task;
        private WorkingLocation workingLocation;
        private String process;

        private ProjectTimeEntryBuilder() {
        }

        public static ProjectTimeEntryBuilder aProjectTimeEntry() {
            return new ProjectTimeEntryBuilder();
        }

        public ProjectTimeEntryBuilder fromTime(LocalDateTime fromTime) {
            this.fromTime = fromTime;
            return this;
        }

        public ProjectTimeEntryBuilder toTime(LocalDateTime toTime) {
            this.toTime = toTime;
            return this;
        }

        public ProjectTimeEntryBuilder task(Task task) {
            this.task = task;
            return this;
        }

        public ProjectTimeEntryBuilder workingLocation(WorkingLocation workingLocation) {
            this.workingLocation = workingLocation;
            return this;
        }

        public ProjectTimeEntryBuilder process(String process) {
            this.process = process;
            return this;
        }

        public ProjectTimeEntry build() {
            ProjectTimeEntry projectTimeEntry = new ProjectTimeEntry();
            projectTimeEntry.setFromTime(fromTime);
            projectTimeEntry.setToTime(toTime);
            projectTimeEntry.setTask(task);
            projectTimeEntry.setWorkingLocation(workingLocation);
            projectTimeEntry.setProcess(process);
            return projectTimeEntry;
        }
    }
}
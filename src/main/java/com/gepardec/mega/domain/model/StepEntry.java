package com.gepardec.mega.domain.model;

import java.time.LocalDate;

public class StepEntry {
    private LocalDate date;

    private Project project;

    private State state;

    private User owner;

    private User assignee;

    private Step step;

    public StepEntry() {
    }

    public StepEntry(LocalDate date, Project project, State state, User owner, User assignee, Step step) {
        this.date = date;
        this.project = project;
        this.state = state;
        this.owner = owner;
        this.assignee = assignee;
        this.step = step;
    }

    public static StepEntryBuilder builder() {
        return StepEntryBuilder.aStepEntry();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public static final class StepEntryBuilder {
        private LocalDate date;
        private Project project;
        private State state;
        private User owner;
        private User assignee;
        private Step step;

        private StepEntryBuilder() {
        }

        public static StepEntryBuilder aStepEntry() {
            return new StepEntryBuilder();
        }

        public StepEntryBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public StepEntryBuilder project(Project project) {
            this.project = project;
            return this;
        }

        public StepEntryBuilder state(State state) {
            this.state = state;
            return this;
        }

        public StepEntryBuilder owner(User owner) {
            this.owner = owner;
            return this;
        }

        public StepEntryBuilder assignee(User assignee) {
            this.assignee = assignee;
            return this;
        }

        public StepEntryBuilder step(Step step) {
            this.step = step;
            return this;
        }

        public StepEntry build() {
            StepEntry stepEntry = new StepEntry();
            stepEntry.setDate(date);
            stepEntry.setProject(project);
            stepEntry.setState(state);
            stepEntry.setOwner(owner);
            stepEntry.setAssignee(assignee);
            stepEntry.setStep(step);
            return stepEntry;
        }
    }
}

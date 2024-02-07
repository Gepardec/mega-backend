package com.gepardec.mega.domain.model;

import java.time.LocalDate;

public class StepEntry {

    private final LocalDate date;

    private final Project project;

    private final State state;

    private final User owner;

    private final User assignee;

    private final Step step;

    private StepEntry(Builder builder) {
        this.date = builder.date;
        this.project = builder.project;
        this.state = builder.state;
        this.owner = builder.owner;
        this.assignee = builder.assignee;
        this.step = builder.step;
    }

    public static Builder builder() {
        return Builder.aStepEntry();
    }

    public LocalDate getDate() {
        return date;
    }

    public Project getProject() {
        return project;
    }

    public State getState() {
        return state;
    }

    public User getOwner() {
        return owner;
    }

    public User getAssignee() {
        return assignee;
    }

    public Step getStep() {
        return step;
    }

    public static final class Builder {
        private LocalDate date;
        private Project project;
        private State state;
        private User owner;
        private User assignee;
        private Step step;

        private Builder() {
        }

        public static Builder aStepEntry() {
            return new Builder();
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder project(Project project) {
            this.project = project;
            return this;
        }

        public Builder state(State state) {
            this.state = state;
            return this;
        }

        public Builder owner(User owner) {
            this.owner = owner;
            return this;
        }

        public Builder assignee(User assignee) {
            this.assignee = assignee;
            return this;
        }

        public Builder step(Step step) {
            this.step = step;
            return this;
        }

        public StepEntry build() {
            return new StepEntry(this);
        }
    }
}

package com.gepardec.mega.domain.model;

import com.gepardec.mega.db.entity.project.ProjectStep;

import java.time.LocalDate;

public class ProjectEntry {

    private final LocalDate date;

    private final String name;

    private final ProjectStep step;

    private final User assignee;

    private final User owner;

    private final Project project;


    public ProjectEntry(Builder builder) {
        this.date = builder.date;
        this.name = builder.name;
        this.step = builder.step;
        this.assignee = builder.assignee;
        this.owner = builder.owner;
        this.project = builder.project;
    }

    public static Builder builder() {
        return Builder.aProjectEntry();
    }

    public LocalDate getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public ProjectStep getStep() {
        return step;
    }

    public User getAssignee() {
        return assignee;
    }

    public User getOwner() {
        return owner;
    }

    public Project getProject() {
        return project;
    }

    public static final class Builder {
        private LocalDate date;
        private String name;
        private ProjectStep step;
        private User assignee;
        private User owner;
        private Project project;

        private Builder() {
        }

        public static Builder aProjectEntry() {
            return new Builder();
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder step(ProjectStep step) {
            this.step = step;
            return this;
        }

        public Builder assignee(User assignee) {
            this.assignee = assignee;
            return this;
        }

        public Builder owner(User owner) {
            this.owner = owner;
            return this;
        }

        public Builder project(Project project) {
            this.project = project;
            return this;
        }

        public ProjectEntry build() {
            return new ProjectEntry(this);
        }
    }
}

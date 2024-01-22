package com.gepardec.mega.domain.model;

import com.gepardec.mega.db.entity.project.ProjectStep;

import java.time.LocalDate;

public class ProjectEntry {
    private LocalDate date;

    private String name;

    private ProjectStep step;

    private User assignee;

    private User owner;

    private Project project;

    public ProjectEntry() {
    }

    public ProjectEntry(LocalDate date, String name, ProjectStep step, User assignee, User owner, Project project) {
        this.date = date;
        this.name = name;
        this.step = step;
        this.assignee = assignee;
        this.owner = owner;
        this.project = project;
    }

    public static ProjectEntryBuilder builder() {
        return ProjectEntryBuilder.aProjectEntry();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProjectStep getStep() {
        return step;
    }

    public void setStep(ProjectStep step) {
        this.step = step;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public static final class ProjectEntryBuilder {
        private LocalDate date;
        private String name;
        private ProjectStep step;
        private User assignee;
        private User owner;
        private Project project;

        private ProjectEntryBuilder() {
        }

        public static ProjectEntryBuilder aProjectEntry() {
            return new ProjectEntryBuilder();
        }

        public ProjectEntryBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public ProjectEntryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ProjectEntryBuilder step(ProjectStep step) {
            this.step = step;
            return this;
        }

        public ProjectEntryBuilder assignee(User assignee) {
            this.assignee = assignee;
            return this;
        }

        public ProjectEntryBuilder owner(User owner) {
            this.owner = owner;
            return this;
        }

        public ProjectEntryBuilder project(Project project) {
            this.project = project;
            return this;
        }

        public ProjectEntry build() {
            ProjectEntry projectEntry = new ProjectEntry();
            projectEntry.setDate(date);
            projectEntry.setName(name);
            projectEntry.setStep(step);
            projectEntry.setAssignee(assignee);
            projectEntry.setOwner(owner);
            projectEntry.setProject(project);
            return projectEntry;
        }
    }
}

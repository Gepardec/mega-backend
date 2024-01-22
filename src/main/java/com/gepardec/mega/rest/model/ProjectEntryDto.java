package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.domain.model.ProjectState;
import com.gepardec.mega.domain.model.ProjectStep;

import java.util.Objects;

// TODO Discuss: Rename all rest pojos to DTO?
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectEntryDto {
    @JsonProperty
    private ProjectState state;

    @JsonProperty
    private boolean preset;

    @JsonProperty
    private String projectName;

    @JsonProperty
    private ProjectStep step;

    @JsonProperty
    private String currentMonthYear;

    public ProjectEntryDto() {
    }

    public ProjectEntryDto(ProjectState state, boolean preset, String projectName, ProjectStep step, String currentMonthYear) {
        this.state = state;
        this.preset = preset;
        this.projectName = projectName;
        this.step = step;
        this.currentMonthYear = currentMonthYear;
    }

    public static ProjectEntryDtoBuilder builder() {
        return ProjectEntryDtoBuilder.aProjectEntryDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectEntryDto that = (ProjectEntryDto) o;
        return isPreset() == that.isPreset() && getState() == that.getState() && Objects.equals(getProjectName(), that.getProjectName()) && getStep() == that.getStep() && Objects.equals(getCurrentMonthYear(), that.getCurrentMonthYear());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getState(), isPreset(), getProjectName(), getStep(), getCurrentMonthYear());
    }

    public ProjectState getState() {
        return state;
    }

    public void setState(ProjectState state) {
        this.state = state;
    }

    public boolean isPreset() {
        return preset;
    }

    public void setPreset(boolean preset) {
        this.preset = preset;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public ProjectStep getStep() {
        return step;
    }

    public void setStep(ProjectStep step) {
        this.step = step;
    }

    public String getCurrentMonthYear() {
        return currentMonthYear;
    }

    public void setCurrentMonthYear(String currentMonthYear) {
        this.currentMonthYear = currentMonthYear;
    }

    public static final class ProjectEntryDtoBuilder {
        private ProjectState state;
        private boolean preset;
        private String projectName;
        private ProjectStep step;
        private String currentMonthYear;

        private ProjectEntryDtoBuilder() {
        }

        public static ProjectEntryDtoBuilder aProjectEntryDto() {
            return new ProjectEntryDtoBuilder();
        }

        public ProjectEntryDtoBuilder state(ProjectState state) {
            this.state = state;
            return this;
        }

        public ProjectEntryDtoBuilder preset(boolean preset) {
            this.preset = preset;
            return this;
        }

        public ProjectEntryDtoBuilder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public ProjectEntryDtoBuilder step(ProjectStep step) {
            this.step = step;
            return this;
        }

        public ProjectEntryDtoBuilder currentMonthYear(String currentMonthYear) {
            this.currentMonthYear = currentMonthYear;
            return this;
        }

        public ProjectEntryDto build() {
            ProjectEntryDto projectEntryDto = new ProjectEntryDto();
            projectEntryDto.setState(state);
            projectEntryDto.setPreset(preset);
            projectEntryDto.setProjectName(projectName);
            projectEntryDto.setStep(step);
            projectEntryDto.setCurrentMonthYear(currentMonthYear);
            return projectEntryDto;
        }
    }
}

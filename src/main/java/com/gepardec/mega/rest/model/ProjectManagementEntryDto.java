package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.domain.model.ProjectState;
import jakarta.annotation.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectManagementEntryDto {
    @JsonProperty
    private Integer zepId;

    @JsonProperty
    private String projectName;

    @JsonProperty
    @Nullable
    private ProjectState controlProjectState;

    @JsonProperty
    @Nullable
    private ProjectState controlBillingState;

    @JsonProperty
    @Nullable
    private Boolean presetControlProjectState;

    @JsonProperty
    @Nullable
    private Boolean presetControlBillingState;

    @JsonProperty
    private List<ManagementEntryDto> entries;

    @JsonProperty
    private Duration aggregatedBillableWorkTimeInSeconds;

    @JsonProperty
    private Duration aggregatedNonBillableWorkTimeInSeconds;

    public ProjectManagementEntryDto() {
    }

    public ProjectManagementEntryDto(Integer zepId, String projectName, @Nullable ProjectState controlProjectState, @Nullable ProjectState controlBillingState, @Nullable Boolean presetControlProjectState, @Nullable Boolean presetControlBillingState, List<ManagementEntryDto> entries, Duration aggregatedBillableWorkTimeInSeconds, Duration aggregatedNonBillableWorkTimeInSeconds) {
        this.zepId = zepId;
        this.projectName = projectName;
        this.controlProjectState = controlProjectState;
        this.controlBillingState = controlBillingState;
        this.presetControlProjectState = presetControlProjectState;
        this.presetControlBillingState = presetControlBillingState;
        this.entries = entries;
        this.aggregatedBillableWorkTimeInSeconds = aggregatedBillableWorkTimeInSeconds;
        this.aggregatedNonBillableWorkTimeInSeconds = aggregatedNonBillableWorkTimeInSeconds;
    }

    public static ProjectManagementEntryDtoBuilder builder() {
        return ProjectManagementEntryDtoBuilder.aProjectManagementEntryDto();
    }

    public Integer getZepId() {
        return zepId;
    }

    public void setZepId(Integer zepId) {
        this.zepId = zepId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Nullable
    public ProjectState getControlProjectState() {
        return controlProjectState;
    }

    public void setControlProjectState(@Nullable ProjectState controlProjectState) {
        this.controlProjectState = controlProjectState;
    }

    @Nullable
    public ProjectState getControlBillingState() {
        return controlBillingState;
    }

    public void setControlBillingState(@Nullable ProjectState controlBillingState) {
        this.controlBillingState = controlBillingState;
    }

    @Nullable
    public Boolean getPresetControlProjectState() {
        return presetControlProjectState;
    }

    public void setPresetControlProjectState(@Nullable Boolean presetControlProjectState) {
        this.presetControlProjectState = presetControlProjectState;
    }

    @Nullable
    public Boolean getPresetControlBillingState() {
        return presetControlBillingState;
    }

    public void setPresetControlBillingState(@Nullable Boolean presetControlBillingState) {
        this.presetControlBillingState = presetControlBillingState;
    }

    public List<ManagementEntryDto> getEntries() {
        return entries;
    }

    public void setEntries(List<ManagementEntryDto> entries) {
        this.entries = entries;
    }

    public Duration getAggregatedBillableWorkTimeInSeconds() {
        return aggregatedBillableWorkTimeInSeconds;
    }

    public void setAggregatedBillableWorkTimeInSeconds(Duration aggregatedBillableWorkTimeInSeconds) {
        this.aggregatedBillableWorkTimeInSeconds = aggregatedBillableWorkTimeInSeconds;
    }

    public Duration getAggregatedNonBillableWorkTimeInSeconds() {
        return aggregatedNonBillableWorkTimeInSeconds;
    }

    public void setAggregatedNonBillableWorkTimeInSeconds(Duration aggregatedNonBillableWorkTimeInSeconds) {
        this.aggregatedNonBillableWorkTimeInSeconds = aggregatedNonBillableWorkTimeInSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectManagementEntryDto that = (ProjectManagementEntryDto) o;
        return Objects.equals(getZepId(), that.getZepId()) && Objects.equals(getProjectName(), that.getProjectName()) && getControlProjectState() == that.getControlProjectState() && getControlBillingState() == that.getControlBillingState() && Objects.equals(getPresetControlProjectState(), that.getPresetControlProjectState()) && Objects.equals(getPresetControlBillingState(), that.getPresetControlBillingState()) && Objects.equals(getEntries(), that.getEntries()) && Objects.equals(getAggregatedBillableWorkTimeInSeconds(), that.getAggregatedBillableWorkTimeInSeconds()) && Objects.equals(getAggregatedNonBillableWorkTimeInSeconds(), that.getAggregatedNonBillableWorkTimeInSeconds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getZepId(), getProjectName(), getControlProjectState(), getControlBillingState(), getPresetControlProjectState(), getPresetControlBillingState(), getEntries(), getAggregatedBillableWorkTimeInSeconds(), getAggregatedNonBillableWorkTimeInSeconds());
    }

    public static final class ProjectManagementEntryDtoBuilder {
        private Integer zepId;
        private String projectName;
        private ProjectState controlProjectState;
        private ProjectState controlBillingState;
        private Boolean presetControlProjectState;
        private Boolean presetControlBillingState;
        private List<ManagementEntryDto> entries;
        private Duration aggregatedBillableWorkTimeInSeconds;
        private Duration aggregatedNonBillableWorkTimeInSeconds;

        private ProjectManagementEntryDtoBuilder() {
        }

        public static ProjectManagementEntryDtoBuilder aProjectManagementEntryDto() {
            return new ProjectManagementEntryDtoBuilder();
        }

        public ProjectManagementEntryDtoBuilder zepId(Integer zepId) {
            this.zepId = zepId;
            return this;
        }

        public ProjectManagementEntryDtoBuilder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public ProjectManagementEntryDtoBuilder controlProjectState(ProjectState controlProjectState) {
            this.controlProjectState = controlProjectState;
            return this;
        }

        public ProjectManagementEntryDtoBuilder controlBillingState(ProjectState controlBillingState) {
            this.controlBillingState = controlBillingState;
            return this;
        }

        public ProjectManagementEntryDtoBuilder presetControlProjectState(Boolean presetControlProjectState) {
            this.presetControlProjectState = presetControlProjectState;
            return this;
        }

        public ProjectManagementEntryDtoBuilder presetControlBillingState(Boolean presetControlBillingState) {
            this.presetControlBillingState = presetControlBillingState;
            return this;
        }

        public ProjectManagementEntryDtoBuilder entries(List<ManagementEntryDto> entries) {
            this.entries = entries;
            return this;
        }

        public ProjectManagementEntryDtoBuilder aggregatedBillableWorkTimeInSeconds(Duration aggregatedBillableWorkTimeInSeconds) {
            this.aggregatedBillableWorkTimeInSeconds = aggregatedBillableWorkTimeInSeconds;
            return this;
        }

        public ProjectManagementEntryDtoBuilder aggregatedNonBillableWorkTimeInSeconds(Duration aggregatedNonBillableWorkTimeInSeconds) {
            this.aggregatedNonBillableWorkTimeInSeconds = aggregatedNonBillableWorkTimeInSeconds;
            return this;
        }

        public ProjectManagementEntryDto build() {
            ProjectManagementEntryDto projectManagementEntryDto = new ProjectManagementEntryDto();
            projectManagementEntryDto.setZepId(zepId);
            projectManagementEntryDto.setProjectName(projectName);
            projectManagementEntryDto.setControlProjectState(controlProjectState);
            projectManagementEntryDto.setControlBillingState(controlBillingState);
            projectManagementEntryDto.setPresetControlProjectState(presetControlProjectState);
            projectManagementEntryDto.setPresetControlBillingState(presetControlBillingState);
            projectManagementEntryDto.setEntries(entries);
            projectManagementEntryDto.setAggregatedBillableWorkTimeInSeconds(aggregatedBillableWorkTimeInSeconds);
            projectManagementEntryDto.setAggregatedNonBillableWorkTimeInSeconds(aggregatedNonBillableWorkTimeInSeconds);
            return projectManagementEntryDto;
        }
    }
}

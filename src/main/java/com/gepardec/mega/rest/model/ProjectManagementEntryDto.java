package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.domain.model.ProjectState;
import jakarta.annotation.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectManagementEntryDto {

    private final Integer zepId;

    private final String projectName;

    private final ProjectState controlProjectState;

    private final ProjectState controlBillingState;

    private final Boolean presetControlProjectState;

    private final Boolean presetControlBillingState;

    private final List<ManagementEntryDto> entries;

    private final Duration aggregatedBillableWorkTimeInSeconds;

    private final Duration aggregatedNonBillableWorkTimeInSeconds;

    @JsonCreator
    public ProjectManagementEntryDto(Builder builder) {
        this.zepId = builder.zepId;
        this.projectName = builder.projectName;
        this.controlProjectState = builder.controlProjectState;
        this.controlBillingState = builder.controlBillingState;
        this.presetControlProjectState = builder.presetControlProjectState;
        this.presetControlBillingState = builder.presetControlBillingState;
        this.entries = builder.entries;
        this.aggregatedBillableWorkTimeInSeconds = builder.aggregatedBillableWorkTimeInSeconds;
        this.aggregatedNonBillableWorkTimeInSeconds = builder.aggregatedNonBillableWorkTimeInSeconds;
    }

    public static Builder builder() {
        return Builder.aProjectManagementEntryDto();
    }

    public Integer getZepId() {
        return zepId;
    }

    public String getProjectName() {
        return projectName;
    }

    @Nullable
    public ProjectState getControlProjectState() {
        return controlProjectState;
    }

    @Nullable
    public ProjectState getControlBillingState() {
        return controlBillingState;
    }

    @Nullable
    public Boolean getPresetControlProjectState() {
        return presetControlProjectState;
    }

    @Nullable
    public Boolean getPresetControlBillingState() {
        return presetControlBillingState;
    }

    public List<ManagementEntryDto> getEntries() {
        return entries;
    }


    public Duration getAggregatedBillableWorkTimeInSeconds() {
        return aggregatedBillableWorkTimeInSeconds;
    }

    public Duration getAggregatedNonBillableWorkTimeInSeconds() {
        return aggregatedNonBillableWorkTimeInSeconds;
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

    public static final class Builder {
       @JsonProperty private Integer zepId;
       @JsonProperty private String projectName;
       @JsonProperty @Nullable private ProjectState controlProjectState;
       @JsonProperty @Nullable private ProjectState controlBillingState;
       @JsonProperty @Nullable private Boolean presetControlProjectState;
       @JsonProperty @Nullable private Boolean presetControlBillingState;
       @JsonProperty private List<ManagementEntryDto> entries;
       @JsonProperty private Duration aggregatedBillableWorkTimeInSeconds;
       @JsonProperty private Duration aggregatedNonBillableWorkTimeInSeconds;

        private Builder() {
        }

        public static Builder aProjectManagementEntryDto() {
            return new Builder();
        }

        public Builder zepId(Integer zepId) {
            this.zepId = zepId;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder controlProjectState(ProjectState controlProjectState) {
            this.controlProjectState = controlProjectState;
            return this;
        }

        public Builder controlBillingState(ProjectState controlBillingState) {
            this.controlBillingState = controlBillingState;
            return this;
        }

        public Builder presetControlProjectState(Boolean presetControlProjectState) {
            this.presetControlProjectState = presetControlProjectState;
            return this;
        }

        public Builder presetControlBillingState(Boolean presetControlBillingState) {
            this.presetControlBillingState = presetControlBillingState;
            return this;
        }

        public Builder entries(List<ManagementEntryDto> entries) {
            this.entries = entries;
            return this;
        }

        public Builder aggregatedBillableWorkTimeInSeconds(Duration aggregatedBillableWorkTimeInSeconds) {
            this.aggregatedBillableWorkTimeInSeconds = aggregatedBillableWorkTimeInSeconds;
            return this;
        }

        public Builder aggregatedNonBillableWorkTimeInSeconds(Duration aggregatedNonBillableWorkTimeInSeconds) {
            this.aggregatedNonBillableWorkTimeInSeconds = aggregatedNonBillableWorkTimeInSeconds;
            return this;
        }

        public ProjectManagementEntryDto build() {
            return new ProjectManagementEntryDto(this);
        }
    }
}

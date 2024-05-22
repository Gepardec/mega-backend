package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = ProjectHoursSummaryDto.Builder.class)
public class ProjectHoursSummaryDto {
    private final String projectName;
    private final double billableHoursSum;
    private final double nonBillableHoursSum;
    private final double chargeability;
    private final boolean isInternalProject;

    private ProjectHoursSummaryDto(Builder builder){
        this.projectName = builder.projectName;
        this.billableHoursSum = builder.billableHoursSum;
        this.nonBillableHoursSum = builder.nonBillableHoursSum;
        this.chargeability = builder.chargeability;
        this.isInternalProject = builder.isInternalProject;
    }

    public static Builder builder() {return Builder.aProjectHoursSummaryDto();}

    public String getProjectName() {
        return projectName;
    }

    public double getBillableHoursSum() {
        return billableHoursSum;
    }

    public double getNonBillableHoursSum() {
        return nonBillableHoursSum;
    }

    public double getChargeability() {
        return chargeability;
    }

    public boolean getIsInternalProject() {
        return isInternalProject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectHoursSummaryDto that = (ProjectHoursSummaryDto) o;
        return Double.compare(billableHoursSum, that.billableHoursSum) == 0 && Double.compare(nonBillableHoursSum, that.nonBillableHoursSum) == 0 && Objects.equals(projectName, that.projectName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectName, billableHoursSum, nonBillableHoursSum);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String projectName;
        private double billableHoursSum;
        private double nonBillableHoursSum;
        private double chargeability;
        private boolean isInternalProject;
        private Builder(){}
        public static Builder aProjectHoursSummaryDto(){return new Builder();}
        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder billableHoursSum(double billableHoursSum) {
            this.billableHoursSum = billableHoursSum;
            return this;
        }

        public Builder nonBillableHoursSum(double nonBillableHoursSum) {
            this.nonBillableHoursSum = nonBillableHoursSum;
            return this;
        }

        public Builder chargeability(double chargeability) {
            this.chargeability = chargeability;
            return this;
        }

        public Builder isInternalProject(boolean isInternalProject) {
            this.isInternalProject = isInternalProject;
            return this;
        }

        public ProjectHoursSummaryDto build() {return new ProjectHoursSummaryDto(this);}
    }
}

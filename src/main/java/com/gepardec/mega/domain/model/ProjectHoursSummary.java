package com.gepardec.mega.domain.model;


public class ProjectHoursSummary {
    private final String projectName;
    private final double billableHoursSum;
    private final double nonBillableHoursSum;
    private final double chargeability;

    private final boolean isInternalProject;

    private ProjectHoursSummary(Builder builder){
        this.projectName = builder.projectName;
        this.billableHoursSum = builder.billableHoursSum;
        this.nonBillableHoursSum = builder.nonBillableHoursSum;
        this.chargeability = builder.chargeability;
        this.isInternalProject = builder.isInternalProject;
    }

    public static Builder builder() {return Builder.aProjectHoursSummary();}

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

    public static final class Builder {
        private String projectName;
        private double billableHoursSum;
        private double nonBillableHoursSum;
        private double chargeability;
        private boolean isInternalProject;

        private Builder(){}
        public static Builder aProjectHoursSummary(){return new Builder();}
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

        public ProjectHoursSummary build() {return new ProjectHoursSummary(this);}
    }
}

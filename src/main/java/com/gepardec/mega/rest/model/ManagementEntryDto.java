package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.gepardec.mega.domain.model.State;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = ManagementEntryDto.Builder.class)
public class ManagementEntryDto {

    private final EmployeeDto employee;

    private final State employeeCheckState;

    private final String employeeCheckStateReason;

    private final State internalCheckState;

    private final State projectCheckState;

    private final List<PmProgressDto> employeeProgresses;

    private final long totalComments;

    private final long finishedComments;

    private final String entryDate;

    private final String billableTime;

    private final String nonBillableTime;

    private ManagementEntryDto(Builder builder) {
        this.employee = builder.employee;
        this.employeeCheckState = builder.employeeCheckState;
        this.employeeCheckStateReason = builder.employeeCheckStateReason;
        this.internalCheckState = builder.internalCheckState;
        this.projectCheckState = builder.projectCheckState;
        this.employeeProgresses = builder.employeeProgresses;
        this.totalComments = builder.totalComments;
        this.finishedComments = builder.finishedComments;
        this.entryDate = builder.entryDate;
        this.billableTime = builder.billableTime;
        this.nonBillableTime = builder.nonBillableTime;
    }

    public static Builder builder() {
        return Builder.aManagementEntryDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagementEntryDto that = (ManagementEntryDto) o;
        return getTotalComments() == that.getTotalComments() && getFinishedComments() == that.getFinishedComments() && Objects.equals(getEmployee(), that.getEmployee()) && getEmployeeCheckState() == that.getEmployeeCheckState() && Objects.equals(getEmployeeCheckStateReason(), that.getEmployeeCheckStateReason()) && getInternalCheckState() == that.getInternalCheckState() && getProjectCheckState() == that.getProjectCheckState() && Objects.equals(getEmployeeProgresses(), that.getEmployeeProgresses()) && Objects.equals(getEntryDate(), that.getEntryDate()) && Objects.equals(getBillableTime(), that.getBillableTime()) && Objects.equals(getNonBillableTime(), that.getNonBillableTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmployee(), getEmployeeCheckState(), getEmployeeCheckStateReason(), getInternalCheckState(), getProjectCheckState(), getEmployeeProgresses(), getTotalComments(), getFinishedComments(), getEntryDate(), getBillableTime(), getNonBillableTime());
    }

    public EmployeeDto getEmployee() {
        return employee;
    }

    public State getEmployeeCheckState() {
        return employeeCheckState;
    }

    public String getEmployeeCheckStateReason() {
        return employeeCheckStateReason;
    }

    public State getInternalCheckState() {
        return internalCheckState;
    }

    public State getProjectCheckState() {
        return projectCheckState;
    }

    @Nullable
    public List<PmProgressDto> getEmployeeProgresses() {
        return employeeProgresses;
    }


    public long getTotalComments() {
        return totalComments;
    }

    public long getFinishedComments() {
        return finishedComments;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public String getBillableTime() {
        return billableTime;
    }

    public String getNonBillableTime() {
        return nonBillableTime;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private EmployeeDto employee;
        private State employeeCheckState;
        private String employeeCheckStateReason;
        private State internalCheckState;
        private State projectCheckState;
        private List<PmProgressDto> employeeProgresses;
        private long totalComments;
        private long finishedComments;
        private String entryDate;
        private String billableTime;
        private String nonBillableTime;

        private Builder() {
        }

        public static Builder aManagementEntryDto() {
            return new Builder();
        }

        public Builder employee(EmployeeDto employee) {
            this.employee = employee;
            return this;
        }

        public Builder employeeCheckState(State employeeCheckState) {
            this.employeeCheckState = employeeCheckState;
            return this;
        }

        public Builder employeeCheckStateReason(String employeeCheckStateReason) {
            this.employeeCheckStateReason = employeeCheckStateReason;
            return this;
        }

        public Builder internalCheckState(State internalCheckState) {
            this.internalCheckState = internalCheckState;
            return this;
        }

        public Builder projectCheckState(State projectCheckState) {
            this.projectCheckState = projectCheckState;
            return this;
        }

        public Builder employeeProgresses(List<PmProgressDto> employeeProgresses) {
            this.employeeProgresses = employeeProgresses;
            return this;
        }

        public Builder totalComments(long totalComments) {
            this.totalComments = totalComments;
            return this;
        }

        public Builder finishedComments(long finishedComments) {
            this.finishedComments = finishedComments;
            return this;
        }

        public Builder entryDate(String entryDate) {
            this.entryDate = entryDate;
            return this;
        }

        public Builder billableTime(String billableTime) {
            this.billableTime = billableTime;
            return this;
        }

        public Builder nonBillableTime(String nonBillableTime) {
            this.nonBillableTime = nonBillableTime;
            return this;
        }

        public ManagementEntryDto build() {
            return new ManagementEntryDto(this);
        }
    }
}

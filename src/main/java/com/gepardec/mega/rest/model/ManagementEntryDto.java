package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.domain.model.State;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagementEntryDto {

    @JsonProperty
    private EmployeeDto employee;

    @JsonProperty
    private State employeeCheckState;

    @JsonProperty
    private String employeeCheckStateReason;

    @JsonProperty
    private State internalCheckState;

    @JsonProperty
    private State projectCheckState;

    @JsonProperty
    @Nullable
    private List<PmProgressDto> employeeProgresses;

    @JsonProperty
    private long totalComments;

    @JsonProperty
    private long finishedComments;

    @JsonProperty
    private String entryDate;

    @JsonProperty
    private String billableTime;

    @JsonProperty
    private String nonBillableTime;

    public ManagementEntryDto() {
    }

    public ManagementEntryDto(EmployeeDto employee, State employeeCheckState, String employeeCheckStateReason, State internalCheckState, State projectCheckState, @Nullable List<PmProgressDto> employeeProgresses, long totalComments, long finishedComments, String entryDate, String billableTime, String nonBillableTime) {
        this.employee = employee;
        this.employeeCheckState = employeeCheckState;
        this.employeeCheckStateReason = employeeCheckStateReason;
        this.internalCheckState = internalCheckState;
        this.projectCheckState = projectCheckState;
        this.employeeProgresses = employeeProgresses;
        this.totalComments = totalComments;
        this.finishedComments = finishedComments;
        this.entryDate = entryDate;
        this.billableTime = billableTime;
        this.nonBillableTime = nonBillableTime;
    }

    public static ManagementEntryDtoBuilder builder() {
        return ManagementEntryDtoBuilder.aManagementEntryDto();
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

    public void setEmployee(EmployeeDto employee) {
        this.employee = employee;
    }

    public State getEmployeeCheckState() {
        return employeeCheckState;
    }

    public void setEmployeeCheckState(State employeeCheckState) {
        this.employeeCheckState = employeeCheckState;
    }

    public String getEmployeeCheckStateReason() {
        return employeeCheckStateReason;
    }

    public void setEmployeeCheckStateReason(String employeeCheckStateReason) {
        this.employeeCheckStateReason = employeeCheckStateReason;
    }

    public State getInternalCheckState() {
        return internalCheckState;
    }

    public void setInternalCheckState(State internalCheckState) {
        this.internalCheckState = internalCheckState;
    }

    public State getProjectCheckState() {
        return projectCheckState;
    }

    public void setProjectCheckState(State projectCheckState) {
        this.projectCheckState = projectCheckState;
    }

    @Nullable
    public List<PmProgressDto> getEmployeeProgresses() {
        return employeeProgresses;
    }

    public void setEmployeeProgresses(@Nullable List<PmProgressDto> employeeProgresses) {
        this.employeeProgresses = employeeProgresses;
    }

    public long getTotalComments() {
        return totalComments;
    }

    public void setTotalComments(long totalComments) {
        this.totalComments = totalComments;
    }

    public long getFinishedComments() {
        return finishedComments;
    }

    public void setFinishedComments(long finishedComments) {
        this.finishedComments = finishedComments;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public String getBillableTime() {
        return billableTime;
    }

    public void setBillableTime(String billableTime) {
        this.billableTime = billableTime;
    }

    public String getNonBillableTime() {
        return nonBillableTime;
    }

    public void setNonBillableTime(String nonBillableTime) {
        this.nonBillableTime = nonBillableTime;
    }

    public static final class ManagementEntryDtoBuilder {
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

        private ManagementEntryDtoBuilder() {
        }

        public static ManagementEntryDtoBuilder aManagementEntryDto() {
            return new ManagementEntryDtoBuilder();
        }

        public ManagementEntryDtoBuilder employee(EmployeeDto employee) {
            this.employee = employee;
            return this;
        }

        public ManagementEntryDtoBuilder employeeCheckState(State employeeCheckState) {
            this.employeeCheckState = employeeCheckState;
            return this;
        }

        public ManagementEntryDtoBuilder employeeCheckStateReason(String employeeCheckStateReason) {
            this.employeeCheckStateReason = employeeCheckStateReason;
            return this;
        }

        public ManagementEntryDtoBuilder internalCheckState(State internalCheckState) {
            this.internalCheckState = internalCheckState;
            return this;
        }

        public ManagementEntryDtoBuilder projectCheckState(State projectCheckState) {
            this.projectCheckState = projectCheckState;
            return this;
        }

        public ManagementEntryDtoBuilder employeeProgresses(List<PmProgressDto> employeeProgresses) {
            this.employeeProgresses = employeeProgresses;
            return this;
        }

        public ManagementEntryDtoBuilder totalComments(long totalComments) {
            this.totalComments = totalComments;
            return this;
        }

        public ManagementEntryDtoBuilder finishedComments(long finishedComments) {
            this.finishedComments = finishedComments;
            return this;
        }

        public ManagementEntryDtoBuilder entryDate(String entryDate) {
            this.entryDate = entryDate;
            return this;
        }

        public ManagementEntryDtoBuilder billableTime(String billableTime) {
            this.billableTime = billableTime;
            return this;
        }

        public ManagementEntryDtoBuilder nonBillableTime(String nonBillableTime) {
            this.nonBillableTime = nonBillableTime;
            return this;
        }

        public ManagementEntryDto build() {
            ManagementEntryDto managementEntryDto = new ManagementEntryDto();
            managementEntryDto.setEmployee(employee);
            managementEntryDto.setEmployeeCheckState(employeeCheckState);
            managementEntryDto.setEmployeeCheckStateReason(employeeCheckStateReason);
            managementEntryDto.setInternalCheckState(internalCheckState);
            managementEntryDto.setProjectCheckState(projectCheckState);
            managementEntryDto.setEmployeeProgresses(employeeProgresses);
            managementEntryDto.setTotalComments(totalComments);
            managementEntryDto.setFinishedComments(finishedComments);
            managementEntryDto.setEntryDate(entryDate);
            managementEntryDto.setBillableTime(billableTime);
            managementEntryDto.setNonBillableTime(nonBillableTime);
            return managementEntryDto;
        }
    }
}

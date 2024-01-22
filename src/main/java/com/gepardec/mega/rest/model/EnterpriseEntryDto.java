package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gepardec.mega.domain.model.ProjectState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnterpriseEntryDto {

    private ProjectState zepTimesReleased;

    private ProjectState chargeabilityExternalEmployeesRecorded;

    private ProjectState payrollAccountingSent;

    private LocalDate date;

    private LocalDateTime creationDate;

    public EnterpriseEntryDto() {
    }

    public EnterpriseEntryDto(ProjectState zepTimesReleased, ProjectState chargeabilityExternalEmployeesRecorded, ProjectState payrollAccountingSent, LocalDate date, LocalDateTime creationDate) {
        this.zepTimesReleased = zepTimesReleased;
        this.chargeabilityExternalEmployeesRecorded = chargeabilityExternalEmployeesRecorded;
        this.payrollAccountingSent = payrollAccountingSent;
        this.date = date;
        this.creationDate = creationDate;
    }

    public static EnterpriseEntryDtoBuilder builder() {
        return EnterpriseEntryDtoBuilder.anEnterpriseEntryDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnterpriseEntryDto that = (EnterpriseEntryDto) o;
        return getZepTimesReleased() == that.getZepTimesReleased() && getChargeabilityExternalEmployeesRecorded() == that.getChargeabilityExternalEmployeesRecorded() && getPayrollAccountingSent() == that.getPayrollAccountingSent() && Objects.equals(getDate(), that.getDate()) && Objects.equals(getCreationDate(), that.getCreationDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getZepTimesReleased(), getChargeabilityExternalEmployeesRecorded(), getPayrollAccountingSent(), getDate(), getCreationDate());
    }

    public ProjectState getZepTimesReleased() {
        return zepTimesReleased;
    }

    public void setZepTimesReleased(ProjectState zepTimesReleased) {
        this.zepTimesReleased = zepTimesReleased;
    }

    public ProjectState getChargeabilityExternalEmployeesRecorded() {
        return chargeabilityExternalEmployeesRecorded;
    }

    public void setChargeabilityExternalEmployeesRecorded(ProjectState chargeabilityExternalEmployeesRecorded) {
        this.chargeabilityExternalEmployeesRecorded = chargeabilityExternalEmployeesRecorded;
    }

    public ProjectState getPayrollAccountingSent() {
        return payrollAccountingSent;
    }

    public void setPayrollAccountingSent(ProjectState payrollAccountingSent) {
        this.payrollAccountingSent = payrollAccountingSent;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public static final class EnterpriseEntryDtoBuilder {
        private ProjectState zepTimesReleased;
        private ProjectState chargeabilityExternalEmployeesRecorded;
        private ProjectState payrollAccountingSent;
        private LocalDate date;
        private LocalDateTime creationDate;

        private EnterpriseEntryDtoBuilder() {
        }

        public static EnterpriseEntryDtoBuilder anEnterpriseEntryDto() {
            return new EnterpriseEntryDtoBuilder();
        }

        public EnterpriseEntryDtoBuilder zepTimesReleased(ProjectState zepTimesReleased) {
            this.zepTimesReleased = zepTimesReleased;
            return this;
        }

        public EnterpriseEntryDtoBuilder chargeabilityExternalEmployeesRecorded(ProjectState chargeabilityExternalEmployeesRecorded) {
            this.chargeabilityExternalEmployeesRecorded = chargeabilityExternalEmployeesRecorded;
            return this;
        }

        public EnterpriseEntryDtoBuilder payrollAccountingSent(ProjectState payrollAccountingSent) {
            this.payrollAccountingSent = payrollAccountingSent;
            return this;
        }

        public EnterpriseEntryDtoBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public EnterpriseEntryDtoBuilder creationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public EnterpriseEntryDto build() {
            EnterpriseEntryDto enterpriseEntryDto = new EnterpriseEntryDto();
            enterpriseEntryDto.setZepTimesReleased(zepTimesReleased);
            enterpriseEntryDto.setChargeabilityExternalEmployeesRecorded(chargeabilityExternalEmployeesRecorded);
            enterpriseEntryDto.setPayrollAccountingSent(payrollAccountingSent);
            enterpriseEntryDto.setDate(date);
            enterpriseEntryDto.setCreationDate(creationDate);
            return enterpriseEntryDto;
        }
    }
}

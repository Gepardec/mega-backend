package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.domain.model.ProjectState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnterpriseEntryDto {

    private final ProjectState zepTimesReleased;

    private final ProjectState chargeabilityExternalEmployeesRecorded;

    private final ProjectState payrollAccountingSent;

    private final LocalDate date;

    private final LocalDateTime creationDate;


    @JsonCreator
    public EnterpriseEntryDto(Builder builder) {
        this.zepTimesReleased = builder.zepTimesReleased;
        this.chargeabilityExternalEmployeesRecorded = builder.chargeabilityExternalEmployeesRecorded;
        this.payrollAccountingSent = builder.payrollAccountingSent;
        this.date = builder.date;
        this.creationDate = builder.creationDate;
    }

    public static Builder builder() {
        return Builder.anEnterpriseEntryDto();
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

    public ProjectState getChargeabilityExternalEmployeesRecorded() {
        return chargeabilityExternalEmployeesRecorded;
    }

    public ProjectState getPayrollAccountingSent() {
        return payrollAccountingSent;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public static final class Builder {
        @JsonProperty private ProjectState zepTimesReleased;
        @JsonProperty private ProjectState chargeabilityExternalEmployeesRecorded;
        @JsonProperty private ProjectState payrollAccountingSent;
        @JsonProperty private LocalDate date;
        @JsonProperty private LocalDateTime creationDate;

        private Builder() {
        }

        public static Builder anEnterpriseEntryDto() {
            return new Builder();
        }

        public Builder zepTimesReleased(ProjectState zepTimesReleased) {
            this.zepTimesReleased = zepTimesReleased;
            return this;
        }

        public Builder chargeabilityExternalEmployeesRecorded(ProjectState chargeabilityExternalEmployeesRecorded) {
            this.chargeabilityExternalEmployeesRecorded = chargeabilityExternalEmployeesRecorded;
            return this;
        }

        public Builder payrollAccountingSent(ProjectState payrollAccountingSent) {
            this.payrollAccountingSent = payrollAccountingSent;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder creationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public EnterpriseEntryDto build() {
            return new EnterpriseEntryDto(this);
        }
    }
}

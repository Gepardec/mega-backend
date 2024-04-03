package com.gepardec.mega.zep.rest.entity;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

public record ZepAbsence (
    Integer id,
    String employeeId,
    LocalDate startDate,
    LocalDate endDate,
    boolean approved,
    ZepAbsenceReason absenceReason) {

    @JsonCreator
    public ZepAbsence(Builder builder) {
        this(builder.id,
             builder.employeeId,
             builder.startDate,
             builder.endDate,
             builder.approved,
             builder.absenceReason);
    }

    public static Builder builder () {
        return Builder.aZepAbsence();
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {

        @JsonProperty
        private Integer id;
        @JsonProperty("employee_id")
        private String employeeId;
        @JsonProperty
        private String type;
        @JsonProperty("start_date")

        private LocalDate startDate;
        @JsonProperty("end_date")

        private LocalDate endDate;
        @JsonProperty
        private boolean approved;
        @JsonProperty("absenceReason")
        private ZepAbsenceReason absenceReason;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }


        public Builder approved(boolean approved) {
            this.approved = approved;
            return this;
        }

        public Builder absenceReason(ZepAbsenceReason absenceReason) {
            this.absenceReason = absenceReason;
            return this;
        }

        public static Builder aZepAbsence() {
            return new Builder();
        }

        public ZepAbsence build() {
            return new ZepAbsence(this);
        }
    }

}

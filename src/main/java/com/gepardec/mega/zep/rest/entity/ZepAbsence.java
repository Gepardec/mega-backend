package com.gepardec.mega.zep.rest.entity;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

public record ZepAbsence (
    Integer id,
    String employeeId,
    String type,
    LocalDate startDate,
    LocalDate endDate,
    Double hours,
    LocalTime from,
    LocalTime to,
    String note,
    boolean approved,
    String timezone,
    String created,
    String modified,
    ZepAbsenceReason absenceReason) {

    @JsonCreator
    public ZepAbsence(Builder builder) {
        this(builder.id,
             builder.employeeId,
             builder.type,
             builder.startDate,
             builder.endDate,
             builder.hours,
             builder.from,
             builder.to,
             builder.note,
             builder.approved,
             builder.timezone,
             builder.created,
             builder.modified,
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
        private Double hours;
        @JsonProperty
        private LocalTime from;
        @JsonProperty
        private LocalTime to;
        @JsonProperty
        private String note;
        @JsonProperty
        private boolean approved;
        @JsonProperty
        private String timezone;
        @JsonProperty
        private String created;
        @JsonProperty
        private String modified;
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

        public Builder hours(Double hours) {
            this.hours = hours;
            return this;
        }

        public Builder from(LocalTime from) {
            this.from = from;
            return this;
        }

        public Builder to(LocalTime to) {
            this.to = to;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public Builder approved(boolean approved) {
            this.approved = approved;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder created(String created) {
            this.created = created;
            return this;
        }

        public Builder modified(String modified) {
            this.modified = modified;
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

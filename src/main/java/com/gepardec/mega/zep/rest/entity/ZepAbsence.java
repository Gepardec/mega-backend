package com.gepardec.mega.zep.rest.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepAbsence {

    private final Integer id;
    private final String employeeId;
    private final String type;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Double hours;
    private final LocalTime from;
    private final LocalTime to;
    private final String note;
    private final boolean approved;
    private final String timezone;
    private final String created;
    private final String modified;
    private final ZepAbsenceReason absenceReason;


    public ZepAbsence(Builder builder) {
        this.id = builder.id;
        this.employeeId = builder.employeeId;
        this.type = builder.type;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.hours = builder.hours;
        this.from = builder.from;
        this.to = builder.to;
        this.note = builder.note;
        this.approved = builder.approved;
        this.timezone = builder.timezone;
        this.created = builder.created;
        this.modified = builder.modified;
        this.absenceReason = builder.absenceReason;
    }

    public Integer getId() {
        return id;
    }


    public String getEmployeeId() {
        return employeeId;
    }

    

    public String getType() {
        return type;
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    

    public LocalDate getEndDate() {
        return endDate;
    }

    

    public Double getHours() {
        return hours;
    }

    

    public LocalTime getFrom() {
        return from;
    }

    

    public LocalTime getTo() {
        return to;
    }

    

    public String getNote() {
        return note;
    }

    

    public boolean isApproved() {
        return approved;
    }

    

    public String getTimezone() {
        return timezone;
    }

    

    public String getCreated() {
        return created;
    }

    

    public String getModified() {
        return modified;
    }

    

    public ZepAbsenceReason getAbsenceReason() {
        return absenceReason;
    }

    

    public static class Builder {

        private Integer id;
        @JsonProperty("employee_id")
        private String employeeId;
        private String type;
        @JsonProperty("start_date")

        private LocalDate startDate;
        @JsonProperty("end_date")

        private LocalDate endDate;
        private Double hours;
        private LocalTime from;
        private LocalTime to;
        private String note;
        private boolean approved;
        private String timezone;
        private String created;
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

        public ZepAbsence build() {
            return new ZepAbsence(this);
        }
    }

}

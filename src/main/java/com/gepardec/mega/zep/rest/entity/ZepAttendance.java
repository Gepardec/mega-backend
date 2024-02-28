package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ZepAttendance (
    Integer id,
    LocalDate date,
    LocalTime from,
    LocalTime to,
    String employeeId,
    Integer projectId,
    Integer projectTaskId,
    Double duration,
    Integer billable,
    String workLocation,
    Integer workLocationIsProjectRelevant,
    String note,
    String activity,
    String start,
    String destination,
    String vehicle,
    Integer isPrivate,
    String passengers,
    String km,
    String directionOfTravel,
    String ticketId,
    String subtaskId,
    String invoiceItemId,
    LocalDateTime created,
    LocalDateTime modified
) {


    @JsonCreator
    public ZepAttendance(Builder builder) {
        this(builder.id,
             builder.date,
             builder.from,
             builder.to,
             builder.employeeId,
             builder.projectId,
             builder.projectTaskId,
             builder.duration,
             builder.billable,
             builder.workLocation,
             builder.workLocationIsProjectRelevant,
             builder.note,
             builder.activity,
             builder.start,
             builder.destination,
             builder.vehicle,
             builder.isPrivate,
             builder.passengers,
             builder.km,
             builder.directionOfTravel,
             builder.ticketId,
             builder.subtaskId,
             builder.invoiceItemId,
             builder.created,
             builder.modified
        );
    }

    public static Builder builder() {
        return Builder.aZepAttendance();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty
        private Integer id;
        @JsonProperty
        private LocalDate date;
        @JsonProperty
        private LocalTime from;
        @JsonProperty
        private LocalTime to;
        @JsonProperty("employee_id")
        private String employeeId;
        @JsonProperty("project_id")
        private Integer projectId;
        @JsonProperty("project_task_id")
        private Integer projectTaskId;
        @JsonProperty("duration")
        private Double duration;
        @JsonProperty("billable")
        private Integer billable;

        @JsonProperty("work_location")
        private String workLocation;

        @JsonProperty("work_location_is_project_relevant")
        private Integer workLocationIsProjectRelevant;
        @JsonProperty
        private String note;
        @JsonProperty
        private String activity;
        @JsonProperty
        private String start;
        @JsonProperty
        private String destination;
        @JsonProperty
        private String vehicle;
        @JsonProperty("private")
        private Integer isPrivate;
        @JsonProperty
        private String passengers;
        @JsonProperty
        private String km;
        @JsonProperty("direction_of_travel")
        private String directionOfTravel;
        @JsonProperty("ticket_id")
        private String ticketId;
        @JsonProperty("subtask_id")
        private String subtaskId;
        @JsonProperty("invoice_item_id")
        private String invoiceItemId;
        @JsonProperty
        private LocalDateTime created;
        @JsonProperty
        private LocalDateTime modified;


        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
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

        public Builder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder projectId(Integer projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder projectTaskId(Integer projectTaskId) {
            this.projectTaskId = projectTaskId;
            return this;
        }

        public Builder duration(Double duration) {
            this.duration = duration;
            return this;
        }

        public Builder billable(Integer billable) {
            this.billable = billable;
            return this;
        }

        public Builder workLocation(String workLocation) {
            this.workLocation = workLocation;
            return this;
        }

        public Builder workLocationIsProjectRelevant(Integer workLocationIsProjectRelevant) {
            this.workLocationIsProjectRelevant = workLocationIsProjectRelevant;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public Builder activity(String activity) {
            this.activity = activity;
            return this;
        }

        public Builder start(String start) {
            this.start = start;
            return this;
        }

        public Builder destination(String destination) {
            this.destination = destination;
            return this;
        }

        public Builder vehicle(String vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        public Builder isPrivate(Integer isPrivate) {
            this.isPrivate = isPrivate;
            return this;
        }

        public Builder passengers(String passengers) {
            this.passengers = passengers;
            return this;
        }

        public Builder km(String km) {
            this.km = km;
            return this;
        }

        public Builder directionOfTravel(String directionOfTravel) {
            this.directionOfTravel = directionOfTravel;
            return this;
        }

        public Builder ticketId(String ticketId) {
            this.ticketId = ticketId;
            return this;
        }

        public Builder subtaskId(String subtaskId) {
            this.subtaskId = subtaskId;
            return this;
        }

        public Builder invoiceItemId(String invoiceItemId) {
            this.invoiceItemId = invoiceItemId;
            return this;
        }

        public Builder created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public Builder modified(LocalDateTime modified) {
            this.modified = modified;
            return this;
        }

        public ZepAttendance build() {
            return new ZepAttendance(this);
        }

        public static Builder aZepAttendance() {
            return new Builder();
        }
    }
}

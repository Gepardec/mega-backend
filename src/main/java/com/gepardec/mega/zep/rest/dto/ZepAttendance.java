package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
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
    Boolean billable,
    String workLocation,
    Boolean workLocationIsProjectRelevant,
    String activity,
    String vehicle,
    String directionOfTravel
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
             builder.activity,
             builder.vehicle,
             builder.directionOfTravel
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
        private Boolean billable;

        @JsonProperty("work_location")
        private String workLocation;

        @JsonProperty("work_location_is_project_relevant")
        private Boolean workLocationIsProjectRelevant;

        @JsonProperty
        private String activity;

        @JsonProperty
        private String vehicle;

        @JsonProperty("direction_of_travel")
        private String directionOfTravel;

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

        public Builder billable(Boolean billable) {
            this.billable = billable;
            return this;
        }

        public Builder workLocation(String workLocation) {
            this.workLocation = workLocation;
            return this;
        }

        public Builder workLocationIsProjectRelevant(Boolean workLocationIsProjectRelevant) {
            this.workLocationIsProjectRelevant = workLocationIsProjectRelevant;
            return this;
        }



        public Builder activity(String activity) {
            this.activity = activity;
            return this;
        }

        public Builder vehicle(String vehicle) {
            this.vehicle = vehicle;
            return this;
        }


        public Builder directionOfTravel(String directionOfTravel) {
            this.directionOfTravel = directionOfTravel;
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

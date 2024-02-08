package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepAttendance {

    private final Integer id;
    private final LocalDate date;
    private final LocalTime from;
    private final LocalTime to;
    private final String employeeId;
    private final Integer projectId;
    private final Integer projectTaskId;
    private final Double duration;
    private final Integer billable;
    private final String workLocation;
    private final Integer workLocationIsProjectRelevant;
    private final String note;
    private final String activity;
    private final String start;
    private final String destination;
    private final String vehicle;
    private final Integer isPrivate;
    private final String passengers;
    private final String km;
    private final String directionOfTravel;
    private final String ticketId;
    private final String subtaskId;
    private final String invoiceItemId;
    private final LocalDateTime created;
    private final LocalDateTime modified;

    @JsonCreator
    public ZepAttendance(Builder builder) {
        this.id = builder.id;
        this.date = builder.date;
        this.from = builder.from;
        this.to = builder.to;
        this.employeeId = builder.employeeId;
        this.projectId = builder.projectId;
        this.projectTaskId = builder.projectTaskId;
        this.duration = builder.duration;
        this.billable = builder.billable;
        this.workLocation = builder.workLocation;
        this.workLocationIsProjectRelevant = builder.workLocationIsProjectRelevant;
        this.note = builder.note;
        this.activity = builder.activity;
        this.start = builder.start;
        this.destination = builder.destination;
        this.vehicle = builder.vehicle;
        this.isPrivate = builder.isPrivate;
        this.passengers = builder.passengers;
        this.km = builder.km;
        this.directionOfTravel = builder.directionOfTravel;
        this.ticketId = builder.ticketId;
        this.subtaskId = builder.subtaskId;
        this.invoiceItemId = builder.invoiceItemId;
        this.created = builder.created;
        this.modified = builder.modified;
    }

    public Integer getId() {
        return id;
    }

    

    public LocalDate getDate() {
        return date;
    }

    

    public LocalTime getFrom() {
        return from;
    }

    

    public LocalTime getTo() {
        return to;
    }

    

    public String getEmployeeId() {
        return employeeId;
    }

    

    public Integer getProjectId() {
        return projectId;
    }

    

    public Integer getProjectTaskId() {
        return projectTaskId;
    }

    

    public Double getDuration() {
        return duration;
    }

    

    public Integer getBillable() {
        return billable;
    }

    

    public String getWorkLocation() {
        return workLocation;
    }

    

    public Integer getWorkLocationIsProjectRelevant() {
        return workLocationIsProjectRelevant;
    }

    

    public String getNote() {
        return note;
    }

    

    public String getActivity() {
        return activity;
    }

    

    public String getStart() {
        return start;
    }

    

    public String getDestination() {
        return destination;
    }

    

    public String getVehicle() {
        return vehicle;
    }

    

    public Integer getIsPrivate() {
        return isPrivate;
    }

    

    public String getPassengers() {
        return passengers;
    }

    

    public String getKm() {
        return km;
    }

    

    public String getDirectionOfTravel() {
        return directionOfTravel;
    }

    

    public String getTicketId() {
        return ticketId;
    }

    

    public String getSubtaskId() {
        return subtaskId;
    }

    

    public String getInvoiceItemId() {
        return invoiceItemId;
    }

    

    public LocalDateTime getCreated() {
        return created;
    }

    

    public LocalDateTime getModified() {
        return modified;
    }

    public static Builder builder() {
        return Builder.aZepAttendance();
    }

    public static class Builder {
        private Integer id;
        private LocalDate date;
        private LocalTime from;
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
        private String note;
        private String activity;
        private String start;
        private String destination;
        private String vehicle;
        @JsonProperty("private")
        private Integer isPrivate;
        private String passengers;
        private String km;
        @JsonProperty("direction_of_travel")
        private String directionOfTravel;
        @JsonProperty("ticket_id")
        private String ticketId;
        @JsonProperty("subtask_id")
        private String subtaskId;
        @JsonProperty("invoice_item_id")
        private String invoiceItemId;
        private LocalDateTime created;
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

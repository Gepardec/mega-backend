package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ZepAttendance {
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

    public ZepAttendance() {
    }

    public ZepAttendance(Integer id, LocalDate date, LocalTime from, LocalTime to, String employeeId, Integer projectId, Integer projectTaskId, Double duration, Integer billable, String workLocation, Integer workLocationIsProjectRelevant, String note, String activity, String start, String destination, String vehicle, Integer isPrivate, String passengers, String km, String directionOfTravel, String ticketId, String subtaskId, String invoiceItemId, LocalDateTime created, LocalDateTime modified) {
        this.id = id;
        this.date = date;
        this.from = from;
        this.to = to;
        this.employeeId = employeeId;
        this.projectId = projectId;
        this.projectTaskId = projectTaskId;
        this.duration = duration;
        this.billable = billable;
        this.workLocation = workLocation;
        this.workLocationIsProjectRelevant = workLocationIsProjectRelevant;
        this.note = note;
        this.activity = activity;
        this.start = start;
        this.destination = destination;
        this.vehicle = vehicle;
        this.isPrivate = isPrivate;
        this.passengers = passengers;
        this.km = km;
        this.directionOfTravel = directionOfTravel;
        this.ticketId = ticketId;
        this.subtaskId = subtaskId;
        this.invoiceItemId = invoiceItemId;
        this.created = created;
        this.modified = modified;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getFrom() {
        return from;
    }

    public void setFrom(LocalTime from) {
        this.from = from;
    }

    public LocalTime getTo() {
        return to;
    }

    public void setTo(LocalTime to) {
        this.to = to;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getProjectTaskId() {
        return projectTaskId;
    }

    public void setProjectTaskId(Integer projectTaskId) {
        this.projectTaskId = projectTaskId;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Integer getBillable() {
        return billable;
    }

    public void setBillable(Integer billable) {
        this.billable = billable;
    }

    public String getWorkLocation() {
        return workLocation;
    }

    public void setWorkLocation(String workLocation) {
        this.workLocation = workLocation;
    }

    public Integer getWorkLocationIsProjectRelevant() {
        return workLocationIsProjectRelevant;
    }

    public void setWorkLocationIsProjectRelevant(Integer workLocationIsProjectRelevant) {
        this.workLocationIsProjectRelevant = workLocationIsProjectRelevant;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public Integer getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Integer aPrivate) {
        isPrivate = aPrivate;
    }

    public String getPassengers() {
        return passengers;
    }

    public void setPassengers(String passengers) {
        this.passengers = passengers;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getDirectionOfTravel() {
        return directionOfTravel;
    }

    public void setDirectionOfTravel(String directionOfTravel) {
        this.directionOfTravel = directionOfTravel;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getSubtaskId() {
        return subtaskId;
    }

    public void setSubtaskId(String subtaskId) {
        this.subtaskId = subtaskId;
    }

    public String getInvoiceItemId() {
        return invoiceItemId;
    }

    public void setInvoiceItemId(String invoiceItemId) {
        this.invoiceItemId = invoiceItemId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    public static ZepAttendanceBuilder builder() {
        return new ZepAttendanceBuilder();
    }
}

package com.gepardec.mega.zep.rest.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ZepAttendanceBuilder {
    private Integer id;
    private LocalDate date;
    private LocalTime from;
    private LocalTime localTime;
    private String employeeId;
    private Integer projectId;
    private Integer projectTaskId;
    private Double duration;
    private Integer billable;
    private String workLocation;
    private Integer workLocationIsProjectRelevant;
    private String note;
    private String activity;
    private String start;
    private String destination;
    private String vehicle;
    private Boolean isPrivate;
    private String passengers;
    private String km;
    private String directionOfTravel;
    private String ticketId;
    private String subtaskId;
    private String invoiceItemId;
    private LocalDateTime created;
    private LocalDateTime modified;

    public ZepAttendanceBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public ZepAttendanceBuilder date(LocalDate date) {
        this.date = date;
        return this;
    }

    public ZepAttendanceBuilder from(LocalTime from) {
        this.from = from;
        return this;
    }

    public ZepAttendanceBuilder to(LocalTime localTime) {
        this.localTime = localTime;
        return this;
    }

    public ZepAttendanceBuilder employeeId(String employeeId) {
        this.employeeId = employeeId;
        return this;
    }

    public ZepAttendanceBuilder projectId(Integer projectId) {
        this.projectId = projectId;
        return this;
    }

    public ZepAttendanceBuilder projectTaskId(Integer projectTaskId) {
        this.projectTaskId = projectTaskId;
        return this;
    }

    public ZepAttendanceBuilder duration(Double duration) {
        this.duration = duration;
        return this;
    }

    public ZepAttendanceBuilder billable(Integer billable) {
        this.billable = billable;
        return this;
    }

    public ZepAttendanceBuilder workLocation(String workLocation) {
        this.workLocation = workLocation;
        return this;
    }

    public ZepAttendanceBuilder workLocationIsProjectRelevant(int workLocationIsProjectRelevant) {
        this.workLocationIsProjectRelevant = workLocationIsProjectRelevant;
        return this;
    }

    public ZepAttendanceBuilder note(String note) {
        this.note = note;
        return this;
    }

    public ZepAttendanceBuilder activity(String activity) {
        this.activity = activity;
        return this;
    }

    public ZepAttendanceBuilder start(String start) {
        this.start = start;
        return this;
    }

    public ZepAttendanceBuilder destination(String destination) {
        this.destination = destination;
        return this;
    }

    public ZepAttendanceBuilder vehicle(String vehicle) {
        this.vehicle = vehicle;
        return this;
    }

    public ZepAttendanceBuilder isPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
        return this;
    }

    public ZepAttendanceBuilder passengers(String passengers) {
        this.passengers = passengers;
        return this;
    }

    public ZepAttendanceBuilder km(String km) {
        this.km = km;
        return this;
    }

    public ZepAttendanceBuilder directionOfTravel(String directionOfTravel) {
        this.directionOfTravel = directionOfTravel;
        return this;
    }

    public ZepAttendanceBuilder ticketId(String ticketId) {
        this.ticketId = ticketId;
        return this;
    }

    public ZepAttendanceBuilder subtaskId(String subtaskId) {
        this.subtaskId = subtaskId;
        return this;
    }

    public ZepAttendanceBuilder invoiceItemId(String invoiceItemId) {
        this.invoiceItemId = invoiceItemId;
        return this;
    }

    public ZepAttendanceBuilder created(LocalDateTime created) {
        this.created = created;
        return this;
    }

    public ZepAttendanceBuilder modified(LocalDateTime modified) {
        this.modified = modified;
        return this;
    }

    public ZepAttendance build() {
        return new ZepAttendance(id, date, from, localTime, employeeId, projectId, projectTaskId, duration, billable, workLocation, workLocationIsProjectRelevant, note, activity, start, destination, vehicle, isPrivate, passengers, km, directionOfTravel, ticketId, subtaskId, invoiceItemId, created, modified);
    }
}

package com.gepardec.mega.zep.rest.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.zep.rest.entity.builder.ZepAbsenceBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepAbsence {

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

    public ZepAbsence() {
    }

    public ZepAbsence(Integer id, String employeeId, String type, LocalDate startDate, LocalDate endDate, Double hours, LocalTime from, LocalTime to, String note, boolean approved, String timezone, String created, String modified, ZepAbsenceReason absenceReason) {
        this.id = id;
        this.employeeId = employeeId;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hours = hours;
        this.from = from;
        this.to = to;
        this.note = note;
        this.approved = approved;
        this.timezone = timezone;
        this.created = created;
        this.modified = modified;
        this.absenceReason = absenceReason;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getHours() {
        return hours;
    }

    public void setHours(Double hours) {
        this.hours = hours;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public ZepAbsenceReason getAbsenceReason() {
        return absenceReason;
    }

    public void setAbsenceReason(ZepAbsenceReason absenceReason) {
        this.absenceReason = absenceReason;
    }

    public static ZepAbsenceBuilder builder() {
        return new ZepAbsenceBuilder();
    }
}

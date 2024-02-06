package com.gepardec.mega.zep.rest.entity.builder;

import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import com.gepardec.mega.zep.rest.entity.ZepAbsenceReason;

import java.time.LocalDate;
import java.time.LocalTime;

public class ZepAbsenceBuilder {
    private Integer id;
    private String employeeId;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double hours;
    private LocalTime from;
    private LocalTime localTime;
    private String note;
    private boolean approved;
    private String timezone;
    private String created;
    private String modified;
    private ZepAbsenceReason absenceReason;

    public ZepAbsenceBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public ZepAbsenceBuilder employeeId(String employeeId) {
        this.employeeId = employeeId;
        return this;
    }

    public ZepAbsenceBuilder type(String type) {
        this.type = type;
        return this;
    }

    public ZepAbsenceBuilder startDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public ZepAbsenceBuilder endDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public ZepAbsenceBuilder hours(Double hours) {
        this.hours = hours;
        return this;
    }

    public ZepAbsenceBuilder from(LocalTime from) {
        this.from = from;
        return this;
    }

    public ZepAbsenceBuilder to(LocalTime localTime) {
        this.localTime = localTime;
        return this;
    }

    public ZepAbsenceBuilder note(String note) {
        this.note = note;
        return this;
    }

    public ZepAbsenceBuilder approved(boolean approved) {
        this.approved = approved;
        return this;
    }

    public ZepAbsenceBuilder timezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    public ZepAbsenceBuilder created(String created) {
        this.created = created;
        return this;
    }

    public ZepAbsenceBuilder modified(String modified) {
        this.modified = modified;
        return this;
    }

    public ZepAbsenceBuilder absenceReason(ZepAbsenceReason absenceReason) {
        this.absenceReason = absenceReason;
        return this;
    }

    public ZepAbsence build() {
        return new ZepAbsence(id, employeeId, type, startDate, endDate, hours, from, localTime, note, approved, timezone, created, modified, absenceReason);
    }
}

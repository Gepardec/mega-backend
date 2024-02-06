package com.gepardec.mega.zep.rest.entity.builder;

import com.gepardec.mega.zep.rest.entity.ZepAbsenceReason;
import com.gepardec.mega.zep.rest.entity.ZepAbsenceReasonType;

public class ZepAbsenceReasonBuilder {
    private String name;
    private String comment;
    private Byte approve;
    private Byte allowSelfEntry;
    private Byte allowHalfDayHoliday;
    private Byte allowHoursOfVacation;
    private Byte anyoneCanSeeName;
    private String color;
    private ZepAbsenceReasonType type;
    private String clientCalendarCategory;
    private Byte informEmployeeUponApprovalOrRevocation;
    private String created;
    private String modified;

    public ZepAbsenceReasonBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ZepAbsenceReasonBuilder comment(String comment) {
        this.comment = comment;
        return this;
    }

    public ZepAbsenceReasonBuilder approve(Byte approve) {
        this.approve = approve;
        return this;
    }

    public ZepAbsenceReasonBuilder allowSelfEntry(Byte allowSelfEntry) {
        this.allowSelfEntry = allowSelfEntry;
        return this;
    }

    public ZepAbsenceReasonBuilder allowHalfDayHoliday(Byte allowHalfDayHoliday) {
        this.allowHalfDayHoliday = allowHalfDayHoliday;
        return this;
    }

    public ZepAbsenceReasonBuilder allowHoursOfVacation(Byte allowHoursOfVacation) {
        this.allowHoursOfVacation = allowHoursOfVacation;
        return this;
    }

    public ZepAbsenceReasonBuilder anyoneCanSeeName(Byte anyoneCanSeeName) {
        this.anyoneCanSeeName = anyoneCanSeeName;
        return this;
    }

    public ZepAbsenceReasonBuilder color(String color) {
        this.color = color;
        return this;
    }

    public ZepAbsenceReasonBuilder type(ZepAbsenceReasonType type) {
        this.type = type;
        return this;
    }

    public ZepAbsenceReasonBuilder clientCalendarCategory(String clientCalendarCategory) {
        this.clientCalendarCategory = clientCalendarCategory;
        return this;
    }

    public ZepAbsenceReasonBuilder informEmployeeUponApprovalOrRevocation(Byte informEmployeeUponApprovalOrRevocation) {
        this.informEmployeeUponApprovalOrRevocation = informEmployeeUponApprovalOrRevocation;
        return this;
    }

    public ZepAbsenceReasonBuilder created(String created) {
        this.created = created;
        return this;
    }

    public ZepAbsenceReasonBuilder modified(String modified) {
        this.modified = modified;
        return this;
    }

    public ZepAbsenceReason build() {
        return new ZepAbsenceReason(name, comment, approve, allowSelfEntry, allowHalfDayHoliday, allowHoursOfVacation, anyoneCanSeeName, color, type, clientCalendarCategory, informEmployeeUponApprovalOrRevocation, created, modified);
    }
}

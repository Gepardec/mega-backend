package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.zep.rest.entity.builder.ZepAbsenceReasonBuilder;

public class ZepAbsenceReason {

    public String name;
    public String comment;
    public Byte approve;
    @JsonProperty("allow_self_entry")
    public Byte allowSelfEntry;
    @JsonProperty("allow_half_day_holiday")
    public Byte allowHalfDayHoliday;
    @JsonProperty("allow_hours_of_vacation")
    public Byte allowHoursOfVacation;
    @JsonProperty("anyone_can_see_name")
    public Byte anyoneCanSeeName;
    public String color;
    public ZepAbsenceReasonType type;

    @JsonProperty("client_calendar_category")
    public String clientCalendarCategory;

    @JsonProperty("inform_employee_upon_approval_or_revocation")
    public Byte informEmployeeUponApprovalOrRevocation;

    public String created;
    public String modified;

    public String getName() {
        return name;
    }

    public ZepAbsenceReason() {
    }

    public ZepAbsenceReason(String name, String comment, Byte approve, Byte allowSelfEntry, Byte allowHalfDayHoliday, Byte allowHoursOfVacation, Byte anyoneCanSeeName, String color, ZepAbsenceReasonType type, String clientCalendarCategory, Byte informEmployeeUponApprovalOrRevocation, String created, String modified) {
        this.name = name;
        this.comment = comment;
        this.approve = approve;
        this.allowSelfEntry = allowSelfEntry;
        this.allowHalfDayHoliday = allowHalfDayHoliday;
        this.allowHoursOfVacation = allowHoursOfVacation;
        this.anyoneCanSeeName = anyoneCanSeeName;
        this.color = color;
        this.type = type;
        this.clientCalendarCategory = clientCalendarCategory;
        this.informEmployeeUponApprovalOrRevocation = informEmployeeUponApprovalOrRevocation;
        this.created = created;
        this.modified = modified;
    }

    public static ZepAbsenceReasonBuilder builder() {
        return new ZepAbsenceReasonBuilder();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Byte getApprove() {
        return approve;
    }

    public void setApprove(Byte approve) {
        this.approve = approve;
    }

    public Byte getAllowSelfEntry() {
        return allowSelfEntry;
    }

    public void setAllowSelfEntry(Byte allowSelfEntry) {
        this.allowSelfEntry = allowSelfEntry;
    }

    public Byte getAllowHalfDayHoliday() {
        return allowHalfDayHoliday;
    }

    public void setAllowHalfDayHoliday(Byte allowHalfDayHoliday) {
        this.allowHalfDayHoliday = allowHalfDayHoliday;
    }

    public Byte getAllowHoursOfVacation() {
        return allowHoursOfVacation;
    }

    public void setAllowHoursOfVacation(Byte allowHoursOfVacation) {
        this.allowHoursOfVacation = allowHoursOfVacation;
    }

    public Byte getAnyoneCanSeeName() {
        return anyoneCanSeeName;
    }

    public void setAnyoneCanSeeName(Byte anyoneCanSeeName) {
        this.anyoneCanSeeName = anyoneCanSeeName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public ZepAbsenceReasonType getType() {
        return type;
    }

    public void setType(ZepAbsenceReasonType type) {
        this.type = type;
    }

    public String getClientCalendarCategory() {
        return clientCalendarCategory;
    }

    public void setClientCalendarCategory(String clientCalendarCategory) {
        this.clientCalendarCategory = clientCalendarCategory;
    }

    public Byte getInformEmployeeUponApprovalOrRevocation() {
        return informEmployeeUponApprovalOrRevocation;
    }

    public void setInformEmployeeUponApprovalOrRevocation(Byte informEmployeeUponApprovalOrRevocation) {
        this.informEmployeeUponApprovalOrRevocation = informEmployeeUponApprovalOrRevocation;
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
}

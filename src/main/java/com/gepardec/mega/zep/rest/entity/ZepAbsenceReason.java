package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
public class ZepAbsenceReason {

    private final String name;
    private final String comment;
    private final Byte approve;
    private final Byte allowSelfEntry;
    private final Byte allowHalfDayHoliday;
    private final Byte allowHoursOfVacation;
    private final Byte anyoneCanSeeName;
    private final String color;
    private final ZepAbsenceReasonType type;
    private final String clientCalendarCategory;
    private final Byte informEmployeeUponApprovalOrRevocation;
    private final String created;
    private final String modified;

    public ZepAbsenceReason(Builder builder) {
        this.name = builder.name;
        this.comment = builder.comment;
        this.approve = builder.approve;
        this.allowSelfEntry = builder.allowSelfEntry;
        this.allowHalfDayHoliday = builder.allowHalfDayHoliday;
        this.allowHoursOfVacation = builder.allowHoursOfVacation;
        this.anyoneCanSeeName = builder.anyoneCanSeeName;
        this.color = builder.color;
        this.type = builder.type;
        this.clientCalendarCategory = builder.clientCalendarCategory;
        this.informEmployeeUponApprovalOrRevocation = builder.informEmployeeUponApprovalOrRevocation;
        this.created = builder.created;
        this.modified = builder.modified;
    }

    public String getComment() {
        return comment;
    }

    public Byte getApprove() {
        return approve;
    }

    public Byte getAllowSelfEntry() {
        return allowSelfEntry;
    }

    public Byte getAllowHalfDayHoliday() {
        return allowHalfDayHoliday;
    }

    public Byte getAllowHoursOfVacation() {
        return allowHoursOfVacation;
    }

    public Byte getAnyoneCanSeeName() {
        return anyoneCanSeeName;
    }

    public String getColor() {
        return color;
    }

    public ZepAbsenceReasonType getType() {
        return type;
    }

    public String getClientCalendarCategory() {
        return clientCalendarCategory;
    }

    public Byte getInformEmployeeUponApprovalOrRevocation() {
        return informEmployeeUponApprovalOrRevocation;
    }

    public String getCreated() {
        return created;
    }

    public String getModified() {
        return modified;
    }

    public static Builder builder() {
        return Builder.aZepAbsenceReason();
    }

    public String getName() {
        return name;
    }


    public static class Builder {

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


        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder approve(Byte approve) {
            this.approve = approve;
            return this;
        }

        public Builder allowSelfEntry(Byte allowSelfEntry) {
            this.allowSelfEntry = allowSelfEntry;
            return this;
        }

        public Builder allowHalfDayHoliday(Byte allowHalfDayHoliday) {
            this.allowHalfDayHoliday = allowHalfDayHoliday;
            return this;
        }

        public Builder allowHoursOfVacation(Byte allowHoursOfVacation) {
            this.allowHoursOfVacation = allowHoursOfVacation;
            return this;
        }

        public Builder anyoneCanSeeName(Byte anyoneCanSeeName) {
            this.anyoneCanSeeName = anyoneCanSeeName;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Builder type(ZepAbsenceReasonType type) {
            this.type = type;
            return this;
        }

        public Builder clientCalendarCategory(String clientCalendarCategory) {
            this.clientCalendarCategory = clientCalendarCategory;
            return this;
        }

        public Builder informEmployeeUponApprovalOrRevocation(Byte informEmployeeUponApprovalOrRevocation) {
            this.informEmployeeUponApprovalOrRevocation = informEmployeeUponApprovalOrRevocation;
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

        public ZepAbsenceReason build() {
            return new ZepAbsenceReason(this);
        }

        public static Builder aZepAbsenceReason() {
            return new Builder();
        }
    }

}

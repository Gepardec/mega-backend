package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record ZepAbsenceReason (
    String name,
    String comment,
    Byte approve,
    Byte allowSelfEntry,
    Byte allowHalfDayHoliday,
    Byte allowHoursOfVacation,
    Byte anyoneCanSeeName,
    String color,
    ZepAbsenceReasonType type,
    String clientCalendarCategory,
    Byte informEmployeeUponApprovalOrRevocation,
    String created,
    String modified
) {
    @JsonCreator
    public ZepAbsenceReason(Builder builder) {
        this(builder.name,
             builder.comment,
             builder.approve,
             builder.allowSelfEntry,
             builder.allowHalfDayHoliday,
             builder.allowHoursOfVacation,
             builder.anyoneCanSeeName,
             builder.color,
             builder.type,
             builder.clientCalendarCategory,
             builder.informEmployeeUponApprovalOrRevocation,
             builder.created,
             builder.modified
        );
    }

    public static Builder builder() {
        return Builder.aZepAbsenceReason();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        @JsonProperty
        public String name;
        @JsonProperty
        public String comment;
        @JsonProperty
        public Byte approve;
        @JsonProperty("allow_self_entry")
        public Byte allowSelfEntry;
        @JsonProperty("allow_half_day_holiday")
        public Byte allowHalfDayHoliday;
        @JsonProperty("allow_hours_of_vacation")
        public Byte allowHoursOfVacation;
        @JsonProperty("anyone_can_see_name")
        public Byte anyoneCanSeeName;
        @JsonProperty
        public String color;
        @JsonProperty
        public ZepAbsenceReasonType type;

        @JsonProperty("client_calendar_category")
        public String clientCalendarCategory;

        @JsonProperty("inform_employee_upon_approval_or_revocation")
        public Byte informEmployeeUponApprovalOrRevocation;

        @JsonProperty
        public String created;
        @JsonProperty
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

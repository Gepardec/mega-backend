package com.gepardec.mega.domain.model;

import java.time.LocalDate;
import java.util.Map;

public class AbsenceTime {

    private Integer id;
    private String userId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String fromTime;
    private String toTime;
    private String reason;
    private Boolean isHalfADay;
    private Boolean accepted;
    private String comment;
    private String timezone;
    private Boolean suppressMails;
    private String created;
    private String modified;
    private Map<String, String> attributes;

    public AbsenceTime() {
    }

    public AbsenceTime(Integer id, String userId, LocalDate fromDate, LocalDate toDate, String fromTime, String toTime, String reason, Boolean isHalfADay, Boolean accepted, String comment, String timezone, Boolean suppressMails, String created, String modified, Map<String, String> attributes) {
        this.id = id;
        this.userId = userId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.reason = reason;
        this.isHalfADay = isHalfADay;
        this.accepted = accepted;
        this.comment = comment;
        this.timezone = timezone;
        this.suppressMails = suppressMails;
        this.created = created;
        this.modified = modified;
        this.attributes = attributes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getHalfADay() {
        return isHalfADay;
    }

    public void setHalfADay(Boolean halfADay) {
        isHalfADay = halfADay;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Boolean getSuppressMails() {
        return suppressMails;
    }

    public void setSuppressMails(Boolean suppressMails) {
        this.suppressMails = suppressMails;
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

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {


        private Integer id;
        private String userId;
        private LocalDate fromDate;
        private LocalDate toDate;
        private String fromTime;
        private String toTime;
        private String reason;
        private Boolean isHalfADay;
        private Boolean accepted;
        private String comment;
        private String timezone;
        private Boolean suppressMails;
        private String created;
        private String modified;
        private Map<String, String> attributes;

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder fromDate(LocalDate fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public Builder toDate(LocalDate toDate) {
            this.toDate = toDate;
            return this;
        }

        public Builder fromTime(String fromTime) {
            this.fromTime = fromTime;
            return this;
        }

        public Builder toTime(String toTime) {
            this.toTime = toTime;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder isHalfADay(Boolean isHalfADay) {
            this.isHalfADay = isHalfADay;
            return this;
        }

        public Builder accepted(Boolean accepted) {
            this.accepted = accepted;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder suppressMails(Boolean suppressMails) {
            this.suppressMails = suppressMails;
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

        public Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public AbsenceTime build() {
            return new AbsenceTime(id, userId, fromDate, toDate, fromTime, toTime, reason, isHalfADay, accepted, comment, timezone, suppressMails, created, modified, attributes);
        }
    }
}

package com.gepardec.mega.domain.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class ProjectTime {
    private String userId;
    private String duration;
    private Boolean isBillable;

    public ProjectTime() {
    }

    public ProjectTime(String userId, String duration, Boolean isBillable) {
        this.userId = userId;
        this.duration = duration;
        this.isBillable = isBillable;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Boolean getBillable() {
        return isBillable;
    }

    public void setBillable(Boolean billable) {
        isBillable = billable;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String userId;
        private String duration;
        private Boolean isBillable;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder duration(String duration) {
            this.duration = duration;
            return this;
        }

        public Builder isBillable(Boolean isBillable) {
            this.isBillable = isBillable;
            return this;
        }

        public ProjectTime build() {
            return new ProjectTime(userId, duration, isBillable);
        }
    }
}


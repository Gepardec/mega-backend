package com.gepardec.mega.domain.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class ProjectTime {
    private String id;
    private String userId;
    private LocalDate date;
    private String startTime;
    private String endTime;
    private String duration;
    private Boolean isBillable;
    private Boolean isLocationRelevantToProject;
    private String location;
    private String comment;
    private String projectNr;
    private String processNr;
    private String task;
    private String startLocation;
    private String endLocation;
    private Integer km;
    private Integer amountPassengers;
    private String vehicle;
    private Integer ticketNr;
    private String subtaskNr;
    private String travelDirection;
    private Boolean isPrivateVehicle;
    private String created;
    private String modified;
    private Map<String, String> attributes;

    public ProjectTime() {
    }

    public ProjectTime(String id, String userId, LocalDate date, String startTime, String endTime, String duration, Boolean isBillable, Boolean isLocationRelevantToProject, String location, String comment, String projectNr, String processNr, String task, String startLocation, String endLocation, Integer km, Integer amountPassengers, String vehicle, Integer ticketNr, String subtaskNr, String travelDirection, Boolean isPrivateVehicle, String created, String modified, Map<String, String> attributes) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.isBillable = isBillable;
        this.isLocationRelevantToProject = isLocationRelevantToProject;
        this.location = location;
        this.comment = comment;
        this.projectNr = projectNr;
        this.processNr = processNr;
        this.task = task;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.km = km;
        this.amountPassengers = amountPassengers;
        this.vehicle = vehicle;
        this.ticketNr = ticketNr;
        this.subtaskNr = subtaskNr;
        this.travelDirection = travelDirection;
        this.isPrivateVehicle = isPrivateVehicle;
        this.created = created;
        this.modified = modified;
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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

    public Boolean getLocationRelevantToProject() {
        return isLocationRelevantToProject;
    }

    public void setLocationRelevantToProject(Boolean locationRelevantToProject) {
        isLocationRelevantToProject = locationRelevantToProject;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getProjectNr() {
        return projectNr;
    }

    public void setProjectNr(String projectNr) {
        this.projectNr = projectNr;
    }

    public String getProcessNr() {
        return processNr;
    }

    public void setProcessNr(String processNr) {
        this.processNr = processNr;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public Integer getKm() {
        return km;
    }

    public void setKm(Integer km) {
        this.km = km;
    }

    public Integer getAmountPassengers() {
        return amountPassengers;
    }

    public void setAmountPassengers(Integer amountPassengers) {
        this.amountPassengers = amountPassengers;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public Integer getTicketNr() {
        return ticketNr;
    }

    public void setTicketNr(Integer ticketNr) {
        this.ticketNr = ticketNr;
    }

    public String getSubtaskNr() {
        return subtaskNr;
    }

    public void setSubtaskNr(String subtaskNr) {
        this.subtaskNr = subtaskNr;
    }

    public String getTravelDirection() {
        return travelDirection;
    }

    public void setTravelDirection(String travelDirection) {
        this.travelDirection = travelDirection;
    }

    public Boolean getPrivateVehicle() {
        return isPrivateVehicle;
    }

    public void setPrivateVehicle(Boolean privateVehicle) {
        isPrivateVehicle = privateVehicle;
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

        private String id;
        private String userId;
        private LocalDate date;
        private String startTime;
        private String endTime;
        private String duration;
        private Boolean isBillable;
        private Boolean isLocationRelevantToProject;
        private String location;
        private String comment;
        private String projectNr;
        private String processNr;
        private String task;
        private String startLocation;
        private String endLocation;
        private Integer km;
        private Integer amountPassengers;
        private String vehicle;
        private Integer ticketNr;
        private String subtaskNr;
        private String travelDirection;
        private Boolean isPrivateVehicle;
        private String created;
        private String modified;
        private Map<String, String> attributes;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder startTime(String startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(String endTime) {
            this.endTime = endTime;
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

        public Builder isLocationRelevantToProject(Boolean isLocationRelevantToProject) {
            this.isLocationRelevantToProject = isLocationRelevantToProject;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder projectNr(String projectNr) {
            this.projectNr = projectNr;
            return this;
        }

        public Builder processNr(String processNr) {
            this.processNr = processNr;
            return this;
        }

        public Builder task(String task) {
            this.task = task;
            return this;
        }

        public Builder startLocation(String startLocation) {
            this.startLocation = startLocation;
            return this;
        }

        public Builder endLocation(String endLocation) {
            this.endLocation = endLocation;
            return this;
        }

        public Builder km(Integer km) {
            this.km = km;
            return this;
        }

        public Builder amountPassengers(Integer amountPassengers) {
            this.amountPassengers = amountPassengers;
            return this;
        }

        public Builder vehicle(String vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        public Builder ticketNr(Integer ticketNr) {
            this.ticketNr = ticketNr;
            return this;
        }

        public Builder subtaskNr(String subtaskNr) {
            this.subtaskNr = subtaskNr;
            return this;
        }

        public Builder travelDirection(String travelDirection) {
            this.travelDirection = travelDirection;
            return this;
        }

        public Builder isPrivateVehicle(Boolean isPrivateVehicle) {
            this.isPrivateVehicle = isPrivateVehicle;
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

        public ProjectTime build() {
            return new ProjectTime(id, userId, date, startTime, endTime, duration, isBillable, isLocationRelevantToProject, location, comment, projectNr, processNr, task, startLocation, endLocation, km, amountPassengers, vehicle, ticketNr, subtaskNr, travelDirection, isPrivateVehicle, created, modified, attributes);
        }
    }
}


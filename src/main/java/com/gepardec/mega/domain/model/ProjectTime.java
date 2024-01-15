package com.gepardec.mega.domain.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.Map;

@Getter
@Setter
public class ProjectTime {
    private String id;
    private String userId;
    private String date;
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
}


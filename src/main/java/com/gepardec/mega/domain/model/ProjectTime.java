package com.gepardec.mega.domain.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Builder
@Getter
@Setter
public class ProjectTime {
    private String id;
    private String userId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
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


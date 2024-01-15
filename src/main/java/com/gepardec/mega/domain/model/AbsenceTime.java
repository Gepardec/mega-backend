package com.gepardec.mega.domain.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Builder
@Getter @Setter
public class AbsenceTime {

    private Integer id;
    private String userId;
    private String fromTime;
    private String toTime;
    private String reason;
    private Boolean isHalfADay;
    private Boolean accepted;
    private String vonZeit;
    private String bisZeit;
    private String comment;
    private String timezone;
    private Boolean suppressMails;
    private String created;
    private String modified;
    private Map<String, String> attributes;
}

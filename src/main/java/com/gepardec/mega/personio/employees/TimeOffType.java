package com.gepardec.mega.personio.employees;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeOffType {

    private String type;

    private TimeOffTypeAttributes attributes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TimeOffTypeAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(TimeOffTypeAttributes attributes) {
        this.attributes = attributes;
    }
}

package com.gepardec.mega.personio.employees;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeesResponse {

    private String type;

    private PersonioEmployee attributes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PersonioEmployee getAttributes() {
        return attributes;
    }

    public void setAttributes(PersonioEmployee attributes) {
        this.attributes = attributes;
    }
}

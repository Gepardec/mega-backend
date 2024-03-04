package com.gepardec.mega.personio.employees;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeesResponse {

    private String type;

    private PersonioEmployeeDto attributes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PersonioEmployeeDto getAttributes() {
        return attributes;
    }

    public void setAttributes(PersonioEmployeeDto attributes) {
        this.attributes = attributes;
    }
}

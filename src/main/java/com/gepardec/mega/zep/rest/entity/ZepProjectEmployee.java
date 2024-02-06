package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.zep.rest.entity.builder.ZepProjectEmployeeBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepProjectEmployee {

    @JsonProperty("employee_id")
    private String username;
    private boolean lead;

    private ZepProjectEmployeeType type;

    public ZepProjectEmployee() {
    }

    public ZepProjectEmployee(String username, boolean lead, ZepProjectEmployeeType type) {
        this.username = username;
        this.lead = lead;
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLead() {
        return lead;
    }

    public void setLead(boolean lead) {
        this.lead = lead;
    }

    public ZepProjectEmployeeType getType() {
        return type;
    }

    public void setType(ZepProjectEmployeeType type) {
        this.type = type;
    }

    public static ZepProjectEmployeeBuilder builder() {
        return new ZepProjectEmployeeBuilder();
    }
}

package com.gepardec.mega.zep.rest.entity;

public class ZepProjectEmployee {
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

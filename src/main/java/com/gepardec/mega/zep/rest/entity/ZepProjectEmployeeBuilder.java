package com.gepardec.mega.zep.rest.entity;

public class ZepProjectEmployeeBuilder {
    private String username;
    private boolean lead;
    private ZepProjectEmployeeType type;

    public ZepProjectEmployeeBuilder username(String username) {
        this.username = username;
        return this;
    }

    public ZepProjectEmployeeBuilder lead(boolean lead) {
        this.lead = lead;
        return this;
    }

    public ZepProjectEmployeeBuilder type(ZepProjectEmployeeType type) {
        this.type = type;
        return this;
    }

    public ZepProjectEmployee build() {
        return new ZepProjectEmployee(username, lead, type);
    }
}

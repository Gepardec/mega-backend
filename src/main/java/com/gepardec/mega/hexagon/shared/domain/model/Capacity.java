package com.gepardec.mega.hexagon.shared.domain.model;

/**
 * The capacity a caller acts in when reading data. A capacity shapes reads only;
 * it never authorizes or restricts commands.
 */
public enum Capacity {
    EMPLOYEE(Role.EMPLOYEE),
    CONTROLLING(Role.PROJECT_LEAD);

    private final Role requiredRole;

    Capacity(Role requiredRole) {
        this.requiredRole = requiredRole;
    }

    public Role requiredRole() {
        return requiredRole;
    }
}

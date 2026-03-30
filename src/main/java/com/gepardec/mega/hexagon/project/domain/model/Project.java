package com.gepardec.mega.hexagon.project.domain.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Project {

    private final ProjectId id;
    private int zepId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean billable;
    private Set<UUID> leads;

    private Project(ProjectId id, int zepId, String name, LocalDate startDate, LocalDate endDate, boolean billable, Set<UUID> leads) {
        this.id = id;
        this.zepId = zepId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.billable = billable;
        this.leads = new HashSet<>(leads);
    }

    public static Project create(ProjectId id, ZepProjectProfile profile) {
        return new Project(id, profile.zepId(), profile.name(), profile.startDate(), profile.endDate(), profile.billable(), Set.of());
    }

    public static Project reconstitute(ProjectId id, int zepId, String name, LocalDate startDate, LocalDate endDate, boolean billable, Set<UUID> leads) {
        return new Project(id, zepId, name, startDate, endDate, billable, leads);
    }

    public void syncFromZep(ZepProjectProfile profile) {
        this.name = profile.name();
        this.startDate = profile.startDate();
        this.endDate = profile.endDate();
        this.billable = profile.billable();
    }

    public void setLeads(Set<UUID> leads) {
        this.leads = new HashSet<>(leads);
    }

    public ProjectId getId() {
        return id;
    }

    public int getZepId() {
        return zepId;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isBillable() {
        return billable;
    }

    public Set<UUID> getLeads() {
        return Set.copyOf(leads);
    }
}

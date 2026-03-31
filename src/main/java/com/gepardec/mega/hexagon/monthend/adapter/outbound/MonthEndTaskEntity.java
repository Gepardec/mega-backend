package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity(name = "MonthEndTaskEntity")
@Table(name = "monthend_task")
public class MonthEndTaskEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "month_value", nullable = false)
    private LocalDate monthValue;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "subject_employee_id")
    private UUID subjectEmployeeId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "monthend_task_eligible_actor",
            joinColumns = @JoinColumn(name = "task_id")
    )
    @Column(name = "actor_id")
    private Set<UUID> eligibleActorIds = new HashSet<>();

    @Column(name = "completion_policy", nullable = false)
    private String completionPolicy;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "completed_by")
    private UUID completedBy;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getMonthValue() {
        return monthValue;
    }

    public void setMonthValue(LocalDate monthValue) {
        this.monthValue = monthValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getSubjectEmployeeId() {
        return subjectEmployeeId;
    }

    public void setSubjectEmployeeId(UUID subjectEmployeeId) {
        this.subjectEmployeeId = subjectEmployeeId;
    }

    public Set<UUID> getEligibleActorIds() {
        return eligibleActorIds;
    }

    public void setEligibleActorIds(Set<UUID> eligibleActorIds) {
        this.eligibleActorIds = eligibleActorIds == null ? new HashSet<>() : new HashSet<>(eligibleActorIds);
    }

    public String getCompletionPolicy() {
        return completionPolicy;
    }

    public void setCompletionPolicy(String completionPolicy) {
        this.completionPolicy = completionPolicy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(UUID completedBy) {
        this.completedBy = completedBy;
    }
}

package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MonthEndTaskType type;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MonthEndTaskStatus status;

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

    public MonthEndTaskType getType() {
        return type;
    }

    public void setType(MonthEndTaskType type) {
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

    public MonthEndTaskStatus getStatus() {
        return status;
    }

    public void setStatus(MonthEndTaskStatus status) {
        this.status = status;
    }

    public UUID getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(UUID completedBy) {
        this.completedBy = completedBy;
    }
}

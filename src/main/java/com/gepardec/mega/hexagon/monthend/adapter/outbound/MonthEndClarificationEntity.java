package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationStatus;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity(name = "MonthEndClarificationEntity")
@Table(name = "monthend_clarification")
public class MonthEndClarificationEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "month_value", nullable = false)
    private LocalDate monthValue;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "subject_employee_id", nullable = false)
    private UUID subjectEmployeeId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "creator_side", nullable = false)
    private MonthEndClarificationSide creatorSide;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "monthend_clarification_eligible_lead",
            joinColumns = @JoinColumn(name = "clarification_id")
    )
    @Column(name = "lead_id")
    private Set<UUID> eligibleProjectLeadIds = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MonthEndClarificationStatus status;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "resolution_note")
    private String resolutionNote;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "last_modified_at", nullable = false)
    private Instant lastModifiedAt;

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

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public MonthEndClarificationSide getCreatorSide() {
        return creatorSide;
    }

    public void setCreatorSide(MonthEndClarificationSide creatorSide) {
        this.creatorSide = creatorSide;
    }

    public Set<UUID> getEligibleProjectLeadIds() {
        return eligibleProjectLeadIds;
    }

    public void setEligibleProjectLeadIds(Set<UUID> eligibleProjectLeadIds) {
        this.eligibleProjectLeadIds = eligibleProjectLeadIds == null ? new HashSet<>() : new HashSet<>(eligibleProjectLeadIds);
    }

    public MonthEndClarificationStatus getStatus() {
        return status;
    }

    public void setStatus(MonthEndClarificationStatus status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    public UUID getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(UUID resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(Instant lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }
}

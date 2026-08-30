package com.gepardec.mega.hexagon.recognition.adapter.outbound;

import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionCategory;
import com.gepardec.mega.hexagon.recognition.domain.model.RecognitionEntryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "RecognitionEntryEntity")
@Table(name = "recognition_entry")
public class RecognitionEntryEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "message", nullable = false)
    @Length(max = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private RecognitionCategory category;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "submitted_by")
    private UUID submittedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecognitionEntryStatus status;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RecognitionCategory getCategory() {
        return category;
    }

    public void setCategory(RecognitionCategory category) {
        this.category = category;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public UUID getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(UUID submittedBy) {
        this.submittedBy = submittedBy;
    }

    public RecognitionEntryStatus getStatus() {
        return status;
    }

    public void setStatus(RecognitionEntryStatus status) {
        this.status = status;
    }
}

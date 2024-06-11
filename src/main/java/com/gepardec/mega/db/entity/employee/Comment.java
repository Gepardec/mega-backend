package com.gepardec.mega.db.entity.employee;

import com.gepardec.mega.domain.model.SourceSystem;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "comment")
@NamedQueries({
        @NamedQuery(name = "Comment.findAllCommentsBetweenStartDateAndEndDateAndAllOpenCommentsBeforeStartDateForEmail", query = "SELECT c FROM Comment c WHERE c.stepEntry.owner.email = :email AND ((c.stepEntry.date BETWEEN :start AND :end) OR (c.stepEntry.date < :start AND c.employeeState = :state))"),
        @NamedQuery(name = "Comment.findAllCommentsBetweenStartAndEndDateForEmail", query = "SELECT c FROM Comment c WHERE c.stepEntry.owner.email = :email AND ((c.stepEntry.date BETWEEN :start AND :end) OR (c.stepEntry.date < :start))")
})
public class Comment {
    static final int MAX_MESSAGE_LENGTH = 500;

    @Id
    @Column(name = "id", insertable = false, updatable = false)
    @GeneratedValue(generator = "commentIdGenerator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "commentIdGenerator", sequenceName = "sequence_comment_id", allocationSize = 1)
    private Long id;

    /**
     * The creation date of the comment
     */
    @NotNull
    @Column(name = "creation_date", updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime creationDate;

    /**
     * The updated date of the comment
     */
    @NotNull
    @Column(name = "update_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedDate;

    /**
     * The message of the comment for the related step entry
     */
    @NotNull
    @Size(max = MAX_MESSAGE_LENGTH)
    @Column(name = "message", length = MAX_MESSAGE_LENGTH)
    private String message;

    /**
     * The state of the comment
     *
     * @see EmployeeState
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private EmployeeState employeeState;

    /**
     * The source sysstem of the comment
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_system")
    private SourceSystem sourceSystem;

    /**
     * The step entry the comment is for
     *
     * @see StepEntry
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_entry_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_step_entry", value = ConstraintMode.CONSTRAINT))
    private StepEntry stepEntry;

    @PrePersist
    void onPersist() {
        employeeState = EmployeeState.OPEN;
        creationDate = updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public StepEntry getStepEntry() {
        return stepEntry;
    }

    public void setStepEntry(StepEntry stepEntry) {
        this.stepEntry = stepEntry;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = shortenTooLongMessage(message);
    }

    private static String shortenTooLongMessage(String message) {
        if (message == null) {
            return null;
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return message.substring(0, MAX_MESSAGE_LENGTH - 3) + "...";
        }
        return message;
    }

    public EmployeeState getState() {
        return employeeState;
    }

    public void setState(EmployeeState employeeState) {
        this.employeeState = employeeState;
    }

    public SourceSystem getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(SourceSystem sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Comment comment = (Comment) o;
        return (id != null) ? Objects.equals(id, comment.id) : super.equals(o);
    }

    @Override
    public int hashCode() {
        return (id != null) ? Objects.hash(id) : super.hashCode();
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", updatedDate=" + updatedDate +
                ", message='" + message + '\'' +
                ", state=" + employeeState +
                ", stepEntry=" + stepEntry +
                '}';
    }
}

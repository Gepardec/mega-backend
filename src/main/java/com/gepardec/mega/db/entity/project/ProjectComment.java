package com.gepardec.mega.db.entity.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "project_comment")
@NamedQuery(
        name = "ProjectComment.findByProjectNameAndEntryDateBetween",
        query = "SELECT c FROM ProjectComment c WHERE c.project.name = :projectName AND (c.date BETWEEN :start AND :end)"
)
public class ProjectComment {

    @Id
    @Column(name = "id", insertable = false, updatable = false)
    @GeneratedValue(generator = "projectCommentIdGenerator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "projectCommentIdGenerator", sequenceName = "sequence_project_comment_id", allocationSize = 1)
    private Long id;

    /**
     * The global comment for the project
     */
    @Column(name = "comment")
    @Length(max = 500)
    private String comment;

    /**
     * The date (=month) the comment is for
     */
    @NotNull
    @Column(name = "entry_date", updatable = false, columnDefinition = "DATE")
    private LocalDate date;

    /**
     * The creation date of the comment
     */
    @NotNull
    @Column(name = "creation_date", updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime creationDate;

    /**
     * The update date of the comment
     */
    @NotNull
    @Column(name = "update_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedDate;

    /**
     * The related project of the comment
     *
     * @see Project
     */
    @ManyToOne(targetEntity = Project.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;

    @PrePersist
    void onPersist() {
        creationDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectComment that = (ProjectComment) o;
        return id.equals(that.id) && comment.equals(that.comment) && date.equals(that.date) && creationDate.equals(that.creationDate) && updatedDate.equals(that.updatedDate) && project.equals(that.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, comment, date, creationDate, updatedDate, project);
    }
}

package com.gepardec.mega.db.entity.employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "premature_employee_check", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "for_month"}))
@NamedQueries({
        @NamedQuery(name = "PrematureEmployeeCheck.findByEmailAndMonth", query = "select p from PrematureEmployeeCheckEntity p where p.user.email = :email and p.forMonth = :forMonth"),
        @NamedQuery(name = "PrematureEmployeeCheck.findAllByMonth", query = "select p from PrematureEmployeeCheckEntity p where p.forMonth = :forMonth"),
        @NamedQuery(name = "PrematureEmployeeCheck.deleteAllByMonth", query = "delete from PrematureEmployeeCheckEntity p  where p.forMonth = :forMonth"),
})
public class PrematureEmployeeCheckEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PrematureEmployeeCheck_GEN")
    @SequenceGenerator(name = "PrematureEmployeeCheck_GEN", sequenceName = "PrematureEmployeeCheck_SEQ")
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The user, whose check is prematurely done
     */
    @OneToOne(optional = false, orphanRemoval = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Which month has been prematurely checked
     */
    @Column(name = "for_month", nullable = false, columnDefinition = "DATE")
    private LocalDate forMonth;

    /*
     * Reason why the employee can't check in time or other notifications for OM/PL
     */
    @Column(name = "reason", nullable = true)
    private String reason;


    /**
     * The creation date of the step entry
     */
    @NotNull
    @Column(name = "creation_date", updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime creationDate;

    /**
     * The update date of the step entry
     */
    @NotNull
    @Column(name = "update_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedDate;

    /**
     * The state of the premature check (Done, In Progress). Is a requirement.
     */
    @NotNull
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private PrematureEmployeeCheckState state;


    @PrePersist
    void onPersist() {
        creationDate = updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedDate = LocalDateTime.now();
    }


    public void setUser(User user) {
        this.user = user;
    }

    public void setForMonth(LocalDate forMonth) {
        this.forMonth = forMonth;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public PrematureEmployeeCheckState getState() {
        return state;
    }

    public void setState(PrematureEmployeeCheckState state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getForMonth() {
        return forMonth;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }
}

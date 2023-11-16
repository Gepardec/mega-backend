package com.gepardec.mega.db.entity.employee;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "premature_employee_check")
public class PrematureEmployeeCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PrematureEmployeeCheck_GEN")
    @SequenceGenerator(name = "PrematureEmployeeCheck_GEN", sequenceName = "PrematureEmployeeCheck_SEQ")
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The step-entry, which is prematurely checked.
     */
    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "step_entry_id", nullable = false)
    private StepEntry stepEntry;


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

    @PrePersist
    void onPersist() {
        creationDate = updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedDate = LocalDateTime.now();
    }


    public void setStepEntry(StepEntry stepEntry) {
        this.stepEntry = stepEntry;
    }

}

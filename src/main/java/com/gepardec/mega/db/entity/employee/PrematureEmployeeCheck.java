package com.gepardec.mega.db.entity.employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "premature_employee_check", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "for_month"}))
@NamedQueries({
        @NamedQuery(name = "PrematureEmployeeCheck.findByEmail", query = "select p from PrematureEmployeeCheck p where p.user.email = :email"),
})
public class PrematureEmployeeCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PrematureEmployeeCheck_GEN")
    @SequenceGenerator(name = "PrematureEmployeeCheck_GEN", sequenceName = "PrematureEmployeeCheck_SEQ")
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The user, whose check is prematurely done
     */
    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Which month has been prematurely checked
     */
    @Column(name = "for_month", nullable = false, columnDefinition = "DATE")
    private LocalDate forMonth;


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


    public void setUser(User user) {
        this.user = user;
    }

    public void setForMonth(LocalDate forMonth) {
        this.forMonth = forMonth;
    }

    public void setId(Long id){
        this.id = id;
    }

}

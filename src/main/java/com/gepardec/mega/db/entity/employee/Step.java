package com.gepardec.mega.db.entity.employee;

import com.gepardec.mega.domain.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "step",
        uniqueConstraints = {
                @UniqueConstraint(name = "uidx_ordinal", columnNames = {"ordinal"})
        }
)
public class Step {

    /**
     * The related step entries whereby each step entry
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "step")
    @MapKey(name = "date")
    public Map<LocalDate, StepEntry> stepEntries = HashMap.newHashMap(0);

    @Id
    @Column(name = "id", insertable = false, updatable = false)
    @GeneratedValue(generator = "stepIdGenerator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "stepIdGenerator", sequenceName = "sequence_step_id", allocationSize = 1)
    private Long id;

    /**
     * The name of the step
     */
    @NotNull
    @Column(name = "name")
    @Length(min = 1, max = 255)
    private String name;

    /**
     * The role who is responsible for the step completion state
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    /**
     * The ordinal defining the order of the steps
     */
    @NotNull
    @Min(1)
    @Column(name = "ordinal", unique = true)
    private Integer ordinal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Map<LocalDate, StepEntry> getStepEntries() {
        return stepEntries;
    }

    public void setStepEntries(Map<LocalDate, StepEntry> stepEntries) {
        this.stepEntries = stepEntries;
    }

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Step step = (Step) o;
        return (id != null) ? Objects.equals(id, step.id) : super.equals(o);
    }

    @Override
    public int hashCode() {
        return (id != null) ? Objects.hash(id) : super.hashCode();
    }

    @Override
    public String toString() {
        return "Step{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", ordinal=" + ordinal +
                ", stepEntries=" + stepEntries +
                '}';
    }
}

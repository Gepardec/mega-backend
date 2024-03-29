package com.gepardec.mega.db.entity.enterprise;

import com.gepardec.mega.db.entity.common.State;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "enterprise_entry")
@NamedQuery(
        name = "EnterpriseEntry.findByDate",
        query = "SELECT e FROM EnterpriseEntry e WHERE e.date BETWEEN :start AND :end"
)
public class EnterpriseEntry {

    @Id
    @Column(name = "id", insertable = false, updatable = false)
    @GeneratedValue(generator = "enterpriseEntryIdGenerator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "enterpriseEntryIdGenerator", sequenceName = "sequence_enterprise_entry_id", allocationSize = 1)
    private Long id;

    @NotNull
    @Column(name = "zep_times_released")
    private State zepTimesReleased;

    @NotNull
    @Column(name = "chargeability_external_employees_recorded")
    private State chargeabilityExternalEmployeesRecorded;

    @NotNull
    @Column(name = "payroll_accounting_sent")
    private State payrollAccountingSent;

    @NotNull
    @Column(name = "zep_monthly_report_done")
    private State zepMonthlyReportDone;

    /**
     * The date (=month) the enterprise entry is for
     */
    @NotNull
    @Column(name = "entry_date", updatable = false, columnDefinition = "DATE")
    private LocalDate date;

    /**
     * The creation date of the enterprise entry
     */
    @NotNull
    @Column(name = "creation_date", updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime creationDate;

    @PrePersist
    void onPersist() {
        creationDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public State getZepTimesReleased() {
        return zepTimesReleased;
    }

    public void setZepTimesReleased(State zepTimesReleased) {
        this.zepTimesReleased = zepTimesReleased;
    }

    public State getChargeabilityExternalEmployeesRecorded() {
        return chargeabilityExternalEmployeesRecorded;
    }

    public void setChargeabilityExternalEmployeesRecorded(State chargeabilityExternalEmployeesRecorded) {
        this.chargeabilityExternalEmployeesRecorded = chargeabilityExternalEmployeesRecorded;
    }

    public State getPayrollAccountingSent() {
        return payrollAccountingSent;
    }

    public void setPayrollAccountingSent(State payrollAccountingSent) {
        this.payrollAccountingSent = payrollAccountingSent;
    }

    public State getZepMonthlyReportDone() {
        return zepMonthlyReportDone;
    }

    public void setZepMonthlyReportDone(State zepMonthlyReportDone) {
        this.zepMonthlyReportDone = zepMonthlyReportDone;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EnterpriseEntry that = (EnterpriseEntry) o;

        if (!id.equals(that.id)) {
            return false;
        }
        if (zepTimesReleased != that.zepTimesReleased) {
            return false;
        }
        if (chargeabilityExternalEmployeesRecorded != that.chargeabilityExternalEmployeesRecorded) {
            return false;
        }
        if (payrollAccountingSent != that.payrollAccountingSent) {
            return false;
        }
        if (zepMonthlyReportDone != that.zepMonthlyReportDone) {
            return false;
        }
        if (!date.equals(that.date)) {
            return false;
        }
        return creationDate.equals(that.creationDate);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + zepTimesReleased.hashCode();
        result = 31 * result + chargeabilityExternalEmployeesRecorded.hashCode();
        result = 31 * result + payrollAccountingSent.hashCode();
        result = 31 * result + zepMonthlyReportDone.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + creationDate.hashCode();
        return result;
    }
}

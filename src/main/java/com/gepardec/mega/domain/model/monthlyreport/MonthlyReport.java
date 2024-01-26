package com.gepardec.mega.domain.model.monthlyreport;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.rest.model.MappedTimeWarningDTO;
import com.gepardec.mega.rest.model.PmProgressDto;

import java.time.LocalDate;
import java.util.List;

public class MonthlyReport {
    private final Employee employee;

    private final LocalDate initialDate;

    private final List<MappedTimeWarningDTO> timeWarnings;

    private final List<JourneyWarning> journeyWarnings;

    private final List<Comment> comments;

    private final EmployeeState employeeCheckState;

    private final String employeeCheckStateReason;

    private final EmployeeState internalCheckState;

    private final List<PmProgressDto> employeeProgresses;

    private final boolean otherChecksDone;

    private final int vacationDays;

    private final int homeofficeDays;

    private final int compensatoryDays;

    private final int nursingDays;

    private final int maternityLeaveDays;

    private final int externalTrainingDays;

    private final int conferenceDays;

    private final int maternityProtectionDays;

    private final int fatherMonthDays;

    private final int paidSpecialLeaveDays;

    private final int nonPaidVacationDays;

    private final int paidSickLeave;

    private final double vacationDayBalance;

    private final String billableTime;

    private final String totalWorkingTime;

    private final double overtime;

    private final boolean hasPrematureEmployeeCheck;

    private MonthlyReport(Builder builder) {
        this.employee = builder.employee;
        this.initialDate = builder.initialDate;
        this.timeWarnings = builder.timeWarnings;
        this.journeyWarnings = builder.journeyWarnings;
        this.comments = builder.comments;
        this.employeeCheckState = builder.employeeCheckState;
        this.employeeCheckStateReason = builder.employeeCheckStateReason;
        this.internalCheckState = builder.internalCheckState;
        this.employeeProgresses = builder.employeeProgresses;
        this.otherChecksDone = builder.otherChecksDone;
        this.vacationDays = builder.vacationDays;
        this.homeofficeDays = builder.homeofficeDays;
        this.compensatoryDays = builder.compensatoryDays;
        this.nursingDays = builder.nursingDays;
        this.maternityLeaveDays = builder.maternityLeaveDays;
        this.externalTrainingDays = builder.externalTrainingDays;
        this.conferenceDays = builder.conferenceDays;
        this.maternityProtectionDays = builder.maternityProtectionDays;
        this.fatherMonthDays = builder.fatherMonthDays;
        this.paidSpecialLeaveDays = builder.paidSpecialLeaveDays;
        this.nonPaidVacationDays = builder.nonPaidVacationDays;
        this.paidSickLeave = builder.paidSickLeave;
        this.vacationDayBalance = builder.vacationDayBalance;
        this.billableTime = builder.billableTime;
        this.totalWorkingTime = builder.totalWorkingTime;
        this.overtime = builder.overtime;
        this.hasPrematureEmployeeCheck = builder.hasPrematureEmployeeCheck;
    }


    public static Builder builder() {
        return Builder.aMonthlyReport();
    }

    public Employee getEmployee() {
        return employee;
    }

    public LocalDate getInitialDate() {
        return initialDate;
    }

    public List<MappedTimeWarningDTO> getTimeWarnings() {
        return timeWarnings;
    }

    public List<JourneyWarning> getJourneyWarnings() {
        return journeyWarnings;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public EmployeeState getEmployeeCheckState() {
        return employeeCheckState;
    }


    public String getEmployeeCheckStateReason() {
        return employeeCheckStateReason;
    }


    public EmployeeState getInternalCheckState() {
        return internalCheckState;
    }


    public List<PmProgressDto> getEmployeeProgresses() {
        return employeeProgresses;
    }


    public boolean isOtherChecksDone() {
        return otherChecksDone;
    }

    public int getVacationDays() {
        return vacationDays;
    }

    public int getHomeofficeDays() {
        return homeofficeDays;
    }

    public int getCompensatoryDays() {
        return compensatoryDays;
    }

    public int getNursingDays() {
        return nursingDays;
    }

    public int getMaternityLeaveDays() {
        return maternityLeaveDays;
    }

    public int getExternalTrainingDays() {
        return externalTrainingDays;
    }

    public int getConferenceDays() {
        return conferenceDays;
    }

    public int getMaternityProtectionDays() {
        return maternityProtectionDays;
    }

    public int getFatherMonthDays() {
        return fatherMonthDays;
    }

    public int getPaidSpecialLeaveDays() {
        return paidSpecialLeaveDays;
    }

    public int getNonPaidVacationDays() {
        return nonPaidVacationDays;
    }

    public int getPaidSickLeave() {
        return paidSickLeave;
    }

    public double getVacationDayBalance() {
        return vacationDayBalance;
    }

    public String getBillableTime() {
        return billableTime;
    }

    public String getTotalWorkingTime() {
        return totalWorkingTime;
    }

    public double getOvertime() {
        return overtime;
    }

    public boolean isHasPrematureEmployeeCheck() {
        return hasPrematureEmployeeCheck;
    }

    public static final class Builder {
        private Employee employee;
        private LocalDate initialDate;
        private List<MappedTimeWarningDTO> timeWarnings;
        private List<JourneyWarning> journeyWarnings;
        private List<Comment> comments;
        private EmployeeState employeeCheckState;
        private String employeeCheckStateReason;
        private EmployeeState internalCheckState;
        private List<PmProgressDto> employeeProgresses;
        private boolean otherChecksDone;
        private int vacationDays;
        private int homeofficeDays;
        private int compensatoryDays;
        private int nursingDays;
        private int maternityLeaveDays;
        private int externalTrainingDays;
        private int conferenceDays;
        private int maternityProtectionDays;
        private int fatherMonthDays;
        private int paidSpecialLeaveDays;
        private int nonPaidVacationDays;
        private int paidSickLeave;
        private double vacationDayBalance;
        private String billableTime;
        private String totalWorkingTime;
        private double overtime;
        private boolean hasPrematureEmployeeCheck;

        private Builder() {
        }

        public static Builder aMonthlyReport() {
            return new Builder();
        }

        public Builder employee(Employee employee) {
            this.employee = employee;
            return this;
        }

        public Builder initialDate(LocalDate initialDate) {
            this.initialDate = initialDate;
            return this;
        }

        public Builder timeWarnings(List<MappedTimeWarningDTO> timeWarnings) {
            this.timeWarnings = timeWarnings;
            return this;
        }

        public Builder journeyWarnings(List<JourneyWarning> journeyWarnings) {
            this.journeyWarnings = journeyWarnings;
            return this;
        }

        public Builder comments(List<Comment> comments) {
            this.comments = comments;
            return this;
        }

        public Builder employeeCheckState(EmployeeState employeeCheckState) {
            this.employeeCheckState = employeeCheckState;
            return this;
        }

        public Builder employeeCheckStateReason(String employeeCheckStateReason) {
            this.employeeCheckStateReason = employeeCheckStateReason;
            return this;
        }

        public Builder internalCheckState(EmployeeState internalCheckState) {
            this.internalCheckState = internalCheckState;
            return this;
        }

        public Builder employeeProgresses(List<PmProgressDto> employeeProgresses) {
            this.employeeProgresses = employeeProgresses;
            return this;
        }

        public Builder otherChecksDone(boolean otherChecksDone) {
            this.otherChecksDone = otherChecksDone;
            return this;
        }

        public Builder vacationDays(int vacationDays) {
            this.vacationDays = vacationDays;
            return this;
        }

        public Builder homeofficeDays(int homeofficeDays) {
            this.homeofficeDays = homeofficeDays;
            return this;
        }

        public Builder compensatoryDays(int compensatoryDays) {
            this.compensatoryDays = compensatoryDays;
            return this;
        }

        public Builder nursingDays(int nursingDays) {
            this.nursingDays = nursingDays;
            return this;
        }

        public Builder maternityLeaveDays(int maternityLeaveDays) {
            this.maternityLeaveDays = maternityLeaveDays;
            return this;
        }

        public Builder externalTrainingDays(int externalTrainingDays) {
            this.externalTrainingDays = externalTrainingDays;
            return this;
        }

        public Builder conferenceDays(int conferenceDays) {
            this.conferenceDays = conferenceDays;
            return this;
        }

        public Builder maternityProtectionDays(int maternityProtectionDays) {
            this.maternityProtectionDays = maternityProtectionDays;
            return this;
        }

        public Builder fatherMonthDays(int fatherMonthDays) {
            this.fatherMonthDays = fatherMonthDays;
            return this;
        }

        public Builder paidSpecialLeaveDays(int paidSpecialLeaveDays) {
            this.paidSpecialLeaveDays = paidSpecialLeaveDays;
            return this;
        }

        public Builder nonPaidVacationDays(int nonPaidVacationDays) {
            this.nonPaidVacationDays = nonPaidVacationDays;
            return this;
        }

        public Builder paidSickLeave(int paidSickLeave) {
            this.paidSickLeave = paidSickLeave;
            return this;
        }

        public Builder vacationDayBalance(double vacationDayBalance) {
            this.vacationDayBalance = vacationDayBalance;
            return this;
        }

        public Builder billableTime(String billableTime) {
            this.billableTime = billableTime;
            return this;
        }

        public Builder totalWorkingTime(String totalWorkingTime) {
            this.totalWorkingTime = totalWorkingTime;
            return this;
        }

        public Builder overtime(double overtime) {
            this.overtime = overtime;
            return this;
        }

        public Builder hasPrematureEmployeeCheck(boolean hasPrematureEmployeeCheck) {
            this.hasPrematureEmployeeCheck = hasPrematureEmployeeCheck;
            return this;
        }

        public MonthlyReport build() {
            return new MonthlyReport(this);
        }
    }
}

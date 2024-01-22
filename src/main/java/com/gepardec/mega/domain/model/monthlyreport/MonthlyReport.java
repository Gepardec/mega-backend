package com.gepardec.mega.domain.model.monthlyreport;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.rest.model.MappedTimeWarningDTO;
import com.gepardec.mega.rest.model.PmProgressDto;

import java.time.LocalDate;
import java.util.List;

public class MonthlyReport {
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

    public MonthlyReport() {
    }

    public MonthlyReport(Employee employee, LocalDate initialDate, List<MappedTimeWarningDTO> timeWarnings, List<JourneyWarning> journeyWarnings, List<Comment> comments, EmployeeState employeeCheckState, String employeeCheckStateReason, EmployeeState internalCheckState, List<PmProgressDto> employeeProgresses, boolean otherChecksDone, int vacationDays, int homeofficeDays, int compensatoryDays, int nursingDays, int maternityLeaveDays, int externalTrainingDays, int conferenceDays, int maternityProtectionDays, int fatherMonthDays, int paidSpecialLeaveDays, int nonPaidVacationDays, int paidSickLeave, double vacationDayBalance, String billableTime, String totalWorkingTime, double overtime, boolean hasPrematureEmployeeCheck) {
        this.employee = employee;
        this.initialDate = initialDate;
        this.timeWarnings = timeWarnings;
        this.journeyWarnings = journeyWarnings;
        this.comments = comments;
        this.employeeCheckState = employeeCheckState;
        this.employeeCheckStateReason = employeeCheckStateReason;
        this.internalCheckState = internalCheckState;
        this.employeeProgresses = employeeProgresses;
        this.otherChecksDone = otherChecksDone;
        this.vacationDays = vacationDays;
        this.homeofficeDays = homeofficeDays;
        this.compensatoryDays = compensatoryDays;
        this.nursingDays = nursingDays;
        this.maternityLeaveDays = maternityLeaveDays;
        this.externalTrainingDays = externalTrainingDays;
        this.conferenceDays = conferenceDays;
        this.maternityProtectionDays = maternityProtectionDays;
        this.fatherMonthDays = fatherMonthDays;
        this.paidSpecialLeaveDays = paidSpecialLeaveDays;
        this.nonPaidVacationDays = nonPaidVacationDays;
        this.paidSickLeave = paidSickLeave;
        this.vacationDayBalance = vacationDayBalance;
        this.billableTime = billableTime;
        this.totalWorkingTime = totalWorkingTime;
        this.overtime = overtime;
        this.hasPrematureEmployeeCheck = hasPrematureEmployeeCheck;
    }

    public static MonthlyReportBuilder builder() {
        return MonthlyReportBuilder.aMonthlyReport();
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public LocalDate getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(LocalDate initialDate) {
        this.initialDate = initialDate;
    }

    public List<MappedTimeWarningDTO> getTimeWarnings() {
        return timeWarnings;
    }

    public void setTimeWarnings(List<MappedTimeWarningDTO> timeWarnings) {
        this.timeWarnings = timeWarnings;
    }

    public List<JourneyWarning> getJourneyWarnings() {
        return journeyWarnings;
    }

    public void setJourneyWarnings(List<JourneyWarning> journeyWarnings) {
        this.journeyWarnings = journeyWarnings;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public EmployeeState getEmployeeCheckState() {
        return employeeCheckState;
    }

    public void setEmployeeCheckState(EmployeeState employeeCheckState) {
        this.employeeCheckState = employeeCheckState;
    }

    public String getEmployeeCheckStateReason() {
        return employeeCheckStateReason;
    }

    public void setEmployeeCheckStateReason(String employeeCheckStateReason) {
        this.employeeCheckStateReason = employeeCheckStateReason;
    }

    public EmployeeState getInternalCheckState() {
        return internalCheckState;
    }

    public void setInternalCheckState(EmployeeState internalCheckState) {
        this.internalCheckState = internalCheckState;
    }

    public List<PmProgressDto> getEmployeeProgresses() {
        return employeeProgresses;
    }

    public void setEmployeeProgresses(List<PmProgressDto> employeeProgresses) {
        this.employeeProgresses = employeeProgresses;
    }

    public boolean isOtherChecksDone() {
        return otherChecksDone;
    }

    public void setOtherChecksDone(boolean otherChecksDone) {
        this.otherChecksDone = otherChecksDone;
    }

    public int getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(int vacationDays) {
        this.vacationDays = vacationDays;
    }

    public int getHomeofficeDays() {
        return homeofficeDays;
    }

    public void setHomeofficeDays(int homeofficeDays) {
        this.homeofficeDays = homeofficeDays;
    }

    public int getCompensatoryDays() {
        return compensatoryDays;
    }

    public void setCompensatoryDays(int compensatoryDays) {
        this.compensatoryDays = compensatoryDays;
    }

    public int getNursingDays() {
        return nursingDays;
    }

    public void setNursingDays(int nursingDays) {
        this.nursingDays = nursingDays;
    }

    public int getMaternityLeaveDays() {
        return maternityLeaveDays;
    }

    public void setMaternityLeaveDays(int maternityLeaveDays) {
        this.maternityLeaveDays = maternityLeaveDays;
    }

    public int getExternalTrainingDays() {
        return externalTrainingDays;
    }

    public void setExternalTrainingDays(int externalTrainingDays) {
        this.externalTrainingDays = externalTrainingDays;
    }

    public int getConferenceDays() {
        return conferenceDays;
    }

    public void setConferenceDays(int conferenceDays) {
        this.conferenceDays = conferenceDays;
    }

    public int getMaternityProtectionDays() {
        return maternityProtectionDays;
    }

    public void setMaternityProtectionDays(int maternityProtectionDays) {
        this.maternityProtectionDays = maternityProtectionDays;
    }

    public int getFatherMonthDays() {
        return fatherMonthDays;
    }

    public void setFatherMonthDays(int fatherMonthDays) {
        this.fatherMonthDays = fatherMonthDays;
    }

    public int getPaidSpecialLeaveDays() {
        return paidSpecialLeaveDays;
    }

    public void setPaidSpecialLeaveDays(int paidSpecialLeaveDays) {
        this.paidSpecialLeaveDays = paidSpecialLeaveDays;
    }

    public int getNonPaidVacationDays() {
        return nonPaidVacationDays;
    }

    public void setNonPaidVacationDays(int nonPaidVacationDays) {
        this.nonPaidVacationDays = nonPaidVacationDays;
    }

    public int getPaidSickLeave() {
        return paidSickLeave;
    }

    public void setPaidSickLeave(int paidSickLeave) {
        this.paidSickLeave = paidSickLeave;
    }

    public double getVacationDayBalance() {
        return vacationDayBalance;
    }

    public void setVacationDayBalance(double vacationDayBalance) {
        this.vacationDayBalance = vacationDayBalance;
    }

    public String getBillableTime() {
        return billableTime;
    }

    public void setBillableTime(String billableTime) {
        this.billableTime = billableTime;
    }

    public String getTotalWorkingTime() {
        return totalWorkingTime;
    }

    public void setTotalWorkingTime(String totalWorkingTime) {
        this.totalWorkingTime = totalWorkingTime;
    }

    public double getOvertime() {
        return overtime;
    }

    public void setOvertime(double overtime) {
        this.overtime = overtime;
    }

    public boolean isHasPrematureEmployeeCheck() {
        return hasPrematureEmployeeCheck;
    }

    public void setHasPrematureEmployeeCheck(boolean hasPrematureEmployeeCheck) {
        this.hasPrematureEmployeeCheck = hasPrematureEmployeeCheck;
    }

    public static final class MonthlyReportBuilder {
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

        private MonthlyReportBuilder() {
        }

        public static MonthlyReportBuilder aMonthlyReport() {
            return new MonthlyReportBuilder();
        }

        public MonthlyReportBuilder employee(Employee employee) {
            this.employee = employee;
            return this;
        }

        public MonthlyReportBuilder initialDate(LocalDate initialDate) {
            this.initialDate = initialDate;
            return this;
        }

        public MonthlyReportBuilder timeWarnings(List<MappedTimeWarningDTO> timeWarnings) {
            this.timeWarnings = timeWarnings;
            return this;
        }

        public MonthlyReportBuilder journeyWarnings(List<JourneyWarning> journeyWarnings) {
            this.journeyWarnings = journeyWarnings;
            return this;
        }

        public MonthlyReportBuilder comments(List<Comment> comments) {
            this.comments = comments;
            return this;
        }

        public MonthlyReportBuilder employeeCheckState(EmployeeState employeeCheckState) {
            this.employeeCheckState = employeeCheckState;
            return this;
        }

        public MonthlyReportBuilder employeeCheckStateReason(String employeeCheckStateReason) {
            this.employeeCheckStateReason = employeeCheckStateReason;
            return this;
        }

        public MonthlyReportBuilder internalCheckState(EmployeeState internalCheckState) {
            this.internalCheckState = internalCheckState;
            return this;
        }

        public MonthlyReportBuilder employeeProgresses(List<PmProgressDto> employeeProgresses) {
            this.employeeProgresses = employeeProgresses;
            return this;
        }

        public MonthlyReportBuilder otherChecksDone(boolean otherChecksDone) {
            this.otherChecksDone = otherChecksDone;
            return this;
        }

        public MonthlyReportBuilder vacationDays(int vacationDays) {
            this.vacationDays = vacationDays;
            return this;
        }

        public MonthlyReportBuilder homeofficeDays(int homeofficeDays) {
            this.homeofficeDays = homeofficeDays;
            return this;
        }

        public MonthlyReportBuilder compensatoryDays(int compensatoryDays) {
            this.compensatoryDays = compensatoryDays;
            return this;
        }

        public MonthlyReportBuilder nursingDays(int nursingDays) {
            this.nursingDays = nursingDays;
            return this;
        }

        public MonthlyReportBuilder maternityLeaveDays(int maternityLeaveDays) {
            this.maternityLeaveDays = maternityLeaveDays;
            return this;
        }

        public MonthlyReportBuilder externalTrainingDays(int externalTrainingDays) {
            this.externalTrainingDays = externalTrainingDays;
            return this;
        }

        public MonthlyReportBuilder conferenceDays(int conferenceDays) {
            this.conferenceDays = conferenceDays;
            return this;
        }

        public MonthlyReportBuilder maternityProtectionDays(int maternityProtectionDays) {
            this.maternityProtectionDays = maternityProtectionDays;
            return this;
        }

        public MonthlyReportBuilder fatherMonthDays(int fatherMonthDays) {
            this.fatherMonthDays = fatherMonthDays;
            return this;
        }

        public MonthlyReportBuilder paidSpecialLeaveDays(int paidSpecialLeaveDays) {
            this.paidSpecialLeaveDays = paidSpecialLeaveDays;
            return this;
        }

        public MonthlyReportBuilder nonPaidVacationDays(int nonPaidVacationDays) {
            this.nonPaidVacationDays = nonPaidVacationDays;
            return this;
        }

        public MonthlyReportBuilder paidSickLeave(int paidSickLeave) {
            this.paidSickLeave = paidSickLeave;
            return this;
        }

        public MonthlyReportBuilder vacationDayBalance(double vacationDayBalance) {
            this.vacationDayBalance = vacationDayBalance;
            return this;
        }

        public MonthlyReportBuilder billableTime(String billableTime) {
            this.billableTime = billableTime;
            return this;
        }

        public MonthlyReportBuilder totalWorkingTime(String totalWorkingTime) {
            this.totalWorkingTime = totalWorkingTime;
            return this;
        }

        public MonthlyReportBuilder overtime(double overtime) {
            this.overtime = overtime;
            return this;
        }

        public MonthlyReportBuilder hasPrematureEmployeeCheck(boolean hasPrematureEmployeeCheck) {
            this.hasPrematureEmployeeCheck = hasPrematureEmployeeCheck;
            return this;
        }

        public MonthlyReport build() {
            MonthlyReport monthlyReport = new MonthlyReport();
            monthlyReport.setEmployee(employee);
            monthlyReport.setInitialDate(initialDate);
            monthlyReport.setTimeWarnings(timeWarnings);
            monthlyReport.setJourneyWarnings(journeyWarnings);
            monthlyReport.setComments(comments);
            monthlyReport.setEmployeeCheckState(employeeCheckState);
            monthlyReport.setEmployeeCheckStateReason(employeeCheckStateReason);
            monthlyReport.setInternalCheckState(internalCheckState);
            monthlyReport.setEmployeeProgresses(employeeProgresses);
            monthlyReport.setOtherChecksDone(otherChecksDone);
            monthlyReport.setVacationDays(vacationDays);
            monthlyReport.setHomeofficeDays(homeofficeDays);
            monthlyReport.setCompensatoryDays(compensatoryDays);
            monthlyReport.setNursingDays(nursingDays);
            monthlyReport.setMaternityLeaveDays(maternityLeaveDays);
            monthlyReport.setExternalTrainingDays(externalTrainingDays);
            monthlyReport.setConferenceDays(conferenceDays);
            monthlyReport.setMaternityProtectionDays(maternityProtectionDays);
            monthlyReport.setFatherMonthDays(fatherMonthDays);
            monthlyReport.setPaidSpecialLeaveDays(paidSpecialLeaveDays);
            monthlyReport.setNonPaidVacationDays(nonPaidVacationDays);
            monthlyReport.setPaidSickLeave(paidSickLeave);
            monthlyReport.setVacationDayBalance(vacationDayBalance);
            monthlyReport.setBillableTime(billableTime);
            monthlyReport.setTotalWorkingTime(totalWorkingTime);
            monthlyReport.setOvertime(overtime);
            monthlyReport.setHasPrematureEmployeeCheck(hasPrematureEmployeeCheck);
            return monthlyReport;
        }
    }
}

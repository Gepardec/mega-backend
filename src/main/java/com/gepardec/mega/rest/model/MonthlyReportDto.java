package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MonthlyReportDto {
    @JsonProperty
    private EmployeeDto employee;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate initialDate;

    @JsonProperty
    private List<MappedTimeWarningDTO> timeWarnings;

    @JsonProperty
    private List<JourneyWarning> journeyWarnings;

    @JsonProperty
    private List<CommentDto> comments;

    @JsonProperty
    private EmployeeState employeeCheckState;

    @JsonProperty
    private String employeeCheckStateReason;

    @JsonProperty
    private EmployeeState internalCheckState;

    @JsonProperty
    private List<PmProgressDto> employeeProgresses;

    @JsonProperty
    private boolean otherChecksDone;

    @JsonProperty
    private int vacationDays;

    @JsonProperty
    private int homeofficeDays;

    @JsonProperty
    private int compensatoryDays;

    @JsonProperty
    private int nursingDays;

    @JsonProperty
    private int maternityLeaveDays;

    @JsonProperty
    private int externalTrainingDays;

    @JsonProperty
    private int conferenceDays;

    @JsonProperty
    private int maternityProtectionDays;

    @JsonProperty
    private int fatherMonthDays;

    @JsonProperty
    private int paidSpecialLeaveDays;

    @JsonProperty
    private int nonPaidVacationDays;

    @JsonProperty
    private double vacationDayBalance;

    @JsonProperty
    private String billableTime;

    @JsonProperty
    private String totalWorkingTime;

    @JsonProperty
    private int paidSickLeave;

    @JsonProperty
    private double overtime;

    @JsonProperty
    private boolean hasPrematureEmployeeCheck;

    public MonthlyReportDto() {
    }

    public MonthlyReportDto(EmployeeDto employee, LocalDate initialDate, List<MappedTimeWarningDTO> timeWarnings, List<JourneyWarning> journeyWarnings, List<CommentDto> comments, EmployeeState employeeCheckState, String employeeCheckStateReason, EmployeeState internalCheckState, List<PmProgressDto> employeeProgresses, boolean otherChecksDone, int vacationDays, int homeofficeDays, int compensatoryDays, int nursingDays, int maternityLeaveDays, int externalTrainingDays, int conferenceDays, int maternityProtectionDays, int fatherMonthDays, int paidSpecialLeaveDays, int nonPaidVacationDays, double vacationDayBalance, String billableTime, String totalWorkingTime, int paidSickLeave, double overtime, boolean hasPrematureEmployeeCheck) {
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
        this.vacationDayBalance = vacationDayBalance;
        this.billableTime = billableTime;
        this.totalWorkingTime = totalWorkingTime;
        this.paidSickLeave = paidSickLeave;
        this.overtime = overtime;
        this.hasPrematureEmployeeCheck = hasPrematureEmployeeCheck;
    }

    public static MonthlyReportDtoBuilder builder() {
        return MonthlyReportDtoBuilder.aMonthlyReportDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonthlyReportDto that = (MonthlyReportDto) o;
        return isOtherChecksDone() == that.isOtherChecksDone() && getVacationDays() == that.getVacationDays() && getHomeofficeDays() == that.getHomeofficeDays() && getCompensatoryDays() == that.getCompensatoryDays() && getNursingDays() == that.getNursingDays() && getMaternityLeaveDays() == that.getMaternityLeaveDays() && getExternalTrainingDays() == that.getExternalTrainingDays() && getConferenceDays() == that.getConferenceDays() && getMaternityProtectionDays() == that.getMaternityProtectionDays() && getFatherMonthDays() == that.getFatherMonthDays() && getPaidSpecialLeaveDays() == that.getPaidSpecialLeaveDays() && getNonPaidVacationDays() == that.getNonPaidVacationDays() && Double.compare(getVacationDayBalance(), that.getVacationDayBalance()) == 0 && getPaidSickLeave() == that.getPaidSickLeave() && Double.compare(getOvertime(), that.getOvertime()) == 0 && isHasPrematureEmployeeCheck() == that.isHasPrematureEmployeeCheck() && Objects.equals(getEmployee(), that.getEmployee()) && Objects.equals(getInitialDate(), that.getInitialDate()) && Objects.equals(getTimeWarnings(), that.getTimeWarnings()) && Objects.equals(getJourneyWarnings(), that.getJourneyWarnings()) && Objects.equals(getComments(), that.getComments()) && getEmployeeCheckState() == that.getEmployeeCheckState() && Objects.equals(getEmployeeCheckStateReason(), that.getEmployeeCheckStateReason()) && getInternalCheckState() == that.getInternalCheckState() && Objects.equals(getEmployeeProgresses(), that.getEmployeeProgresses()) && Objects.equals(getBillableTime(), that.getBillableTime()) && Objects.equals(getTotalWorkingTime(), that.getTotalWorkingTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmployee(), getInitialDate(), getTimeWarnings(), getJourneyWarnings(), getComments(), getEmployeeCheckState(), getEmployeeCheckStateReason(), getInternalCheckState(), getEmployeeProgresses(), isOtherChecksDone(), getVacationDays(), getHomeofficeDays(), getCompensatoryDays(), getNursingDays(), getMaternityLeaveDays(), getExternalTrainingDays(), getConferenceDays(), getMaternityProtectionDays(), getFatherMonthDays(), getPaidSpecialLeaveDays(), getNonPaidVacationDays(), getVacationDayBalance(), getBillableTime(), getTotalWorkingTime(), getPaidSickLeave(), getOvertime(), isHasPrematureEmployeeCheck());
    }

    public EmployeeDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDto employee) {
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

    public List<CommentDto> getComments() {
        return comments;
    }

    public void setComments(List<CommentDto> comments) {
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

    public int getPaidSickLeave() {
        return paidSickLeave;
    }

    public void setPaidSickLeave(int paidSickLeave) {
        this.paidSickLeave = paidSickLeave;
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

    public static final class MonthlyReportDtoBuilder {
        private EmployeeDto employee;
        private LocalDate initialDate;
        private List<MappedTimeWarningDTO> timeWarnings;
        private List<JourneyWarning> journeyWarnings;
        private List<CommentDto> comments;
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
        private double vacationDayBalance;
        private String billableTime;
        private String totalWorkingTime;
        private int paidSickLeave;
        private double overtime;
        private boolean hasPrematureEmployeeCheck;

        private MonthlyReportDtoBuilder() {
        }

        public static MonthlyReportDtoBuilder aMonthlyReportDto() {
            return new MonthlyReportDtoBuilder();
        }

        public MonthlyReportDtoBuilder employee(EmployeeDto employee) {
            this.employee = employee;
            return this;
        }

        public MonthlyReportDtoBuilder initialDate(LocalDate initialDate) {
            this.initialDate = initialDate;
            return this;
        }

        public MonthlyReportDtoBuilder timeWarnings(List<MappedTimeWarningDTO> timeWarnings) {
            this.timeWarnings = timeWarnings;
            return this;
        }

        public MonthlyReportDtoBuilder journeyWarnings(List<JourneyWarning> journeyWarnings) {
            this.journeyWarnings = journeyWarnings;
            return this;
        }

        public MonthlyReportDtoBuilder comments(List<CommentDto> comments) {
            this.comments = comments;
            return this;
        }

        public MonthlyReportDtoBuilder employeeCheckState(EmployeeState employeeCheckState) {
            this.employeeCheckState = employeeCheckState;
            return this;
        }

        public MonthlyReportDtoBuilder employeeCheckStateReason(String employeeCheckStateReason) {
            this.employeeCheckStateReason = employeeCheckStateReason;
            return this;
        }

        public MonthlyReportDtoBuilder internalCheckState(EmployeeState internalCheckState) {
            this.internalCheckState = internalCheckState;
            return this;
        }

        public MonthlyReportDtoBuilder employeeProgresses(List<PmProgressDto> employeeProgresses) {
            this.employeeProgresses = employeeProgresses;
            return this;
        }

        public MonthlyReportDtoBuilder otherChecksDone(boolean otherChecksDone) {
            this.otherChecksDone = otherChecksDone;
            return this;
        }

        public MonthlyReportDtoBuilder vacationDays(int vacationDays) {
            this.vacationDays = vacationDays;
            return this;
        }

        public MonthlyReportDtoBuilder homeofficeDays(int homeofficeDays) {
            this.homeofficeDays = homeofficeDays;
            return this;
        }

        public MonthlyReportDtoBuilder compensatoryDays(int compensatoryDays) {
            this.compensatoryDays = compensatoryDays;
            return this;
        }

        public MonthlyReportDtoBuilder nursingDays(int nursingDays) {
            this.nursingDays = nursingDays;
            return this;
        }

        public MonthlyReportDtoBuilder maternityLeaveDays(int maternityLeaveDays) {
            this.maternityLeaveDays = maternityLeaveDays;
            return this;
        }

        public MonthlyReportDtoBuilder externalTrainingDays(int externalTrainingDays) {
            this.externalTrainingDays = externalTrainingDays;
            return this;
        }

        public MonthlyReportDtoBuilder conferenceDays(int conferenceDays) {
            this.conferenceDays = conferenceDays;
            return this;
        }

        public MonthlyReportDtoBuilder maternityProtectionDays(int maternityProtectionDays) {
            this.maternityProtectionDays = maternityProtectionDays;
            return this;
        }

        public MonthlyReportDtoBuilder fatherMonthDays(int fatherMonthDays) {
            this.fatherMonthDays = fatherMonthDays;
            return this;
        }

        public MonthlyReportDtoBuilder paidSpecialLeaveDays(int paidSpecialLeaveDays) {
            this.paidSpecialLeaveDays = paidSpecialLeaveDays;
            return this;
        }

        public MonthlyReportDtoBuilder nonPaidVacationDays(int nonPaidVacationDays) {
            this.nonPaidVacationDays = nonPaidVacationDays;
            return this;
        }

        public MonthlyReportDtoBuilder vacationDayBalance(double vacationDayBalance) {
            this.vacationDayBalance = vacationDayBalance;
            return this;
        }

        public MonthlyReportDtoBuilder billableTime(String billableTime) {
            this.billableTime = billableTime;
            return this;
        }

        public MonthlyReportDtoBuilder totalWorkingTime(String totalWorkingTime) {
            this.totalWorkingTime = totalWorkingTime;
            return this;
        }

        public MonthlyReportDtoBuilder paidSickLeave(int paidSickLeave) {
            this.paidSickLeave = paidSickLeave;
            return this;
        }

        public MonthlyReportDtoBuilder overtime(double overtime) {
            this.overtime = overtime;
            return this;
        }

        public MonthlyReportDtoBuilder hasPrematureEmployeeCheck(boolean hasPrematureEmployeeCheck) {
            this.hasPrematureEmployeeCheck = hasPrematureEmployeeCheck;
            return this;
        }

        public MonthlyReportDto build() {
            MonthlyReportDto monthlyReportDto = new MonthlyReportDto();
            monthlyReportDto.setEmployee(employee);
            monthlyReportDto.setInitialDate(initialDate);
            monthlyReportDto.setTimeWarnings(timeWarnings);
            monthlyReportDto.setJourneyWarnings(journeyWarnings);
            monthlyReportDto.setComments(comments);
            monthlyReportDto.setEmployeeCheckState(employeeCheckState);
            monthlyReportDto.setEmployeeCheckStateReason(employeeCheckStateReason);
            monthlyReportDto.setInternalCheckState(internalCheckState);
            monthlyReportDto.setEmployeeProgresses(employeeProgresses);
            monthlyReportDto.setOtherChecksDone(otherChecksDone);
            monthlyReportDto.setVacationDays(vacationDays);
            monthlyReportDto.setHomeofficeDays(homeofficeDays);
            monthlyReportDto.setCompensatoryDays(compensatoryDays);
            monthlyReportDto.setNursingDays(nursingDays);
            monthlyReportDto.setMaternityLeaveDays(maternityLeaveDays);
            monthlyReportDto.setExternalTrainingDays(externalTrainingDays);
            monthlyReportDto.setConferenceDays(conferenceDays);
            monthlyReportDto.setMaternityProtectionDays(maternityProtectionDays);
            monthlyReportDto.setFatherMonthDays(fatherMonthDays);
            monthlyReportDto.setPaidSpecialLeaveDays(paidSpecialLeaveDays);
            monthlyReportDto.setNonPaidVacationDays(nonPaidVacationDays);
            monthlyReportDto.setVacationDayBalance(vacationDayBalance);
            monthlyReportDto.setBillableTime(billableTime);
            monthlyReportDto.setTotalWorkingTime(totalWorkingTime);
            monthlyReportDto.setPaidSickLeave(paidSickLeave);
            monthlyReportDto.setOvertime(overtime);
            monthlyReportDto.setHasPrematureEmployeeCheck(hasPrematureEmployeeCheck);
            return monthlyReportDto;
        }
    }
}

package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = MonthlyReportDto.Builder.class)
public class MonthlyReportDto {

    private final EmployeeDto employee;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate initialDate;

    private final List<MappedTimeWarningDTO> timeWarnings;

    private final List<JourneyWarning> journeyWarnings;

    private final List<CommentDto> comments;

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

    private final double vacationDayBalance;

    private final String billableTime;

    private final String totalWorkingTime;

    private final int paidSickLeave;

    private final double overtime;

    private final String guildLead;

    private final String internalProjectLead;

    private final PrematureEmployeeCheckDto prematureEmployeeCheck;

    private final Integer personioId;

    private MonthlyReportDto(Builder builder) {
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
        this.vacationDayBalance = builder.vacationDayBalance;
        this.billableTime = builder.billableTime;
        this.totalWorkingTime = builder.totalWorkingTime;
        this.paidSickLeave = builder.paidSickLeave;
        this.overtime = builder.overtime;
        this.prematureEmployeeCheck = builder.prematureEmployeeCheck;
        this.guildLead = builder.guildLead;
        this.internalProjectLead = builder.internalProjectLead;
        this.personioId = builder.personioId;
    }

    public static Builder builder() {
        return Builder.aMonthlyReportDto();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonthlyReportDto that = (MonthlyReportDto) o;
        return isOtherChecksDone() == that.isOtherChecksDone() && getVacationDays() == that.getVacationDays() && getHomeofficeDays() == that.getHomeofficeDays() && getCompensatoryDays() == that.getCompensatoryDays() && getNursingDays() == that.getNursingDays() && getMaternityLeaveDays() == that.getMaternityLeaveDays() && getExternalTrainingDays() == that.getExternalTrainingDays() && getConferenceDays() == that.getConferenceDays() && getMaternityProtectionDays() == that.getMaternityProtectionDays() && getFatherMonthDays() == that.getFatherMonthDays() && getPaidSpecialLeaveDays() == that.getPaidSpecialLeaveDays() && getNonPaidVacationDays() == that.getNonPaidVacationDays() && Double.compare(getVacationDayBalance(), that.getVacationDayBalance()) == 0 && getPaidSickLeave() == that.getPaidSickLeave() && Double.compare(getOvertime(), that.getOvertime()) == 0 && getPrematureEmployeeCheck() == that.getPrematureEmployeeCheck() && Objects.equals(getEmployee(), that.getEmployee()) && Objects.equals(getInitialDate(), that.getInitialDate()) && Objects.equals(getTimeWarnings(), that.getTimeWarnings()) && Objects.equals(getJourneyWarnings(), that.getJourneyWarnings()) && Objects.equals(getComments(), that.getComments()) && getEmployeeCheckState() == that.getEmployeeCheckState() && Objects.equals(getEmployeeCheckStateReason(), that.getEmployeeCheckStateReason()) && getInternalCheckState() == that.getInternalCheckState() && Objects.equals(getEmployeeProgresses(), that.getEmployeeProgresses()) && Objects.equals(getBillableTime(), that.getBillableTime()) && Objects.equals(getTotalWorkingTime(), that.getTotalWorkingTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmployee(), getInitialDate(), getTimeWarnings(), getJourneyWarnings(), getComments(), getEmployeeCheckState(), getEmployeeCheckStateReason(), getInternalCheckState(), getEmployeeProgresses(), isOtherChecksDone(), getVacationDays(), getHomeofficeDays(), getCompensatoryDays(), getNursingDays(), getMaternityLeaveDays(), getExternalTrainingDays(), getConferenceDays(), getMaternityProtectionDays(), getFatherMonthDays(), getPaidSpecialLeaveDays(), getNonPaidVacationDays(), getVacationDayBalance(), getBillableTime(), getTotalWorkingTime(), getPaidSickLeave(), getOvertime(), getPrematureEmployeeCheck());
    }

    public EmployeeDto getEmployee() {
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

    public List<CommentDto> getComments() {
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

    public double getVacationDayBalance() {
        return vacationDayBalance;
    }

    public String getBillableTime() {
        return billableTime;
    }

    public String getTotalWorkingTime() {
        return totalWorkingTime;
    }

    public int getPaidSickLeave() {
        return paidSickLeave;
    }

    public double getOvertime() {
        return overtime;
    }

    public String getGuildLead() {
        return guildLead;
    }

    public String getInternalProjectLead() {
        return internalProjectLead;
    }

    public PrematureEmployeeCheckDto getPrematureEmployeeCheck() {
        return prematureEmployeeCheck;
    }

    public Integer getPersonioId() {
        return personioId;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
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

        private String guildLead;

        private String internalProjectLead;

        private PrematureEmployeeCheckDto prematureEmployeeCheck;

        private Integer personioId;

        private Builder() {
        }

        public static Builder aMonthlyReportDto() {
            return new Builder();
        }

        public Builder employee(EmployeeDto employee) {
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

        public Builder comments(List<CommentDto> comments) {
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

        public Builder paidSickLeave(int paidSickLeave) {
            this.paidSickLeave = paidSickLeave;
            return this;
        }

        public Builder overtime(double overtime) {
            this.overtime = overtime;
            return this;
        }

        public Builder prematureEmployeeCheck(PrematureEmployeeCheckDto prematureEmployeeCheck) {
            this.prematureEmployeeCheck = prematureEmployeeCheck;
            return this;
        }

        public Builder guildLead(String guildLead) {
            this.guildLead = guildLead;
            return this;
        }

        public Builder internalProjectLead(String internalProjectLead) {
            this.internalProjectLead = internalProjectLead;
            return this;
        }

        public Builder personioId(Integer personioId) {
            this.personioId = personioId;
            return this;
        }

        public MonthlyReportDto build() {
            return new MonthlyReportDto(this);
        }

    }
}

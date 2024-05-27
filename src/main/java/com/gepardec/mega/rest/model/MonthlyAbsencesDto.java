package com.gepardec.mega.rest.model;

public class MonthlyAbsencesDto {
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
    private final double doctorsVisitingTime;
    private final int availableVacationDays;

    private MonthlyAbsencesDto(Builder builder) {
        this.vacationDays =  builder.vacationDays;
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
        this.doctorsVisitingTime = builder.doctorsVisitingTime;
        this.availableVacationDays = builder.availableVacationDays;
    }

    public static Builder builder() {return Builder.aMonthlyAbsencesDto();}

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

    public double getDoctorsVisitingTime() {
        return doctorsVisitingTime;
    }

    public int getAvailableVacationDays() {
        return availableVacationDays;
    }

    public static final class Builder {
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
        private double doctorsVisitingTime;
        private int availableVacationDays;

        private Builder(){}

        public static Builder aMonthlyAbsencesDto() {return new Builder();}

        public Builder vacationDays(int vacationDays){
            this.vacationDays = vacationDays;
            return this;
        }

        public Builder homeofficeDays(int homeofficeDays){
            this.homeofficeDays = homeofficeDays;
            return this;
        }

        public Builder compensatoryDays(int compensatoryDays){
            this.compensatoryDays = compensatoryDays;
            return this;
        }

        public Builder nursingDays(int nursingDays){
            this.nursingDays = nursingDays;
            return this;
        }

        public Builder maternityLeaveDays(int maternityLeaveDays){
            this.maternityLeaveDays = maternityLeaveDays;
            return this;
        }

        public Builder externalTrainingDays(int externalTrainingDays){
            this.externalTrainingDays = externalTrainingDays;
            return this;
        }

        public Builder conferenceDays(int conferenceDays){
            this.conferenceDays = conferenceDays;
            return this;
        }

        public Builder maternityProtectionDays(int maternityProtectionDays){
            this.maternityProtectionDays = maternityProtectionDays;
            return this;
        }

        public Builder fatherMonthDays(int fatherMonthDays){
            this.fatherMonthDays = fatherMonthDays;
            return this;
        }

        public Builder paidSpecialLeaveDays(int paidSpecialLeaveDays){
            this.paidSpecialLeaveDays = paidSpecialLeaveDays;
            return this;
        }

        public Builder nonPaidVacationDays(int nonPaidVacationDays){
            this.nonPaidVacationDays = nonPaidVacationDays;
            return this;
        }

        public Builder paidSickLeave(int paidSickLeave){
            this.paidSickLeave = paidSickLeave;
            return this;
        }

        public Builder doctorsVisitingTime(double doctorsVisitingTime){
            this.doctorsVisitingTime = doctorsVisitingTime;
            return this;
        }

        public Builder availableVacationDays(int availableVacationDays){
            this.availableVacationDays = availableVacationDays;
            return this;
        }

        public MonthlyAbsencesDto build() {return new MonthlyAbsencesDto(this);}


    }
}

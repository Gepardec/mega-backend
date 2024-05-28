package com.gepardec.mega.rest.model;

public class MonthlyOfficeDaysDto {
    private final int homeofficeDays;
    private final int fridaysAtTheOffice;
    private final int officeDays;

    public static Builder builder(){return Builder.aMonthlyOfficeDaysDto();}

    private MonthlyOfficeDaysDto(Builder builder) {
        this.homeofficeDays = builder.homeofficeDays;
        this.fridaysAtTheOffice = builder.fridaysAtTheOffice;
        this.officeDays = builder.officeDays;
    }

    public int getHomeofficeDays() {
        return homeofficeDays;
    }

    public int getFridaysAtTheOffice() {
        return fridaysAtTheOffice;
    }

    public int getOfficeDays() {
        return officeDays;
    }

    public static class Builder{
        private int homeofficeDays;
        private int fridaysAtTheOffice;
        private int officeDays;

        private Builder(){}
        public static Builder aMonthlyOfficeDaysDto(){return new Builder();}

        public Builder homeOfficeDays(int homeofficeDays){
            this.homeofficeDays = homeofficeDays;
            return this;
        }

        public Builder fridaysAtTheOffice(int fridaysAtTheOffice){
            this.fridaysAtTheOffice = fridaysAtTheOffice;
            return this;
        }

        public Builder officeDays(int officeDays){
            this.officeDays = officeDays;
            return this;
        }

        public MonthlyOfficeDaysDto build(){return new MonthlyOfficeDaysDto(this);}
    }
}

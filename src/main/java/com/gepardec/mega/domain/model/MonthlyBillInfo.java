package com.gepardec.mega.domain.model;

public class MonthlyBillInfo {
    private final int sumBills;
    private final int sumPrivateBills;
    private final int sumCompanyBills;
    private final boolean hasAttachmentWarnings;
    private final boolean employeeHasCreditCard;

    private MonthlyBillInfo(Builder builder){
        this.sumBills = builder.sumBills;
        this.sumPrivateBills = builder.sumPrivateBills;
        this.sumCompanyBills = builder.sumCompanyBills;
        this.hasAttachmentWarnings = builder.hasAttachmentWarnings;
        this.employeeHasCreditCard = builder.employeeHasCreditCard;
    }

    public int getSumBills() {
        return sumBills;
    }

    public int getSumPrivateBills() {
        return sumPrivateBills;
    }

    public int getSumCompanyBills() {
        return sumCompanyBills;
    }

    public boolean getHasAttachmentWarnings() {
        return hasAttachmentWarnings;
    }

    public boolean getEmployeeHasCreditCard() {
        return employeeHasCreditCard;
    }
    public static Builder builder(){return Builder.aMonthlyBillInfo();}

    public static final class Builder {
        private  int sumBills;
        private  int sumPrivateBills;
        private  int sumCompanyBills;
        private  boolean hasAttachmentWarnings;
        private  boolean employeeHasCreditCard;

        private Builder(){}
        public static Builder aMonthlyBillInfo(){return new Builder();}

        public Builder sumBills(int sumBills) {
            this.sumBills = sumBills;
            return this;
        }

        public Builder sumPrivateBills(int sumPrivateBills) {
            this.sumPrivateBills = sumPrivateBills;
            return this;
        }

        public Builder sumCompanyBills(int sumCompanyBills) {
            this.sumCompanyBills = sumCompanyBills;
            return this;
        }

        public Builder hasAttachmentWarnings(boolean hasAttachmentWarnings) {
            this.hasAttachmentWarnings = hasAttachmentWarnings;
            return this;
        }

        public Builder employeeHasCreditCard(boolean employeeHasCreditCard) {
            this.employeeHasCreditCard = employeeHasCreditCard;
            return this;
        }

        public MonthlyBillInfo build(){return new MonthlyBillInfo(this);}
    }

}

package com.gepardec.mega.domain.model;

import com.gepardec.mega.db.entity.common.PaymentMethodType;

import java.time.LocalDate;

public class Bill {
    private final LocalDate billDate;

    private final Double bruttoValue;

    private final String billType;

    private final PaymentMethodType paymentMethodType;

    private final String projectName;


    private Bill(Builder builder){
        this.billDate = builder.billDate;
        this.bruttoValue = builder.bruttoValue;
        this.billType = builder.billType;
        this.paymentMethodType = builder.paymentMethodType;
        this.projectName = builder.projectName;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public Double getBruttoValue() {
        return bruttoValue;
    }

    public String getBillType() {
        return billType;
    }

    public PaymentMethodType getPaymentMethodType() {
        return paymentMethodType;
    }

    public String getProjectName() {
        return projectName;
    }

    public static final class Builder {

            private LocalDate billDate;

            private Double bruttoValue;

            private String billType;

            private PaymentMethodType paymentMethodType;

            private String projectName;


            private Builder(){}

            public static Builder aBeleg(){return new Builder();}

            public Builder billDate(LocalDate billDate) {
                this.billDate = billDate;
                return this;
            }

            public Builder bruttoValue(Double bruttoValue) {
                this.bruttoValue = bruttoValue;
                return this;
            }

            public Builder billType(String billType) {
                this.billType = billType;
                return this;
            }

            public Builder paymentMethodType(PaymentMethodType paymentMethodType) {
                this.paymentMethodType = paymentMethodType;
                return this;
            }

            public Builder projectName(String projectName) {
                this.projectName = projectName;
                return this;
            }


        }
}

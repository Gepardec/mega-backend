package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record ZepReceiptType(
        String id,
        String name,
        DescriptionObject description,
        String bankAccount,
        String datev,
        String accountingKey,
        Double tax,
        Double amount,
        Double externalAmount,
        String currency,
        String paymentType,
        boolean isInclusiveBill,
        boolean isTaxAdjustable,
        boolean isAlternativeDate,
        boolean isSeventyToThirtySplit,
        String created,
        String modified
) {
    @JsonCreator
    public ZepReceiptType(Builder builder) {
        this(
                builder.id,
                builder.name,
                builder.description,
                builder.bankAccount,
                builder.datev,
                builder.accountingKey,
                builder.tax,
                builder.amount,
                builder.externalAmount,
                builder.currency,
                builder.paymentType,
                builder.isInclusiveBill,
                builder.isTaxAdjustable,
                builder.isAlternativeDate,
                builder.isSeventyToThirtySplit,
                builder.created,
                builder.modified
        );
    }

    public static Builder builder() {
        return  Builder.aZepReceiptType();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        @JsonProperty
        private String id;
        @JsonProperty
        private String name;
        @JsonProperty
        private DescriptionObject description;
        @JsonProperty
        private String bankAccount;
        @JsonProperty
        private String datev;
        @JsonProperty
        private String accountingKey;
        @JsonProperty
        private Double tax;
        @JsonProperty
        private Double amount;
        @JsonProperty
        private Double externalAmount;
        @JsonProperty
        private String currency;
        @JsonProperty
        private String paymentType;
        @JsonProperty
        private boolean isInclusiveBill;
        @JsonProperty
        private boolean isTaxAdjustable;
        @JsonProperty
        private boolean isAlternativeDate;
        @JsonProperty
        private boolean isSeventyToThirtySplit;
        @JsonProperty
        private String created;
        @JsonProperty
        private String modified;

        private Builder() {
        }

        public static Builder aZepReceiptType() {return new Builder();}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(DescriptionObject description) {
            this.description = description;
            return this;
        }

        public Builder bankAccount(String bankAccount) {
            this.bankAccount = bankAccount;
            return this;
        }

        public Builder datev(String datev) {
            this.datev = datev;
            return this;
        }

        public Builder accountingKey(String accountingKey) {
            this.accountingKey = accountingKey;
            return this;
        }

        public Builder tax(Double tax) {
            this.tax = tax;
            return this;
        }

        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        public Builder externalAmount(Double externalAmount) {
            this.externalAmount = externalAmount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder paymentType(String paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public Builder isInclusiveBill(boolean isInclusiveBill) {
            this.isInclusiveBill = isInclusiveBill;
            return this;
        }

        public Builder isTaxAdjustable(boolean isTaxAdjustable) {
            this.isTaxAdjustable = isTaxAdjustable;
            return this;
        }

        public Builder isAlternativeDate(boolean isAlternativeDate) {
            this.isAlternativeDate = isAlternativeDate;
            return this;
        }

        public Builder isSeventyToThirtySplit(boolean isSeventyToThirtySplit) {
            this.isSeventyToThirtySplit = isSeventyToThirtySplit;
            return this;
        }

        public Builder created(String created) {
            this.created = created;
            return this;
        }

        public Builder modified(String modified) {
            this.modified = modified;
            return this;
        }

        public ZepReceiptType build() {
            return new ZepReceiptType(this);
        }
    }

    public record DescriptionObject(
            String de
    ) {
        public static Builder builder() {
            return Builder.aDescriptionObject();
        }

        @JsonCreator
        public DescriptionObject(Builder builder) {
            this(
                    builder.de
            );
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Builder {
            @JsonProperty
            private String de;

            private Builder(){
            }

            public static Builder aDescriptionObject() {return new Builder();}
            public Builder de(String de) {
                this.de = de;
                return this;
            }

            public DescriptionObject build() {
                return new DescriptionObject(this);
            }
        }
    }
}
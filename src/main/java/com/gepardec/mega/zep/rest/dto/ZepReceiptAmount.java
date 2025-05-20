package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ZepReceiptAmount(
        Integer receiptId,
        Double quantity,
        Double amount
) {

    public static Builder builder() {
        return Builder.aZepReceiptAmount();
    }

    @JsonCreator
    public ZepReceiptAmount(Builder builder) {
        this(
                builder.receiptId,
                builder.quantity,
                builder.amount
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        @JsonProperty("receipt_id")
        private Integer receiptId;

        @JsonProperty
        private Double quantity;

        @JsonProperty
        private Double amount;

        private Builder() {
        }

        public static Builder aZepReceiptAmount() {
            return new Builder();
        }

        public Builder receiptId(Integer receiptId) {
            this.receiptId = receiptId;
            return this;
        }

        public Builder quantity(Double quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        public ZepReceiptAmount build() {
            return new ZepReceiptAmount(this);
        }
    }
}

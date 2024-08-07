package com.gepardec.mega.zep.rest.dto;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.mega.db.entity.common.PaymentMethodType;
import java.time.LocalDate;

public record ZepReceipt (

        Integer id,
        String employeeId,
        LocalDate receiptDate,
        Double bruttoValue,

        String receiptTypeName,
        PaymentMethodType paymentMethodType,
        int projectId,
        String attachmentFileName

) {
    public static Builder builder() {return Builder.aZepReceipt();}

    @JsonCreator
    public ZepReceipt(Builder builder) {
        this(
                builder.id,
                builder.employeeId,
                builder.receiptDate,
                builder.bruttoValue,
                builder.receiptTypeName,
                builder.paymentMethodType,
                builder.projectId,
                builder.attachmentFileName
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        @JsonProperty
        private Integer id;

        @JsonProperty("employee_id")
        private String employeeId;

        @JsonProperty("date")
        private LocalDate receiptDate;

        @JsonProperty("amount")
        private Double bruttoValue;

        // was an object beforehand and is still one in the zep-documentation but was changed to string -> 10.5.2024
        @JsonProperty("receipt_type_id")
        private String receiptTypeName;
        @JsonProperty("payment_method")
        private PaymentMethodType paymentMethodType;
        @JsonProperty("project_id")
        private int projectId;
        @JsonProperty("filename")
        private String attachmentFileName;

        private Builder() {
        }

        public static Builder aZepReceipt() {return new Builder();}

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }
        public Builder receiptDate(LocalDate receiptDate) {
            this.receiptDate = receiptDate;
            return this;
        }

        public Builder bruttoValue(Double bruttoValue) {
            this.bruttoValue = bruttoValue;
            return this;
        }

        public Builder projectId(int projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder paymentMethodType(String paymentMethodTypeName) {
            this.paymentMethodType = PaymentMethodType.getByName(paymentMethodTypeName).orElse(null);
            return this;
        }

        public Builder receiptTypeName(String receiptTypeName) {
            this.receiptTypeName = receiptTypeName;
            return this;
        }

        public Builder attachmentFileName(String attachmentFileName) {
            this.attachmentFileName = attachmentFileName;
            return this;
        }

        public ZepReceipt build(){return new ZepReceipt(this);}

    }
}


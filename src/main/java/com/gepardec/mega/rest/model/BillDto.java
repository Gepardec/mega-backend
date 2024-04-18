package com.gepardec.mega.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.gepardec.mega.db.entity.common.PaymentMethodType;
import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = BillDto.Builder.class)
public class BillDto {

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private final LocalDate billDate;

    private final Double bruttoValue;

    private final String billType;

    private final PaymentMethodType paymentMethodType;

    private final String projectName;

    private final String attachmentBase64String;


    private BillDto(Builder builder){
        this.billDate = builder.billDate;
        this.bruttoValue = builder.bruttoValue;
        this.billType = builder.billType;
        this.paymentMethodType = builder.paymentMethodType;
        this.projectName = builder.projectName;
        this.attachmentBase64String = builder.attachmentBase64String;
    }

    public static Builder builder() {return Builder.aBillDto();}


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

    public String getAttachmentBase64String() {
        return attachmentBase64String;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillDto billDto = (BillDto) o;
        return Objects.equals(billDate, billDto.billDate) && Objects.equals(bruttoValue, billDto.bruttoValue) && Objects.equals(billType, billDto.billType) && paymentMethodType == billDto.paymentMethodType && Objects.equals(projectName, billDto.projectName) && Objects.equals(attachmentBase64String, billDto.attachmentBase64String);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billDate, bruttoValue, billType, paymentMethodType, projectName, attachmentBase64String);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        private LocalDate billDate;

        @JsonSerialize(using = NumberSerializers.DoubleSerializer.class)
        @JsonDeserialize(using = NumberDeserializers.DoubleDeserializer.class)
        private Double bruttoValue;

        private String billType;

        private PaymentMethodType paymentMethodType;

        private String projectName;

        private String attachmentBase64String;

        private Builder(){}

        public static Builder aBillDto(){return new Builder();}

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

        public Builder attachmentBase64(String attachmentBase64String){
            this.attachmentBase64String = attachmentBase64String;
            return this;
        }

        public BillDto build() {
            return new BillDto(this);
        }
    }
    
    

}

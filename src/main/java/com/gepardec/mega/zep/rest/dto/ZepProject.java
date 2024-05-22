package com.gepardec.mega.zep.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ZepProject (
        Integer id,
        String name,
        LocalDateTime startDate,
        LocalDateTime endDate,
        ZepBillingType billingType,
        Integer customerId
) {

      public static Builder builder() {
            return Builder.aZepProject();
      }

      @JsonCreator
      public ZepProject(Builder builder) {
            this(
                    builder.id,
                    builder.name,
                    builder.startDate,
                    builder.endDate,
                    builder.billingType,
                    builder.customerId
            );

      }

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static final class Builder {
            @JsonProperty
            private Integer id;
            @JsonProperty
            private String name;

            @JsonProperty("start_date")
            private LocalDateTime startDate;

            @JsonProperty("end_date")
            private LocalDateTime endDate;
            @JsonProperty("billing_type")
            private ZepBillingType billingType;

            @JsonProperty("customer_id")
            private Integer customerId;


            private Builder() {
            }

            public static Builder aZepProject() {
                  return new Builder();
            }

            public Builder id(Integer id) {
                  this.id = id;
                  return this;
            }

            public Builder name(String name) {
                  this.name = name;
                  return this;
            }

            public Builder startDate(LocalDateTime startDate) {
                  this.startDate = startDate;
                  return this;
            }

            public Builder endDate(LocalDateTime endDate) {
                  this.endDate = endDate;
                  return this;
            }

            public Builder billingType(ZepBillingType billingType) {
                  this.billingType = billingType;
                  return this;
            }

            public Builder customerId(Integer customerId) {
                  this.customerId = customerId;
                  return this;
            }

            public ZepProject build() {
                  return new ZepProject(this);
            }
      }
}

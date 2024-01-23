package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor @AllArgsConstructor
public class ZepEmployee {
        private String username;
        private String firstname;
        private String lastname;
        @JsonProperty("personal_number")
        private String personalNumber;
        private String street;
        private String zip;
        private String city;
        private String country;
        private String abbreviation;
        private ZepSalutation salutation;
        private String title;
        private String email;
        private String phone;
        private String mobile;
        private String fax;
        @JsonProperty("private_phone")
        private String privatePhone;
        private String birthdate;
        private String iban;
        private String bic;
        @JsonProperty("account_no")
        private Integer accountNo;
        @JsonProperty("bank_name")
        private String bankName;
        @JsonProperty("bank_code")
        private String bankCode;
        private String currency;
        @JsonProperty("release_date")
        private LocalDateTime releaseDate;
        private Double vat;
        @JsonProperty("price_group")
        private String priceGroup;
        private ZepEmployment employment;
        private ZepRights rights;
        @JsonProperty("department_id")
        private Integer departmentId;
        private String language;
        @JsonProperty("personio_id")
        private Integer personioId;
        @JsonProperty("cost_bearer")
        private String costBearer;
        @JsonProperty("tax_id")
        private Integer taxId;
        private LocalDateTime created;
        private LocalDateTime modified;
        @JsonProperty("creditor_number")
        private Integer creditorNumber;
        private Collection<String> categories;
        @JsonProperty("absences_count")
        private Integer absencesCount;

        @JsonIgnoreProperties
        ZepEmploymentPeriod[] employmentPeriods;

        @JsonIgnoreProperties
        Map<DayOfWeek, Duration> regularWorkingHours;
}

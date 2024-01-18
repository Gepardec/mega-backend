package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Collection;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor @AllArgsConstructor
public class ZepEmployee {
        private String username;
        private String firstname;
        private String lastname;
        private String personalNumber;
        private String street;
        private String zip;
        private String city;
        private String country;
        private String abbreviation;
        private String salutation;
        private String title;
        private String email;
        private String phone;
        private String mobile;
        private String fax;
        private String privatePhone;
        private String birthdate;
        private String iban;
        private String bic;
        private String accountNo;
        private String bankName;
        private String bankCode;
        private String currency;
        private String releaseDate;
        private String vat;
        private String priceGroup;
        private ZepEmployment employment;
        private ZepRights rights;
        private String departmentId;
        private String language;
        private String personioId;
        private String costBearer;
        private String taxId;
        private String created;
        private String modified;
        private String creditorNumber;
        private Collection<String> categories;
        private int absencesCount;

        @JsonIgnoreProperties
        ZepEmploymentPeriod[] employmentPeriods;
}

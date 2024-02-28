package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

// Create record fields with line seperation
public record ZepEmployee (
        String username,
        String firstname,
        String lastname,
        String personalNumber,
        String street,
        String zip,
        String city,
        String country,
        String abbreviation,
        ZepSalutation salutation,
        String title,
        String email,
        String phone,
        String mobile,
        String fax,
        String privatePhone,
        String birthdate,
        String iban,
        String bic,
        Integer accountNo,
        String bankName,
        String bankCode,
        String currency,
        LocalDate releaseDate,
        Double vat,
        String priceGroup,
        ZepEmployment employment,
        ZepRights rights,
        Integer departmentId,
        ZepLanguage language,
        Integer personioId,
        String costBearer,
        Integer taxId,
        LocalDateTime created,
        LocalDateTime modified,
        Integer creditorNumber,
        Collection<ZepCategory> categories,
        Collection<ZepDynamicAttribute> dynamicAttributes,
        Integer absencesCount
) {

        @JsonCreator
        public ZepEmployee(Builder builder) {
                this(builder.username,
                     builder.firstname,
                     builder.lastname,
                     builder.personalNumber,
                     builder.street,
                     builder.zip,
                     builder.city,
                     builder.country,
                     builder.abbreviation,
                     builder.salutation,
                     builder.title,
                     builder.email,
                     builder.phone,
                     builder.mobile,
                     builder.fax,
                     builder.privatePhone,
                     builder.birthdate,
                     builder.iban,
                     builder.bic,
                     builder.accountNo,
                     builder.bankName,
                     builder.bankCode,
                     builder.currency,
                     builder.releaseDate,
                     builder.vat,
                     builder.priceGroup,
                     builder.employment,
                     builder.rights,
                     builder.departmentId,
                     builder.language,
                     builder.personioId,
                     builder.costBearer,
                     builder.taxId,
                     builder.created,
                     builder.modified,
                     builder.creditorNumber,
                     builder.categories,
                     builder.dynamicAttributes,
                     builder.absencesCount
                );
        }

        public static Builder builder() {
                return Builder.aZepEmployee();
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Builder {
                @JsonProperty
                private String username;
                @JsonProperty
                private String firstname;
                @JsonProperty
                private String lastname;
                @JsonProperty("personal_number")
                private String personalNumber;
                @JsonProperty
                private String street;
                @JsonProperty
                private String zip;
                @JsonProperty
                private String city;
                @JsonProperty
                private String country;
                @JsonProperty
                private String abbreviation;
                @JsonProperty
                private ZepSalutation salutation;
                @JsonProperty
                private String title;
                @JsonProperty
                private String email;
                @JsonProperty
                private String phone;
                @JsonProperty
                private String mobile;
                @JsonProperty
                private String fax;
                @JsonProperty("private_phone")
                private String privatePhone;
                @JsonProperty
                private String birthdate;
                @JsonProperty
                private String iban;
                @JsonProperty
                private String bic;
                @JsonProperty("account_no")
                private Integer accountNo;
                @JsonProperty("bank_name")
                private String bankName;
                @JsonProperty("bank_code")
                private String bankCode;
                @JsonProperty
                private String currency;
                @JsonProperty("release_date")
                private LocalDate releaseDate;
                @JsonProperty
                private Double vat;
                @JsonProperty("price_group")
                private String priceGroup;
                @JsonProperty
                private ZepEmployment employment;
                @JsonProperty
                private ZepRights rights;
                @JsonProperty("department_id")
                private Integer departmentId;
                @JsonProperty
                private ZepLanguage language;
                @JsonProperty("personio_id")
                private Integer personioId;
                @JsonProperty("cost_bearer")
                private String costBearer;
                @JsonProperty("tax_id")
                private Integer taxId;
                @JsonProperty
                private LocalDateTime created;
                @JsonProperty
                private LocalDateTime modified;
                @JsonProperty("creditor_number")
                private Integer creditorNumber;
                @JsonProperty
                private Collection<ZepCategory> categories;
                @JsonProperty("absences_count")
                private Integer absencesCount;

                @JsonProperty
                private Collection<ZepDynamicAttribute> dynamicAttributes;


                public Builder username(String username) {
                        this.username = username;
                        return this;
                }

                public Builder firstname(String firstname) {
                        this.firstname = firstname;
                        return this;
                }

                public Builder lastname(String lastname) {
                        this.lastname = lastname;
                        return this;
                }

                public Builder personalNumber(String personalNumber) {
                        this.personalNumber = personalNumber;
                        return this;
                }

                public Builder street(String street) {
                        this.street = street;
                        return this;
                }

                public Builder zip(String zip) {
                        this.zip = zip;
                        return this;
                }

                public Builder city(String city) {
                        this.city = city;
                        return this;
                }

                public Builder country(String country) {
                        this.country = country;
                        return this;
                }

                public Builder abbreviation(String abbreviation) {
                        this.abbreviation = abbreviation;
                        return this;
                }

                public Builder salutation(ZepSalutation salutation) {
                        this.salutation = salutation;
                        return this;
                }

                public Builder title(String title) {
                        this.title = title;
                        return this;
                }

                public Builder email(String email) {
                        this.email = email;
                        return this;
                }

                public Builder phone(String phone) {
                        this.phone = phone;
                        return this;
                }

                public Builder mobile(String mobile) {
                        this.mobile = mobile;
                        return this;
                }

                public Builder fax(String fax) {
                        this.fax = fax;
                        return this;
                }

                public Builder privatePhone(String privatePhone) {
                        this.privatePhone = privatePhone;
                        return this;
                }

                public Builder birthdate(String birthdate) {
                        this.birthdate = birthdate;
                        return this;
                }

                public Builder iban(String iban) {
                        this.iban = iban;
                        return this;
                }

                public Builder bic(String bic) {
                        this.bic = bic;
                        return this;
                }

                public Builder accountNo(Integer accountNo) {
                        this.accountNo = accountNo;
                        return this;
                }

                public Builder bankName(String bankName) {
                        this.bankName = bankName;
                        return this;
                }

                public Builder bankCode(String bankCode) {
                        this.bankCode = bankCode;
                        return this;
                }

                public Builder currency(String currency) {
                        this.currency = currency;
                        return this;
                }

                public Builder releaseDate(LocalDate releaseDate) {
                        this.releaseDate = releaseDate;
                        return this;
                }

                public Builder vat(Double vat) {
                        this.vat = vat;
                        return this;
                }

                public Builder priceGroup(String priceGroup) {
                        this.priceGroup = priceGroup;
                        return this;
                }

                public Builder employment(ZepEmployment employment) {
                        this.employment = employment;
                        return this;
                }

                public Builder rights(ZepRights rights) {
                        this.rights = rights;
                        return this;
                }

                public Builder departmentId(Integer departmentId) {
                        this.departmentId = departmentId;
                        return this;
                }

                public Builder language(ZepLanguage language) {
                        this.language = language;
                        return this;
                }

                public Builder personioId(Integer personioId) {
                        this.personioId = personioId;
                        return this;
                }

                public Builder costBearer(String costBearer) {
                        this.costBearer = costBearer;
                        return this;
                }

                public Builder taxId(Integer taxId) {
                        this.taxId = taxId;
                        return this;
                }

                public Builder created(LocalDateTime created) {
                        this.created = created;
                        return this;
                }

                public Builder modified(LocalDateTime modified) {
                        this.modified = modified;
                        return this;
                }

                public Builder creditorNumber(Integer creditorNumber) {
                        this.creditorNumber = creditorNumber;
                        return this;
                }

                public Builder categories(Collection<ZepCategory> categories) {
                        this.categories = categories;
                        return this;
                }

                public Builder absencesCount(Integer absencesCount) {
                        this.absencesCount = absencesCount;
                        return this;
                }

                public Builder dynamicAttributes(Collection<ZepDynamicAttribute> dynamicAttributes) {
                        this.dynamicAttributes = dynamicAttributes;
                        return this;
                }

                public ZepEmployee build() {
                        return new ZepEmployee(this);
                }


                public static Builder aZepEmployee() {
                        return new Builder();
                }


                public ZepEmployee createZepEmployee() {
                        return new ZepEmployee(this);
                }
        }
}

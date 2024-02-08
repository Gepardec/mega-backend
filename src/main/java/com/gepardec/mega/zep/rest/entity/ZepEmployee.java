package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepEmployee {

        private final String username;
        private final String firstname;
        private final String lastname;
        private final String personalNumber;
        private final String street;
        private final String zip;
        private final String city;
        private final String country;
        private final String abbreviation;
        private final ZepSalutation salutation;
        private final String title;
        private final String email;
        private final String phone;
        private final String mobile;
        private final String fax;
        private final String privatePhone;
        private final String birthdate;
        private final String iban;
        private final String bic;
        private final Integer accountNo;
        private final String bankName;
        private final String bankCode;
        private final String currency;
        private final LocalDate releaseDate;
        private final Double vat;
        private final String priceGroup;
        private final ZepEmployment employment;
        private final ZepRights rights;
        private final Integer departmentId;
        private final ZepLanguage language;
        private final Integer personioId;
        private final String costBearer;
        private final Integer taxId;
        private final LocalDateTime created;
        private final LocalDateTime modified;
        private final Integer creditorNumber;
        private final Collection<ZepCategory> categories;
        private final Collection<ZepDynamicAttribute> dynamicAttributes;
        private final Integer absencesCount;


        public ZepEmployee(Builder builder) {
                this.username = builder.username;
                this.firstname = builder.firstname;
                this.lastname = builder.lastname;
                this.personalNumber = builder.personalNumber;
                this.street = builder.street;
                this.zip = builder.zip;
                this.city = builder.city;
                this.country = builder.country;
                this.abbreviation = builder.abbreviation;
                this.salutation = builder.salutation;
                this.title = builder.title;
                this.email = builder.email;
                this.phone = builder.phone;
                this.mobile = builder.mobile;
                this.fax = builder.fax;
                this.privatePhone = builder.privatePhone;
                this.birthdate = builder.birthdate;
                this.iban = builder.iban;
                this.bic = builder.bic;
                this.accountNo = builder.accountNo;
                this.bankName = builder.bankName;
                this.bankCode = builder.bankCode;
                this.currency = builder.currency;
                this.releaseDate = builder.releaseDate;
                this.vat = builder.vat;
                this.priceGroup = builder.priceGroup;
                this.employment = builder.employment;
                this.rights = builder.rights;
                this.departmentId = builder.departmentId;
                this.language = builder.language;
                this.personioId = builder.personioId;
                this.costBearer = builder.costBearer;
                this.taxId = builder.taxId;
                this.created = builder.created;
                this.modified = builder.modified;
                this.creditorNumber = builder.creditorNumber;
                this.categories = builder.categories;
                this.absencesCount = builder.absencesCount;
                this.dynamicAttributes = builder.dynamicAttributes;
        }

        public String getUsername() {
                return username;
        }

        

        public String getFirstname() {
                return firstname;
        }

        

        public String getLastname() {
                return lastname;
        }

        

        public String getPersonalNumber() {
                return personalNumber;
        }

        

        public String getStreet() {
                return street;
        }

        

        public String getZip() {
                return zip;
        }

        

        public String getCity() {
                return city;
        }

        

        public String getCountry() {
                return country;
        }

        

        public String getAbbreviation() {
                return abbreviation;
        }

        

        public ZepSalutation getSalutation() {
                return salutation;
        }

        

        public String getTitle() {
                return title;
        }

        

        public String getEmail() {
                return email;
        }

        

        public String getPhone() {
                return phone;
        }

        

        public String getMobile() {
                return mobile;
        }

        

        public String getFax() {
                return fax;
        }

        

        public String getPrivatePhone() {
                return privatePhone;
        }

        

        public String getBirthdate() {
                return birthdate;
        }

        

        public String getIban() {
                return iban;
        }

        

        public String getBic() {
                return bic;
        }

        

        public Integer getAccountNo() {
                return accountNo;
        }

        

        public String getBankName() {
                return bankName;
        }

        

        public String getBankCode() {
                return bankCode;
        }

        

        public String getCurrency() {
                return currency;
        }

        

        public LocalDate getReleaseDate() {
                return releaseDate;
        }

        

        public Double getVat() {
                return vat;
        }

        

        public String getPriceGroup() {
                return priceGroup;
        }

        

        public ZepEmployment getEmployment() {
                return employment;
        }

        

        public ZepRights getRights() {
                return rights;
        }

        

        public Integer getDepartmentId() {
                return departmentId;
        }

        

        public ZepLanguage getLanguage() {
                return language;
        }

        

        public Integer getPersonioId() {
                return personioId;
        }

        

        public String getCostBearer() {
                return costBearer;
        }

        

        public Integer getTaxId() {
                return taxId;
        }

        

        public LocalDateTime getCreated() {
                return created;
        }

        

        public LocalDateTime getModified() {
                return modified;
        }

        

        public Integer getCreditorNumber() {
                return creditorNumber;
        }

        

        public Integer getAbsencesCount() {
                return absencesCount;
        }

        

        

        public Collection<ZepDynamicAttribute> getDynamicAttributes() {
                return dynamicAttributes;
        }


        public static Builder builder() {
                return Builder.aZepEmployee();
        }

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

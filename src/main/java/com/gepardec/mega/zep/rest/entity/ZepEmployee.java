package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

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
        private LocalDate releaseDate;
        private Double vat;
        @JsonProperty("price_group")
        private String priceGroup;
        private ZepEmployment employment;
        private ZepRights rights;
        @JsonProperty("department_id")
        private Integer departmentId;
        private ZepLanguage language;
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

        public ZepEmployee() {
        }

        public ZepEmployee(String username, String firstname, String lastname, String personalNumber, String street, String zip, String city, String country, String abbreviation, ZepSalutation salutation, String title, String email, String phone, String mobile, String fax, String privatePhone, String birthdate, String iban, String bic, Integer accountNo, String bankName, String bankCode, String currency, LocalDate
                releaseDate, Double vat, String priceGroup, ZepEmployment employment, ZepRights rights, Integer departmentId, ZepLanguage language, Integer personioId, String costBearer, Integer taxId, LocalDateTime created, LocalDateTime modified, Integer creditorNumber, Collection<String> categories, Integer absencesCount) {
                this.username = username;
                this.firstname = firstname;
                this.lastname = lastname;
                this.personalNumber = personalNumber;
                this.street = street;
                this.zip = zip;
                this.city = city;
                this.country = country;
                this.abbreviation = abbreviation;
                this.salutation = salutation;
                this.title = title;
                this.email = email;
                this.phone = phone;
                this.mobile = mobile;
                this.fax = fax;
                this.privatePhone = privatePhone;
                this.birthdate = birthdate;
                this.iban = iban;
                this.bic = bic;
                this.accountNo = accountNo;
                this.bankName = bankName;
                this.bankCode = bankCode;
                this.currency = currency;
                this.releaseDate = releaseDate;
                this.vat = vat;
                this.priceGroup = priceGroup;
                this.employment = employment;
                this.rights = rights;
                this.departmentId = departmentId;
                this.language = language;
                this.personioId = personioId;
                this.costBearer = costBearer;
                this.taxId = taxId;
                this.created = created;
                this.modified = modified;
                this.creditorNumber = creditorNumber;
                this.categories = categories;
                this.absencesCount = absencesCount;
        }

        public String getUsername() {
                return username;
        }

        public void setUsername(String username) {
                this.username = username;
        }

        public String getFirstname() {
                return firstname;
        }

        public void setFirstname(String firstname) {
                this.firstname = firstname;
        }

        public String getLastname() {
                return lastname;
        }

        public void setLastname(String lastname) {
                this.lastname = lastname;
        }

        public String getPersonalNumber() {
                return personalNumber;
        }

        public void setPersonalNumber(String personalNumber) {
                this.personalNumber = personalNumber;
        }

        public String getStreet() {
                return street;
        }

        public void setStreet(String street) {
                this.street = street;
        }

        public String getZip() {
                return zip;
        }

        public void setZip(String zip) {
                this.zip = zip;
        }

        public String getCity() {
                return city;
        }

        public void setCity(String city) {
                this.city = city;
        }

        public String getCountry() {
                return country;
        }

        public void setCountry(String country) {
                this.country = country;
        }

        public String getAbbreviation() {
                return abbreviation;
        }

        public void setAbbreviation(String abbreviation) {
                this.abbreviation = abbreviation;
        }

        public ZepSalutation getSalutation() {
                return salutation;
        }

        public void setSalutation(ZepSalutation salutation) {
                this.salutation = salutation;
        }

        public String getTitle() {
                return title;
        }

        public void setTitle(String title) {
                this.title = title;
        }

        public String getEmail() {
                return email;
        }

        public void setEmail(String email) {
                this.email = email;
        }

        public String getPhone() {
                return phone;
        }

        public void setPhone(String phone) {
                this.phone = phone;
        }

        public String getMobile() {
                return mobile;
        }

        public void setMobile(String mobile) {
                this.mobile = mobile;
        }

        public String getFax() {
                return fax;
        }

        public void setFax(String fax) {
                this.fax = fax;
        }

        public String getPrivatePhone() {
                return privatePhone;
        }

        public void setPrivatePhone(String privatePhone) {
                this.privatePhone = privatePhone;
        }

        public String getBirthdate() {
                return birthdate;
        }

        public void setBirthdate(String birthdate) {
                this.birthdate = birthdate;
        }

        public String getIban() {
                return iban;
        }

        public void setIban(String iban) {
                this.iban = iban;
        }

        public String getBic() {
                return bic;
        }

        public void setBic(String bic) {
                this.bic = bic;
        }

        public Integer getAccountNo() {
                return accountNo;
        }

        public void setAccountNo(Integer accountNo) {
                this.accountNo = accountNo;
        }

        public String getBankName() {
                return bankName;
        }

        public void setBankName(String bankName) {
                this.bankName = bankName;
        }

        public String getBankCode() {
                return bankCode;
        }

        public void setBankCode(String bankCode) {
                this.bankCode = bankCode;
        }

        public String getCurrency() {
                return currency;
        }

        public void setCurrency(String currency) {
                this.currency = currency;
        }

        public LocalDate getReleaseDate() {
                return releaseDate;
        }

        public void setReleaseDate(LocalDate releaseDate) {
                this.releaseDate = releaseDate;
        }

        public Double getVat() {
                return vat;
        }

        public void setVat(Double vat) {
                this.vat = vat;
        }

        public String getPriceGroup() {
                return priceGroup;
        }

        public void setPriceGroup(String priceGroup) {
                this.priceGroup = priceGroup;
        }

        public ZepEmployment getEmployment() {
                return employment;
        }

        public void setEmployment(ZepEmployment employment) {
                this.employment = employment;
        }

        public ZepRights getRights() {
                return rights;
        }

        public void setRights(ZepRights rights) {
                this.rights = rights;
        }

        public Integer getDepartmentId() {
                return departmentId;
        }

        public void setDepartmentId(Integer departmentId) {
                this.departmentId = departmentId;
        }

        public ZepLanguage getLanguage() {
                return language;
        }

        public void setLanguage(ZepLanguage language) {
                this.language = language;
        }

        public Integer getPersonioId() {
                return personioId;
        }

        public void setPersonioId(Integer personioId) {
                this.personioId = personioId;
        }

        public String getCostBearer() {
                return costBearer;
        }

        public void setCostBearer(String costBearer) {
                this.costBearer = costBearer;
        }

        public Integer getTaxId() {
                return taxId;
        }

        public void setTaxId(Integer taxId) {
                this.taxId = taxId;
        }

        public LocalDateTime getCreated() {
                return created;
        }

        public void setCreated(LocalDateTime created) {
                this.created = created;
        }

        public LocalDateTime getModified() {
                return modified;
        }

        public void setModified(LocalDateTime modified) {
                this.modified = modified;
        }

        public Integer getCreditorNumber() {
                return creditorNumber;
        }

        public void setCreditorNumber(Integer creditorNumber) {
                this.creditorNumber = creditorNumber;
        }

        public Collection<String> getCategories() {
                return categories;
        }

        public void setCategories(Collection<String> categories) {
                this.categories = categories;
        }

        public Integer getAbsencesCount() {
                return absencesCount;
        }

        public void setAbsencesCount(Integer absencesCount) {
                this.absencesCount = absencesCount;
        }
        public static ZepEmployeeBuilder builder() {
                return new ZepEmployeeBuilder();
        }
}

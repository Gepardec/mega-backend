package com.gepardec.mega.zep.rest.entity;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ZepEmployeeBuilder {
    private String username;
    private String firstname;
    private String lastname;
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
    private String privatePhone;
    private String birthdate;
    private String iban;
    private String bic;
    private Integer accountNo;
    private String bankName;
    private String bankCode;
    private String currency;
    private LocalDateTime releaseDate;
    private Double vat;
    private String priceGroup;
    private ZepEmployment employment;
    private ZepRights rights;
    private Integer departmentId;
    private String language;
    private Integer personioId;
    private String costBearer;
    private Integer taxId;
    private LocalDateTime created;
    private LocalDateTime modified;
    private Integer creditorNumber;
    private Collection<String> categories;
    private Integer absencesCount;

    public ZepEmployeeBuilder username(String username) {
        this.username = username;
        return this;
    }

    public ZepEmployeeBuilder firstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public ZepEmployeeBuilder lastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public ZepEmployeeBuilder personalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
        return this;
    }

    public ZepEmployeeBuilder street(String street) {
        this.street = street;
        return this;
    }

    public ZepEmployeeBuilder zip(String zip) {
        this.zip = zip;
        return this;
    }

    public ZepEmployeeBuilder city(String city) {
        this.city = city;
        return this;
    }

    public ZepEmployeeBuilder country(String country) {
        this.country = country;
        return this;
    }

    public ZepEmployeeBuilder abbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
        return this;
    }

    public ZepEmployeeBuilder salutation(ZepSalutation salutation) {
        this.salutation = salutation;
        return this;
    }

    public ZepEmployeeBuilder title(String title) {
        this.title = title;
        return this;
    }

    public ZepEmployeeBuilder email(String email) {
        this.email = email;
        return this;
    }

    public ZepEmployeeBuilder phone(String phone) {
        this.phone = phone;
        return this;
    }

    public ZepEmployeeBuilder mobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public ZepEmployeeBuilder fax(String fax) {
        this.fax = fax;
        return this;
    }

    public ZepEmployeeBuilder privatePhone(String privatePhone) {
        this.privatePhone = privatePhone;
        return this;
    }

    public ZepEmployeeBuilder birthdate(String birthdate) {
        this.birthdate = birthdate;
        return this;
    }

    public ZepEmployeeBuilder iban(String iban) {
        this.iban = iban;
        return this;
    }

    public ZepEmployeeBuilder bic(String bic) {
        this.bic = bic;
        return this;
    }

    public ZepEmployeeBuilder accountNo(Integer accountNo) {
        this.accountNo = accountNo;
        return this;
    }

    public ZepEmployeeBuilder bankName(String bankName) {
        this.bankName = bankName;
        return this;
    }

    public ZepEmployeeBuilder bankCode(String bankCode) {
        this.bankCode = bankCode;
        return this;
    }

    public ZepEmployeeBuilder currency(String currency) {
        this.currency = currency;
        return this;
    }

    public ZepEmployeeBuilder releaseDate(LocalDateTime releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    public ZepEmployeeBuilder vat(Double vat) {
        this.vat = vat;
        return this;
    }

    public ZepEmployeeBuilder priceGroup(String priceGroup) {
        this.priceGroup = priceGroup;
        return this;
    }

    public ZepEmployeeBuilder employment(ZepEmployment employment) {
        this.employment = employment;
        return this;
    }

    public ZepEmployeeBuilder rights(ZepRights rights) {
        this.rights = rights;
        return this;
    }

    public ZepEmployeeBuilder departmentId(Integer departmentId) {
        this.departmentId = departmentId;
        return this;
    }

    public ZepEmployeeBuilder language(String language) {
        this.language = language;
        return this;
    }

    public ZepEmployeeBuilder personioId(Integer personioId) {
        this.personioId = personioId;
        return this;
    }

    public ZepEmployeeBuilder costBearer(String costBearer) {
        this.costBearer = costBearer;
        return this;
    }

    public ZepEmployeeBuilder taxId(Integer taxId) {
        this.taxId = taxId;
        return this;
    }

    public ZepEmployeeBuilder created(LocalDateTime created) {
        this.created = created;
        return this;
    }

    public ZepEmployeeBuilder modified(LocalDateTime modified) {
        this.modified = modified;
        return this;
    }

    public ZepEmployeeBuilder creditorNumber(Integer creditorNumber) {
        this.creditorNumber = creditorNumber;
        return this;
    }

    public ZepEmployeeBuilder categories(Collection<String> categories) {
        this.categories = categories;
        return this;
    }

    public ZepEmployeeBuilder absencesCount(Integer absencesCount) {
        this.absencesCount = absencesCount;
        return this;
    }


    public ZepEmployee build() {
        return new ZepEmployee(username, firstname, lastname, personalNumber, street, zip, city, country, abbreviation, salutation, title, email, phone, mobile, fax, privatePhone, birthdate, iban, bic, accountNo, bankName, bankCode, currency, releaseDate, vat, priceGroup, employment, rights, departmentId, language, personioId, costBearer, taxId, created, modified, creditorNumber, categories, absencesCount);
    }
}

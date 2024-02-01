package com.gepardec.mega.zep.rest.entity;

import java.time.LocalDateTime;
import java.util.List;

public class ZepProjectBuilder {
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String comments;
    private String costObject;
    private String costObjectIdentifier;
    private LocalDateTime created;
    private LocalDateTime modified;
    private List<String> keywords;
    private String referenceOrder;
    private String referenceCommission;
    private String referenceProcurement;
    private String referenceObject;
    private String language;
    private String currency;
    private String url;
    private String locationAddress;
    private String locationCity;
    private String locationState;
    private String locationCountry;
    private String revenueAccount;
    private String customerId;
    private Integer customerContactId;
    private String customerProjectReference;
    private Integer customerBillingAddressId;
    private Integer customerShippingAddressId;
    private String hasMultipleCustomers;
    private Integer departmentId;
    private Integer billingType;
    private Integer billingTasks;
    private String planHours;
    private String planHoursPerDay;
    private Boolean planCanExceed;
    private Double planWarningPercent;
    private Double planWarningPercent2;
    private Double planWarningPercent3;
    private Double planWage;
    private String planExpenses;
    private String planExpensesTravel;
    private Double planHoursDone;
    private Double planHoursInvoiced;
    private Integer tasksCount;
    private Integer employeesCount;
    private Integer activitiesCount;

    public ZepProjectBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public ZepProjectBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ZepProjectBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ZepProjectBuilder startDate(LocalDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public ZepProjectBuilder endDate(LocalDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public ZepProjectBuilder status(String status) {
        this.status = status;
        return this;
    }

    public ZepProjectBuilder comments(String comments) {
        this.comments = comments;
        return this;
    }

    public ZepProjectBuilder costObject(String costObject) {
        this.costObject = costObject;
        return this;
    }

    public ZepProjectBuilder costObjectIdentifier(String costObjectIdentifier) {
        this.costObjectIdentifier = costObjectIdentifier;
        return this;
    }

    public ZepProjectBuilder created(LocalDateTime created) {
        this.created = created;
        return this;
    }

    public ZepProjectBuilder modified(LocalDateTime modified) {
        this.modified = modified;
        return this;
    }

    public ZepProjectBuilder keywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public ZepProjectBuilder referenceOrder(String referenceOrder) {
        this.referenceOrder = referenceOrder;
        return this;
    }

    public ZepProjectBuilder referenceCommission(String referenceCommission) {
        this.referenceCommission = referenceCommission;
        return this;
    }

    public ZepProjectBuilder referenceProcurement(String referenceProcurement) {
        this.referenceProcurement = referenceProcurement;
        return this;
    }

    public ZepProjectBuilder referenceObject(String referenceObject) {
        this.referenceObject = referenceObject;
        return this;
    }

    public ZepProjectBuilder language(String language) {
        this.language = language;
        return this;
    }

    public ZepProjectBuilder currency(String currency) {
        this.currency = currency;
        return this;
    }

    public ZepProjectBuilder url(String url) {
        this.url = url;
        return this;
    }

    public ZepProjectBuilder locationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
        return this;
    }

    public ZepProjectBuilder locationCity(String locationCity) {
        this.locationCity = locationCity;
        return this;
    }

    public ZepProjectBuilder locationState(String locationState) {
        this.locationState = locationState;
        return this;
    }

    public ZepProjectBuilder locationCountry(String locationCountry) {
        this.locationCountry = locationCountry;
        return this;
    }

    public ZepProjectBuilder revenueAccount(String revenueAccount) {
        this.revenueAccount = revenueAccount;
        return this;
    }

    public ZepProjectBuilder customerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public ZepProjectBuilder customerContactId(Integer customerContactId) {
        this.customerContactId = customerContactId;
        return this;
    }

    public ZepProjectBuilder customerProjectReference(String customerProjectReference) {
        this.customerProjectReference = customerProjectReference;
        return this;
    }

    public ZepProjectBuilder customerBillingAddressId(Integer customerBillingAddressId) {
        this.customerBillingAddressId = customerBillingAddressId;
        return this;
    }

    public ZepProjectBuilder customerShippingAddressId(Integer customerShippingAddressId) {
        this.customerShippingAddressId = customerShippingAddressId;
        return this;
    }

    public ZepProjectBuilder hasMultipleCustomers(String hasMultipleCustomers) {
        this.hasMultipleCustomers = hasMultipleCustomers;
        return this;
    }

    public ZepProjectBuilder departmentId(Integer departmentId) {
        this.departmentId = departmentId;
        return this;
    }

    public ZepProjectBuilder billingType(Integer billingType) {
        this.billingType = billingType;
        return this;
    }

    public ZepProjectBuilder billingTasks(Integer billingTasks) {
        this.billingTasks = billingTasks;
        return this;
    }

    public ZepProjectBuilder planHours(String planHours) {
        this.planHours = planHours;
        return this;
    }

    public ZepProjectBuilder planHoursPerDay(String planHoursPerDay) {
        this.planHoursPerDay = planHoursPerDay;
        return this;
    }

    public ZepProjectBuilder planCanExceed(Boolean planCanExceed) {
        this.planCanExceed = planCanExceed;
        return this;
    }

    public ZepProjectBuilder planWarningPercent(Double planWarningPercent) {
        this.planWarningPercent = planWarningPercent;
        return this;
    }

    public ZepProjectBuilder planWarningPercent2(Double planWarningPercent2) {
        this.planWarningPercent2 = planWarningPercent2;
        return this;
    }

    public ZepProjectBuilder planWarningPercent3(Double planWarningPercent3) {
        this.planWarningPercent3 = planWarningPercent3;
        return this;
    }

    public ZepProjectBuilder planWage(Double planWage) {
        this.planWage = planWage;
        return this;
    }

    public ZepProjectBuilder planExpenses(String planExpenses) {
        this.planExpenses = planExpenses;
        return this;
    }

    public ZepProjectBuilder planExpensesTravel(String planExpensesTravel) {
        this.planExpensesTravel = planExpensesTravel;
        return this;
    }

    public ZepProjectBuilder planHoursDone(Double planHoursDone) {
        this.planHoursDone = planHoursDone;
        return this;
    }

    public ZepProjectBuilder planHoursInvoiced(Double planHoursInvoiced) {
        this.planHoursInvoiced = planHoursInvoiced;
        return this;
    }

    public ZepProjectBuilder tasksCount(Integer tasksCount) {
        this.tasksCount = tasksCount;
        return this;
    }

    public ZepProjectBuilder employeesCount(Integer employeesCount) {
        this.employeesCount = employeesCount;
        return this;
    }

    public ZepProjectBuilder activitiesCount(Integer activitiesCount) {
        this.activitiesCount = activitiesCount;
        return this;
    }
    public ZepProject build() {
        return new ZepProject(id, name, description, startDate, endDate, status, comments, costObject, costObjectIdentifier, created, modified, keywords, referenceOrder, referenceCommission, referenceProcurement, referenceObject, language, currency, url, locationAddress, locationCity, locationState, locationCountry, revenueAccount, customerId, customerContactId, customerProjectReference, customerBillingAddressId, customerShippingAddressId, hasMultipleCustomers, departmentId, billingType, billingTasks, planHours, planHoursPerDay, planCanExceed, planWarningPercent, planWarningPercent2, planWarningPercent3, planWage, planExpenses, planExpensesTravel, planHoursDone, planHoursInvoiced, tasksCount, employeesCount, activitiesCount);
    }
}

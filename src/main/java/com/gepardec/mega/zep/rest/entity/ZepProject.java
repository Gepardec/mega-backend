package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record ZepProject (
        Integer id,
        String name,
        String description,
        LocalDateTime startDate,
        LocalDateTime endDate,
//        String status,
        String comments,
        String costObject,
        String costObjectIdentifier,
        LocalDateTime created,
        LocalDateTime modified,
        List<String> keywords,
        String referenceOrder,
        String referenceCommission,
        String referenceProcurement,
        String referenceObject,
        String language,
        String currency,
        String url,
        String locationAddress,
        String locationCity,
        String locationState,
        String locationCountry,
        String revenueAccount,
        String customerId,
        Integer customerContactId,
        String customerProjectReference,
        Integer customerBillingAddressId,
        Integer customerShippingAddressId,
        String hasMultipleCustomers,
        Integer departmentId,
        Integer billingType,
        Integer billingTasks,
        String planHours,
        String planHoursPerDay,
        Boolean planCanExceed,
        Double planWarningPercent,
        Double planWarningPercent2,
        Double planWarningPercent3,
        Double planWage,
        String planExpenses,
        String planExpensesTravel,
        Double planHoursDone,
        Double planHoursInvoiced,
        Integer tasksCount,
        Integer employeesCount,
        Integer activitiesCount
) {

      public static Builder builder() {
            return Builder.aZepProject();
      }

      @JsonCreator
      public ZepProject(Builder builder) {
            this(builder.id,
                    builder.name,
                    builder.description,
                    builder.startDate,
                    builder.endDate,
//                    builder.status,
                    builder.comments,
                    builder.costObject,
                    builder.costObjectIdentifier,
                    builder.created,
                    builder.modified,
                    builder.keywords,
                    builder.referenceOrder,
                    builder.referenceCommission,
                    builder.referenceProcurement,
                    builder.referenceObject,
                    builder.language,
                    builder.currency,
                    builder.url,
                    builder.locationAddress,
                    builder.locationCity,
                    builder.locationState,
                    builder.locationCountry,
                    builder.revenueAccount,
                    builder.customerId,
                    builder.customerContactId,
                    builder.customerProjectReference,
                    builder.customerBillingAddressId,
                    builder.customerShippingAddressId,
                    builder.hasMultipleCustomers,
                    builder.departmentId,
                    builder.billingType,
                    builder.billingTasks,
                    builder.planHours,
                    builder.planHoursPerDay,
                    builder.planCanExceed,
                    builder.planWarningPercent,
                    builder.planWarningPercent2,
                    builder.planWarningPercent3,
                    builder.planWage,
                    builder.planExpenses,
                    builder.planExpensesTravel,
                    builder.planHoursDone,
                    builder.planHoursInvoiced,
                    builder.tasksCount,
                    builder.employeesCount,
                    builder.activitiesCount
            );

      }

      @JsonIgnoreProperties(ignoreUnknown = true)
      public static final class Builder {
            @JsonProperty
            private Integer id;
            @JsonProperty
            private String name;
            @JsonProperty
            private String description;

            @JsonProperty("start_date")
            private LocalDateTime startDate;

            @JsonProperty("end_date")
            private LocalDateTime endDate;
//            @JsonProperty
//            private String status;
            @JsonProperty
            private String comments;

            @JsonProperty("cost_object")
            private String costObject;
            @JsonProperty("cost_object_identifier")
            private String costObjectIdentifier;
            @JsonProperty
            private LocalDateTime created;
            @JsonProperty
            private LocalDateTime modified;
            @JsonProperty
            private List<String> keywords;

            @JsonProperty("reference_order")
            private String referenceOrder;
            @JsonProperty("reference_commission")
            private String referenceCommission;
            @JsonProperty("reference_procurement")
            private String referenceProcurement;
            @JsonProperty("reference_object")
            private String referenceObject;
            @JsonProperty
            private String language;
            @JsonProperty
            private String currency;
            @JsonProperty
            private String url;
            @JsonProperty("location_address")
            private String locationAddress;
            @JsonProperty("location_city")
            private String locationCity;
            @JsonProperty("location_state")
            private String locationState;
            @JsonProperty("location_country")
            private String locationCountry;
            @JsonProperty("revenue_account")
            private String revenueAccount;
            @JsonProperty("customer_id")
            private String customerId;
            @JsonProperty("customer_contact_id")
            private Integer customerContactId;
            @JsonProperty("customer_project_reference")
            private String customerProjectReference;
            @JsonProperty("customer_billing_address_id")
            private Integer customerBillingAddressId;
            @JsonProperty("customer_shipping_address_id")
            private Integer customerShippingAddressId;
            @JsonProperty("has_multiple_customers")
            private String hasMultipleCustomers;
            @JsonProperty("department_id")
            private Integer departmentId;
            @JsonProperty("billing_type")
            private Integer billingType;
            @JsonProperty("billing_tasks")
            private Integer billingTasks;
            @JsonProperty("plan_hours")
            private String planHours;
            @JsonProperty("plan_hours_per_day")
            private String planHoursPerDay;
            @JsonProperty("plan_can_exceed")
            private Boolean planCanExceed;
            @JsonProperty("plan_warning_percent")
            private Double planWarningPercent;
            @JsonProperty("plan_warning_percent_2")
            private Double planWarningPercent2;
            @JsonProperty("plan_warning_percent_3")
            private Double planWarningPercent3;
            @JsonProperty("plan_wage")
            private Double planWage;
            @JsonProperty("plan_expenses")
            private String planExpenses;
            @JsonProperty("plan_expenses_travel")
            private String planExpensesTravel;
            @JsonProperty("plan_hours_done")
            private Double planHoursDone;
            @JsonProperty("plan_hours_invoiced")
            private Double planHoursInvoiced;
            @JsonProperty("tasks_count")
            private Integer tasksCount;
            @JsonProperty("employees_count")
            private Integer employeesCount;
            @JsonProperty("activities_count")
            private Integer activitiesCount;

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

            public Builder description(String description) {
                  this.description = description;
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

//            public Builder status(String status) {
//                  this.status = status;
//                  return this;
//            }

            public Builder comments(String comments) {
                  this.comments = comments;
                  return this;
            }

            public Builder costObject(String costObject) {
                  this.costObject = costObject;
                  return this;
            }

            public Builder costObjectIdentifier(String costObjectIdentifier) {
                  this.costObjectIdentifier = costObjectIdentifier;
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

            public Builder keywords(List<String> keywords) {
                  this.keywords = keywords;
                  return this;
            }

            public Builder referenceOrder(String referenceOrder) {
                  this.referenceOrder = referenceOrder;
                  return this;
            }

            public Builder referenceCommission(String referenceCommission) {
                  this.referenceCommission = referenceCommission;
                  return this;
            }

            public Builder referenceProcurement(String referenceProcurement) {
                  this.referenceProcurement = referenceProcurement;
                  return this;
            }

            public Builder referenceObject(String referenceObject) {
                  this.referenceObject = referenceObject;
                  return this;
            }

            public Builder language(String language) {
                  this.language = language;
                  return this;
            }

            public Builder currency(String currency) {
                  this.currency = currency;
                  return this;
            }

            public Builder url(String url) {
                  this.url = url;
                  return this;
            }

            public Builder locationAddress(String locationAddress) {
                  this.locationAddress = locationAddress;
                  return this;
            }

            public Builder locationCity(String locationCity) {
                  this.locationCity = locationCity;
                  return this;
            }

            public Builder locationState(String locationState) {
                  this.locationState = locationState;
                  return this;
            }

            public Builder locationCountry(String locationCountry) {
                  this.locationCountry = locationCountry;
                  return this;
            }

            public Builder revenueAccount(String revenueAccount) {
                  this.revenueAccount = revenueAccount;
                  return this;
            }

            public Builder customerId(String customerId) {
                  this.customerId = customerId;
                  return this;
            }

            public Builder customerContactId(Integer customerContactId) {
                  this.customerContactId = customerContactId;
                  return this;
            }

            public Builder customerProjectReference(String customerProjectReference) {
                  this.customerProjectReference = customerProjectReference;
                  return this;
            }

            public Builder customerBillingAddressId(Integer customerBillingAddressId) {
                  this.customerBillingAddressId = customerBillingAddressId;
                  return this;
            }

            public Builder customerShippingAddressId(Integer customerShippingAddressId) {
                  this.customerShippingAddressId = customerShippingAddressId;
                  return this;
            }

            public Builder hasMultipleCustomers(String hasMultipleCustomers) {
                  this.hasMultipleCustomers = hasMultipleCustomers;
                  return this;
            }

            public Builder departmentId(Integer departmentId) {
                  this.departmentId = departmentId;
                  return this;
            }

            public Builder billingType(Integer billingType) {
                  this.billingType = billingType;
                  return this;
            }

            public Builder billingTasks(Integer billingTasks) {
                  this.billingTasks = billingTasks;
                  return this;
            }

            public Builder planHours(String planHours) {
                  this.planHours = planHours;
                  return this;
            }

            public Builder planHoursPerDay(String planHoursPerDay) {
                  this.planHoursPerDay = planHoursPerDay;
                  return this;
            }

            public Builder planCanExceed(Boolean planCanExceed) {
                  this.planCanExceed = planCanExceed;
                  return this;
            }

            public Builder planWarningPercent(Double planWarningPercent) {
                  this.planWarningPercent = planWarningPercent;
                  return this;
            }

            public Builder planWarningPercent2(Double planWarningPercent2) {
                  this.planWarningPercent2 = planWarningPercent2;
                  return this;
            }

            public Builder planWarningPercent3(Double planWarningPercent3) {
                  this.planWarningPercent3 = planWarningPercent3;
                  return this;
            }

            public Builder planWage(Double planWage) {
                  this.planWage = planWage;
                  return this;
            }

            public Builder planExpenses(String planExpenses) {
                  this.planExpenses = planExpenses;
                  return this;
            }

            public Builder planExpensesTravel(String planExpensesTravel) {
                  this.planExpensesTravel = planExpensesTravel;
                  return this;
            }

            public Builder planHoursDone(Double planHoursDone) {
                  this.planHoursDone = planHoursDone;
                  return this;
            }

            public Builder planHoursInvoiced(Double planHoursInvoiced) {
                  this.planHoursInvoiced = planHoursInvoiced;
                  return this;
            }

            public Builder tasksCount(Integer tasksCount) {
                  this.tasksCount = tasksCount;
                  return this;
            }

            public Builder employeesCount(Integer employeesCount) {
                  this.employeesCount = employeesCount;
                  return this;
            }

            public Builder activitiesCount(Integer activitiesCount) {
                  this.activitiesCount = activitiesCount;
                  return this;
            }


            public ZepProject build() {
                  return new ZepProject(this);
            }
      }
}

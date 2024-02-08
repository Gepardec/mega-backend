package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepProject {

      private final Integer id;
      private final String name;
      private final String description;
      private final LocalDateTime startDate;
      private final LocalDateTime endDate;
      private final String status;
      private final String comments;
      private final String costObject;
      private final String costObjectIdentifier;
      private final LocalDateTime created;
      private final LocalDateTime modified;
      private final List<String> keywords;
      private final String referenceOrder;
      private final String referenceCommission;
      private final String referenceProcurement;
      private final String referenceObject;
      private final String language;
      private final String currency;
      private final String url;
      private final String locationAddress;
      private final String locationCity;
      private final String locationState;
      private final String locationCountry;
      private final String revenueAccount;
      private final String customerId;
      private final Integer customerContactId;
      private final String customerProjectReference;
      private final Integer customerBillingAddressId;
      private final Integer customerShippingAddressId;
      private final String hasMultipleCustomers;
      private final Integer departmentId;
      private final Integer billingType;
      private final Integer billingTasks;
      private final String planHours;
      private final String planHoursPerDay;
      private final Boolean planCanExceed;
      private final Double planWarningPercent;
      private final Double planWarningPercent2;
      private final Double planWarningPercent3;
      private final Double planWage;
      private final String planExpenses;
      private final String planExpensesTravel;
      private final Double planHoursDone;
      private final Double planHoursInvoiced;
      private final Integer tasksCount;
      private final Integer employeesCount;
      private final Integer activitiesCount;


      public static Builder builder() {
            return Builder.aZepProject();
      }

      @JsonCreator
      public ZepProject(Builder builder) {
            this.id = builder.id;
            this.name = builder.name;
            this.description = builder.description;
            this.startDate = builder.startDate;
            this.endDate = builder.endDate;
            this.status = builder.status;
            this.comments = builder.comments;
            this.costObject = builder.costObject;
            this.costObjectIdentifier = builder.costObjectIdentifier;
            this.created = builder.created;
            this.modified = builder.modified;
            this.keywords = builder.keywords;
            this.referenceOrder = builder.referenceOrder;
            this.referenceCommission = builder.referenceCommission;
            this.referenceProcurement = builder.referenceProcurement;
            this.referenceObject = builder.referenceObject;
            this.language = builder.language;
            this.currency = builder.currency;
            this.url = builder.url;
            this.locationAddress = builder.locationAddress;
            this.locationCity = builder.locationCity;
            this.locationState = builder.locationState;
            this.locationCountry = builder.locationCountry;
            this.revenueAccount = builder.revenueAccount;
            this.customerId = builder.customerId;
            this.customerContactId = builder.customerContactId;
            this.customerProjectReference = builder.customerProjectReference;
            this.customerBillingAddressId = builder.customerBillingAddressId;
            this.customerShippingAddressId = builder.customerShippingAddressId;
            this.hasMultipleCustomers = builder.hasMultipleCustomers;
            this.departmentId = builder.departmentId;
            this.billingType = builder.billingType;
            this.billingTasks = builder.billingTasks;
            this.planHours = builder.planHours;
            this.planHoursPerDay = builder.planHoursPerDay;
            this.planCanExceed = builder.planCanExceed;
            this.planWarningPercent = builder.planWarningPercent;
            this.planWarningPercent2 = builder.planWarningPercent2;
            this.planWarningPercent3 = builder.planWarningPercent3;
            this.planWage = builder.planWage;
            this.planExpenses = builder.planExpenses;
            this.planExpensesTravel = builder.planExpensesTravel;
            this.planHoursDone = builder.planHoursDone;
            this.planHoursInvoiced = builder.planHoursInvoiced;
            this.tasksCount = builder.tasksCount;
            this.employeesCount = builder.employeesCount;
            this.activitiesCount = builder.activitiesCount;
      }

      public Integer getId() {
            return id;
      }

      

      public String getName() {
            return name;
      }

      

      public String getDescription() {
            return description;
      }

      

      public LocalDateTime getStartDate() {
            return startDate;
      }

      

      public LocalDateTime getEndDate() {
            return endDate;
      }

      

      public String getStatus() {
            return status;
      }

      

      public String getComments() {
            return comments;
      }

      

      public String getCostObject() {
            return costObject;
      }

      

      public String getCostObjectIdentifier() {
            return costObjectIdentifier;
      }

      

      public LocalDateTime getCreated() {
            return created;
      }

      

      public LocalDateTime getModified() {
            return modified;
      }

      

      public List<String> getKeywords() {
            return keywords;
      }

      

      public String getReferenceOrder() {
            return referenceOrder;
      }

      

      public String getReferenceCommission() {
            return referenceCommission;
      }

      

      public String getReferenceProcurement() {
            return referenceProcurement;
      }
      

      public String getReferenceObject() {
            return referenceObject;
      }

      

      public String getLanguage() {
            return language;
      }

      

      public String getCurrency() {
            return currency;
      }

      

      public String getUrl() {
            return url;
      }

      

      public String getLocationAddress() {
            return locationAddress;
      }

      

      public String getLocationCity() {
            return locationCity;
      }

      

      public String getLocationState() {
            return locationState;
      }

      

      public String getLocationCountry() {
            return locationCountry;
      }

      

      public String getRevenueAccount() {
            return revenueAccount;
      }

      

      public String getCustomerId() {
            return customerId;
      }

      

      public Integer getCustomerContactId() {
            return customerContactId;
      }

      

      public String getCustomerProjectReference() {
            return customerProjectReference;
      }

      

      public Integer getCustomerBillingAddressId() {
            return customerBillingAddressId;
      }

      

      public Integer getCustomerShippingAddressId() {
            return customerShippingAddressId;
      }

      

      public String getHasMultipleCustomers() {
            return hasMultipleCustomers;
      }

      

      public Integer getDepartmentId() {
            return departmentId;
      }

      

      public Integer getBillingType() {
            return billingType;
      }

      

      public Integer getBillingTasks() {
            return billingTasks;
      }

      

      public String getPlanHours() {
            return planHours;
      }

      

      public String getPlanHoursPerDay() {
            return planHoursPerDay;
      }

      

      public Boolean getPlanCanExceed() {
            return planCanExceed;
      }

      

      public Double getPlanWarningPercent() {
            return planWarningPercent;
      }

      

      public Double getPlanWarningPercent2() {
            return planWarningPercent2;
      }

      

      public Double getPlanWarningPercent3() {
            return planWarningPercent3;
      }

      

      public Double getPlanWage() {
            return planWage;
      }

      

      public String getPlanExpenses() {
            return planExpenses;
      }

      

      public String getPlanExpensesTravel() {
            return planExpensesTravel;
      }

      

      public Double getPlanHoursDone() {
            return planHoursDone;
      }

      

      public Double getPlanHoursInvoiced() {
            return planHoursInvoiced;
      }

      

      public Integer getTasksCount() {
            return tasksCount;
      }

      

      public Integer getEmployeesCount() {
            return employeesCount;
      }

      

      public Integer getActivitiesCount() {
            return activitiesCount;
      }

      

      public static final class Builder {
            private Integer id;
            private String name;
            private String description;

            @JsonProperty("start_date")
            private LocalDateTime startDate;

            @JsonProperty("end_date")
            private LocalDateTime endDate;
            private String status;
            private String comments;

            @JsonProperty("cost_object")
            private String costObject;
            @JsonProperty("cost_object_identifier")
            private String costObjectIdentifier;
            private LocalDateTime created;
            private LocalDateTime modified;
            private List<String> keywords;

            @JsonProperty("reference_order")
            private String referenceOrder;
            @JsonProperty("reference_commission")
            private String referenceCommission;
            @JsonProperty("reference_procurement")
            private String referenceProcurement;
            @JsonProperty("reference_object")
            private String referenceObject;

            private String language;
            private String currency;
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

            public Builder status(String status) {
                  this.status = status;
                  return this;
            }

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

            public ZepProject build() {
                  return new ZepProject(this);
            }
      }
}

package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.time.LocalDateTime;
import java.util.List;

public class ZepProject {
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
      @JsonProperty("locationAddress")
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

      @JsonIgnore
      private List<ZepProjectEmployee> employees;

      @JsonIgnore
      private List<String> leads;

      public List<ZepProjectEmployee> getEmployees() {
            return employees;
      }

      public void setEmployees(List<ZepProjectEmployee> employees) {
            this.employees = employees;
      }

      public List<String> getLeads() {
            return leads;
      }

      public void setLeads(List<String> leads) {
            this.leads = leads;
      }

      public static ZepProjectBuilder builder() {
            return new ZepProjectBuilder();
      }

      public ZepProject(Integer id, String name, String description, LocalDateTime startDate, LocalDateTime endDate, String status, String comments, String costObject, String costObjectIdentifier, LocalDateTime created, LocalDateTime modified, List<String> keywords, String referenceOrder, String referenceCommission, String referenceProcurement, String referenceObject, String language, String currency, String url, String locationAddress, String locationCity, String locationState, String locationCountry, String revenueAccount, String customerId, Integer customerContactId, String customerProjectReference, Integer customerBillingAddressId, Integer customerShippingAddressId, String hasMultipleCustomers, Integer departmentId, Integer billingType, Integer billingTasks, String planHours, String planHoursPerDay, Boolean planCanExceed, Double planWarningPercent, Double planWarningPercent2, Double planWarningPercent3, Double planWage, String planExpenses, String planExpensesTravel, Double planHoursDone, Double planHoursInvoiced, Integer tasksCount, Integer employeesCount, Integer activitiesCount) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
            this.comments = comments;
            this.costObject = costObject;
            this.costObjectIdentifier = costObjectIdentifier;
            this.created = created;
            this.modified = modified;
            this.keywords = keywords;
            this.referenceOrder = referenceOrder;
            this.referenceCommission = referenceCommission;
            this.referenceProcurement = referenceProcurement;
            this.referenceObject = referenceObject;
            this.language = language;
            this.currency = currency;
            this.url = url;
            this.locationAddress = locationAddress;
            this.locationCity = locationCity;
            this.locationState = locationState;
            this.locationCountry = locationCountry;
            this.revenueAccount = revenueAccount;
            this.customerId = customerId;
            this.customerContactId = customerContactId;
            this.customerProjectReference = customerProjectReference;
            this.customerBillingAddressId = customerBillingAddressId;
            this.customerShippingAddressId = customerShippingAddressId;
            this.hasMultipleCustomers = hasMultipleCustomers;
            this.departmentId = departmentId;
            this.billingType = billingType;
            this.billingTasks = billingTasks;
            this.planHours = planHours;
            this.planHoursPerDay = planHoursPerDay;
            this.planCanExceed = planCanExceed;
            this.planWarningPercent = planWarningPercent;
            this.planWarningPercent2 = planWarningPercent2;
            this.planWarningPercent3 = planWarningPercent3;
            this.planWage = planWage;
            this.planExpenses = planExpenses;
            this.planExpensesTravel = planExpensesTravel;
            this.planHoursDone = planHoursDone;
            this.planHoursInvoiced = planHoursInvoiced;
            this.tasksCount = tasksCount;
            this.employeesCount = employeesCount;
            this.activitiesCount = activitiesCount;
      }

      public Integer getId() {
            return id;
      }

      public void setId(Integer id) {
            this.id = id;
      }

      public String getName() {
            return name;
      }

      public void setName(String name) {
            this.name = name;
      }

      public String getDescription() {
            return description;
      }

      public void setDescription(String description) {
            this.description = description;
      }

      public LocalDateTime getStartDate() {
            return startDate;
      }

      public void setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
      }

      public LocalDateTime getEndDate() {
            return endDate;
      }

      public void setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
      }

      public String getStatus() {
            return status;
      }

      public void setStatus(String status) {
            this.status = status;
      }

      public String getComments() {
            return comments;
      }

      public void setComments(String comments) {
            this.comments = comments;
      }

      public String getCostObject() {
            return costObject;
      }

      public void setCostObject(String costObject) {
            this.costObject = costObject;
      }

      public String getCostObjectIdentifier() {
            return costObjectIdentifier;
      }

      public void setCostObjectIdentifier(String costObjectIdentifier) {
            this.costObjectIdentifier = costObjectIdentifier;
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

      public List<String> getKeywords() {
            return keywords;
      }

      public void setKeywords(List<String> keywords) {
            this.keywords = keywords;
      }

      public String getReferenceOrder() {
            return referenceOrder;
      }

      public void setReferenceOrder(String referenceOrder) {
            this.referenceOrder = referenceOrder;
      }

      public String getReferenceCommission() {
            return referenceCommission;
      }

      public void setReferenceCommission(String referenceCommission) {
            this.referenceCommission = referenceCommission;
      }

      public String getReferenceProcurement() {
            return referenceProcurement;
      }

      public void setReferenceProcurement(String referenceProcurement) {
            this.referenceProcurement = referenceProcurement;
      }

      public String getReferenceObject() {
            return referenceObject;
      }

      public void setReferenceObject(String referenceObject) {
            this.referenceObject = referenceObject;
      }

      public String getLanguage() {
            return language;
      }

      public void setLanguage(String language) {
            this.language = language;
      }

      public String getCurrency() {
            return currency;
      }

      public void setCurrency(String currency) {
            this.currency = currency;
      }

      public String getUrl() {
            return url;
      }

      public void setUrl(String url) {
            this.url = url;
      }

      public String getLocationAddress() {
            return locationAddress;
      }

      public void setLocationAddress(String locationAddress) {
            this.locationAddress = locationAddress;
      }

      public String getLocationCity() {
            return locationCity;
      }

      public void setLocationCity(String locationCity) {
            this.locationCity = locationCity;
      }

      public String getLocationState() {
            return locationState;
      }

      public void setLocationState(String locationState) {
            this.locationState = locationState;
      }

      public String getLocationCountry() {
            return locationCountry;
      }

      public void setLocationCountry(String locationCountry) {
            this.locationCountry = locationCountry;
      }

      public String getRevenueAccount() {
            return revenueAccount;
      }

      public void setRevenueAccount(String revenueAccount) {
            this.revenueAccount = revenueAccount;
      }

      public String getCustomerId() {
            return customerId;
      }

      public void setCustomerId(String customerId) {
            this.customerId = customerId;
      }

      public Integer getCustomerContactId() {
            return customerContactId;
      }

      public void setCustomerContactId(Integer customerContactId) {
            this.customerContactId = customerContactId;
      }

      public String getCustomerProjectReference() {
            return customerProjectReference;
      }

      public void setCustomerProjectReference(String customerProjectReference) {
            this.customerProjectReference = customerProjectReference;
      }

      public Integer getCustomerBillingAddressId() {
            return customerBillingAddressId;
      }

      public void setCustomerBillingAddressId(Integer customerBillingAddressId) {
            this.customerBillingAddressId = customerBillingAddressId;
      }

      public Integer getCustomerShippingAddressId() {
            return customerShippingAddressId;
      }

      public void setCustomerShippingAddressId(Integer customerShippingAddressId) {
            this.customerShippingAddressId = customerShippingAddressId;
      }

      public String getHasMultipleCustomers() {
            return hasMultipleCustomers;
      }

      public void setHasMultipleCustomers(String hasMultipleCustomers) {
            this.hasMultipleCustomers = hasMultipleCustomers;
      }

      public Integer getDepartmentId() {
            return departmentId;
      }

      public void setDepartmentId(Integer departmentId) {
            this.departmentId = departmentId;
      }

      public Integer getBillingType() {
            return billingType;
      }

      public void setBillingType(Integer billingType) {
            this.billingType = billingType;
      }

      public Integer getBillingTasks() {
            return billingTasks;
      }

      public void setBillingTasks(Integer billingTasks) {
            this.billingTasks = billingTasks;
      }

      public String getPlanHours() {
            return planHours;
      }

      public void setPlanHours(String planHours) {
            this.planHours = planHours;
      }

      public String getPlanHoursPerDay() {
            return planHoursPerDay;
      }

      public void setPlanHoursPerDay(String planHoursPerDay) {
            this.planHoursPerDay = planHoursPerDay;
      }

      public Boolean getPlanCanExceed() {
            return planCanExceed;
      }

      public void setPlanCanExceed(Boolean planCanExceed) {
            this.planCanExceed = planCanExceed;
      }

      public Double getPlanWarningPercent() {
            return planWarningPercent;
      }

      public void setPlanWarningPercent(Double planWarningPercent) {
            this.planWarningPercent = planWarningPercent;
      }

      public Double getPlanWarningPercent2() {
            return planWarningPercent2;
      }

      public void setPlanWarningPercent2(Double planWarningPercent2) {
            this.planWarningPercent2 = planWarningPercent2;
      }

      public Double getPlanWarningPercent3() {
            return planWarningPercent3;
      }

      public void setPlanWarningPercent3(Double planWarningPercent3) {
            this.planWarningPercent3 = planWarningPercent3;
      }

      public Double getPlanWage() {
            return planWage;
      }

      public void setPlanWage(Double planWage) {
            this.planWage = planWage;
      }

      public String getPlanExpenses() {
            return planExpenses;
      }

      public void setPlanExpenses(String planExpenses) {
            this.planExpenses = planExpenses;
      }

      public String getPlanExpensesTravel() {
            return planExpensesTravel;
      }

      public void setPlanExpensesTravel(String planExpensesTravel) {
            this.planExpensesTravel = planExpensesTravel;
      }

      public Double getPlanHoursDone() {
            return planHoursDone;
      }

      public void setPlanHoursDone(Double planHoursDone) {
            this.planHoursDone = planHoursDone;
      }

      public Double getPlanHoursInvoiced() {
            return planHoursInvoiced;
      }

      public void setPlanHoursInvoiced(Double planHoursInvoiced) {
            this.planHoursInvoiced = planHoursInvoiced;
      }

      public Integer getTasksCount() {
            return tasksCount;
      }

      public void setTasksCount(Integer tasksCount) {
            this.tasksCount = tasksCount;
      }

      public Integer getEmployeesCount() {
            return employeesCount;
      }

      public void setEmployeesCount(Integer employeesCount) {
            this.employeesCount = employeesCount;
      }

      public Integer getActivitiesCount() {
            return activitiesCount;
      }

      public void setActivitiesCount(Integer activitiesCount) {
            this.activitiesCount = activitiesCount;
      }
}

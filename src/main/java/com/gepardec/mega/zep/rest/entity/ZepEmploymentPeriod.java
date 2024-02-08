package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepEmploymentPeriod {

     private final int id;
     private final String employeeId;
     private final LocalDateTime startDate;
     private final LocalDateTime endDate;
     private final String note;
     private final Double annualLeaveEntitlement;
     private final LocalDateTime beginningOfYear;
     private final double periodHolidayEntitlement;
     private final boolean isHolidayPerYear;
     private final Double dayAbsentInHours;
     private final String created;
     private final String modified;

     @JsonCreator
     public ZepEmploymentPeriod(Builder builder) {
          this.id = builder.id;
          this.employeeId = builder.employeeId;
          this.startDate = builder.startDate;
          this.endDate = builder.endDate;
          this.note = builder.note;
          this.annualLeaveEntitlement = builder.annualLeaveEntitlement;
          this.beginningOfYear = builder.beginningOfYear;
          this.periodHolidayEntitlement = builder.periodHolidayEntitlement;
          this.isHolidayPerYear = builder.isHolidayPerYear;
          this.dayAbsentInHours = builder.dayAbsentInHours;
          this.created = builder.created;
          this.modified = builder.modified;
     }

     public static Builder builder() {
          return Builder.aZepEmploymentPeriod();
     }

     public int getId() {
          return id;
     }


     public String getEmployeeId() {
          return employeeId;
     }


     public LocalDateTime getStartDate() {
          return startDate;
     }


     public LocalDateTime getEndDate() {
          return endDate;
     }


     public String getNote() {
          return note;
     }


     public Double getAnnualLeaveEntitlement() {
          return annualLeaveEntitlement;
     }


     public LocalDateTime getBeginningOfYear() {
          return beginningOfYear;
     }


     public double getPeriodHolidayEntitlement() {
          return periodHolidayEntitlement;
     }


     public boolean isHolidayPerYear() {
          return isHolidayPerYear;
     }


     public Double getDayAbsentInHours() {
          return dayAbsentInHours;
     }


     public String getCreated() {
          return created;
     }


     public String getModified() {
          return modified;
     }

     public static class Builder {
          private int id;
          @JsonProperty("employee_id")
          private String employeeId;
          @JsonProperty("start_date")
          private LocalDateTime startDate;
          @JsonProperty("end_date")
          private LocalDateTime endDate;
          private String note;
          @JsonProperty("annual_leave_entitlement")
          private Double annualLeaveEntitlement;
          @JsonProperty("beginning_of_year")
          private LocalDateTime beginningOfYear;
          @JsonProperty("period_holiday_entitlement")
          private Double periodHolidayEntitlement;
          @JsonProperty("is_holiday_per_year")
          private Boolean isHolidayPerYear;
          @JsonProperty("day_absent_in_hours")
          private Double dayAbsentInHours;
          private String created;
          private String modified;


            public Builder id(int id) {
                 this.id = id;
                 return this;
            }

            public Builder employeeId(String employeeId) {
                 this.employeeId = employeeId;
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

            public Builder note(String note) {
                 this.note = note;
                 return this;
            }

            public Builder annualLeaveEntitlement(Double annualLeaveEntitlement) {
                 this.annualLeaveEntitlement = annualLeaveEntitlement;
                 return this;
            }

            public Builder beginningOfYear(LocalDateTime beginningOfYear) {
                 this.beginningOfYear = beginningOfYear;
                 return this;
            }

            public Builder periodHolidayEntitlement(double periodHolidayEntitlement) {
                 this.periodHolidayEntitlement = periodHolidayEntitlement;
                 return this;
            }

            public Builder isHolidayPerYear(boolean isHolidayPerYear) {
                 this.isHolidayPerYear = isHolidayPerYear;
                 return this;
            }

            public Builder dayAbsentInHours(Double dayAbsentInHours) {
                 this.dayAbsentInHours = dayAbsentInHours;
                 return this;
            }

            public Builder created(String created) {
                 this.created = created;
                 return this;
            }

            public Builder modified(String modified) {
                 this.modified = modified;
                 return this;
            }

            public static Builder aZepEmploymentPeriod() {
                 return new Builder();
            }

            public ZepEmploymentPeriod build() {
                 return new ZepEmploymentPeriod(this);
            }
     }
}

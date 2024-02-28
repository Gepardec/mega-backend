package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record ZepEmploymentPeriod (
        int id,
        String employeeId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String note,
        Double annualLeaveEntitlement,
        LocalDateTime beginningOfYear,
        Double periodHolidayEntitlement,
        Boolean isHolidayPerYear,
        Double dayAbsentInHours,
        String created,
        String modified
) {

     @JsonCreator
     public ZepEmploymentPeriod(Builder builder) {
          this(builder.id,
               builder.employeeId,
               builder.startDate,
               builder.endDate,
               builder.note,
               builder.annualLeaveEntitlement,
               builder.beginningOfYear,
               builder.periodHolidayEntitlement,
               builder.isHolidayPerYear,
               builder.dayAbsentInHours,
               builder.created,
               builder.modified);
     }

     public static Builder builder() {
          return Builder.aZepEmploymentPeriod();
     }


     @JsonIgnoreProperties(ignoreUnknown = true)
     public static class Builder {
          @JsonProperty
          private int id;
          @JsonProperty("employee_id")
          private String employeeId;
          @JsonProperty("start_date")
          private LocalDateTime startDate;
          @JsonProperty("end_date")
          private LocalDateTime endDate;
          @JsonProperty
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
          @JsonProperty
          private String created;
          @JsonProperty
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

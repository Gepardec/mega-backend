package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class ZepEmploymentPeriod {
     private int id;
     @JsonProperty("employee_id")
     private String employeeId;
     @JsonProperty("start_date")
     private LocalDateTime startDate;
     @JsonProperty("end_date")
     private LocalDateTime endDate;
     private String note;
     @JsonProperty("annual_leave_entitlement")
     private double annualLeaveEntitlement;
     @JsonProperty("beginning_of_year")
     private LocalDateTime beginningOfYear;
     @JsonProperty("period_holiday_entitlement")
     private double periodHolidayEntitlement;
     @JsonProperty("is_holiday_per_year")
     private boolean isHolidayPerYear;
     @JsonProperty("day_absent_in_hours")
     private double dayAbsentInHours;
     private LocalDateTime created;
     private LocalDateTime modified;
}

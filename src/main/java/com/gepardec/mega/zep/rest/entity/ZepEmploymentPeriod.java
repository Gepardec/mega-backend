package com.gepardec.mega.zep.rest.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class ZepEmploymentPeriod {
    
     private int id;
     private int employeeId;
     private LocalDateTime startDate;
     private LocalDateTime endDate;
     private String note;
     private double annualLeaveEntitlement;
     private LocalDateTime beginningOfYear;
     private double periodHolidayEntitlement;
     private boolean isHolidayPerYear;
     private double dayAbsentInHours;
     private LocalDateTime created;
     private LocalDateTime modified;
}

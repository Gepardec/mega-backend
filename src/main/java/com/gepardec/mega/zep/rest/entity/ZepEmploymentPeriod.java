package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

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

     public ZepEmploymentPeriod() {
     }

     public ZepEmploymentPeriod(int id, String employeeId, LocalDateTime startDate, LocalDateTime endDate, String note, Double annualLeaveEntitlement, LocalDateTime beginningOfYear, double periodHolidayEntitlement, boolean isHolidayPerYear, Double dayAbsentInHours, String created, String modified) {
          this.id = id;
          this.employeeId = employeeId;
          this.startDate = startDate;
          this.endDate = endDate;
          this.note = note;
          this.annualLeaveEntitlement = annualLeaveEntitlement;
          this.beginningOfYear = beginningOfYear;
          this.periodHolidayEntitlement = periodHolidayEntitlement;
          this.isHolidayPerYear = isHolidayPerYear;
          this.dayAbsentInHours = dayAbsentInHours;
          this.created = created;
          this.modified = modified;
     }

     public int getId() {
          return id;
     }

     public void setId(int id) {
          this.id = id;
     }

     public String getEmployeeId() {
          return employeeId;
     }

     public void setEmployeeId(String employeeId) {
          this.employeeId = employeeId;
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

     public String getNote() {
          return note;
     }

     public void setNote(String note) {
          this.note = note;
     }

     public Double getAnnualLeaveEntitlement() {
          return annualLeaveEntitlement;
     }

     public void setAnnualLeaveEntitlement(Double annualLeaveEntitlement) {
          this.annualLeaveEntitlement = annualLeaveEntitlement;
     }

     public LocalDateTime getBeginningOfYear() {
          return beginningOfYear;
     }

     public void setBeginningOfYear(LocalDateTime beginningOfYear) {
          this.beginningOfYear = beginningOfYear;
     }

     public double getPeriodHolidayEntitlement() {
          return periodHolidayEntitlement;
     }

     public void setPeriodHolidayEntitlement(double periodHolidayEntitlement) {
          this.periodHolidayEntitlement = periodHolidayEntitlement;
     }

     public boolean isHolidayPerYear() {
          return isHolidayPerYear;
     }

     public void setHolidayPerYear(boolean holidayPerYear) {
          isHolidayPerYear = holidayPerYear;
     }

     public Double getDayAbsentInHours() {
          return dayAbsentInHours;
     }

     public void setDayAbsentInHours(Double dayAbsentInHours) {
          this.dayAbsentInHours = dayAbsentInHours;
     }

     public String getCreated() {
          return created;
     }

     public void setCreated(String created) {
          this.created = created;
     }

     public String getModified() {
          return modified;
     }

     public void setModified(String modified) {
          this.modified = modified;
     }

     public static ZepEmploymentPeriodBuilder builder() {
          return new ZepEmploymentPeriodBuilder();
     }
}

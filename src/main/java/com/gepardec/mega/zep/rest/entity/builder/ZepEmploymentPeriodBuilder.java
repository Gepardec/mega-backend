package com.gepardec.mega.zep.rest.entity.builder;

import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;

import java.time.LocalDateTime;

public class ZepEmploymentPeriodBuilder {
    private int id;
    private String employeeId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String note;
    private Double annualLeaveEntitlement;
    private LocalDateTime beginningOfYear;
    private double periodHolidayEntitlement;
    private boolean isHolidayPerYear;
    private Double dayAbsentInHours;
    private String created;
    private String modified;

    public ZepEmploymentPeriodBuilder id(int id) {
        this.id = id;
        return this;
    }

    public ZepEmploymentPeriodBuilder employeeId(String employeeId) {
        this.employeeId = employeeId;
        return this;
    }

    public ZepEmploymentPeriodBuilder startDate(LocalDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public ZepEmploymentPeriodBuilder endDate(LocalDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public ZepEmploymentPeriodBuilder note(String note) {
        this.note = note;
        return this;
    }

    public ZepEmploymentPeriodBuilder annualLeaveEntitlement(Double annualLeaveEntitlement) {
        this.annualLeaveEntitlement = annualLeaveEntitlement;
        return this;
    }

    public ZepEmploymentPeriodBuilder beginningOfYear(LocalDateTime beginningOfYear) {
        this.beginningOfYear = beginningOfYear;
        return this;
    }

    public ZepEmploymentPeriodBuilder periodHolidayEntitlement(double periodHolidayEntitlement) {
        this.periodHolidayEntitlement = periodHolidayEntitlement;
        return this;
    }

    public ZepEmploymentPeriodBuilder isHolidayPerYear(boolean isHolidayPerYear) {
        this.isHolidayPerYear = isHolidayPerYear;
        return this;
    }

    public ZepEmploymentPeriodBuilder dayAbsentInHours(Double dayAbsentInHours) {
        this.dayAbsentInHours = dayAbsentInHours;
        return this;
    }

    public ZepEmploymentPeriodBuilder created(String created) {
        this.created = created;
        return this;
    }

    public ZepEmploymentPeriodBuilder modified(String modified) {
        this.modified = modified;
        return this;
    }

    public ZepEmploymentPeriod build() {
        return new ZepEmploymentPeriod(id, employeeId, startDate, endDate, note, annualLeaveEntitlement, beginningOfYear, periodHolidayEntitlement, isHolidayPerYear, dayAbsentInHours, created, modified);
    }
}
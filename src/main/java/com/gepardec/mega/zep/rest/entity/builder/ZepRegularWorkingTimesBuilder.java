package com.gepardec.mega.zep.rest.entity.builder;

import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;

import java.time.LocalDateTime;

public class ZepRegularWorkingTimesBuilder {
    private int id;
    private String employeeId;
    private LocalDateTime startDate;
    private Double monday;
    private Double tuesday;
    private Double wednesday;
    private Double thursday;
    private Double friday;
    private Double saturday;
    private Double sunday;
    private Boolean isMonthly;
    private Double monthlyHours;
    private Double maxHoursInMonth;
    private Double maxHoursInWeek;
    public ZepRegularWorkingTimesBuilder id(int id) {
        this.id = id;
        return this;
    }

    public ZepRegularWorkingTimesBuilder employee_id(String employeeId) {
        this.employeeId = employeeId;
        return this;
    }

    public ZepRegularWorkingTimesBuilder start_date(LocalDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public ZepRegularWorkingTimesBuilder monday(Double monday) {
        this.monday = monday;
        return this;
    }

    public ZepRegularWorkingTimesBuilder tuesday(Double tuesday) {
        this.tuesday = tuesday;
        return this;
    }

    public ZepRegularWorkingTimesBuilder wednesday(Double wednesday) {
        this.wednesday = wednesday;
        return this;
    }

    public ZepRegularWorkingTimesBuilder thursday(Double thursday) {
        this.thursday = thursday;
        return this;
    }

    public ZepRegularWorkingTimesBuilder friday(Double friday) {
        this.friday = friday;
        return this;
    }

    public ZepRegularWorkingTimesBuilder saturday(Double saturday) {
        this.saturday = saturday;
        return this;
    }

    public ZepRegularWorkingTimesBuilder sunday(Double sunday) {
        this.sunday = sunday;
        return this;
    }

    public ZepRegularWorkingTimesBuilder is_monthly(Boolean isMonthly) {
        this.isMonthly = isMonthly;
        return this;
    }

    public ZepRegularWorkingTimesBuilder monthly_hours(Double monthlyHours) {
        this.monthlyHours = monthlyHours;
        return this;
    }

    public ZepRegularWorkingTimesBuilder max_hours_in_month(Double maxHoursInMonth) {
        this.maxHoursInMonth = maxHoursInMonth;
        return this;
    }

    public ZepRegularWorkingTimesBuilder max_hours_in_week(Double maxHoursInWeek) {
        this.maxHoursInWeek = maxHoursInWeek;
        return this;
    }

    public ZepRegularWorkingTimes build() {
        return new ZepRegularWorkingTimes(id, employeeId, startDate, monday, tuesday, wednesday, thursday, friday, saturday, sunday, isMonthly, monthlyHours, maxHoursInMonth, maxHoursInWeek);
    }
}

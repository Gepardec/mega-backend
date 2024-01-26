package com.gepardec.mega.zep.rest.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZepRegularWorkingTimes {
        private int id;
        private String employee_id;
        private LocalDateTime start_date;
        private Double monday;
        private Double tuesday;
        private Double wednesday;
        private Double thursday;
        private Double friday;
        private Double saturday;
        private Double sunday;
        private Boolean is_monthly;
        private Double monthly_hours;
        private Double max_hours_in_month;
        private Double max_hours_in_week;


        public ZepRegularWorkingTimes() {
        }

        public ZepRegularWorkingTimes(int id, String employee_id, LocalDateTime start_date, Double monday, Double tuesday, Double wednesday, Double thursday, Double friday, Double saturday, Double sunday, Boolean is_monthly, Double monthly_hours, Double max_hours_in_month, Double max_hours_in_week) {
                this.id = id;
                this.employee_id = employee_id;
                this.start_date = start_date;
                this.monday = monday;
                this.tuesday = tuesday;
                this.wednesday = wednesday;
                this.thursday = thursday;
                this.friday = friday;
                this.saturday = saturday;
                this.sunday = sunday;
                this.is_monthly = is_monthly;
                this.monthly_hours = monthly_hours;
                this.max_hours_in_month = max_hours_in_month;
                this.max_hours_in_week = max_hours_in_week;
        }

        public int getId() {
                return id;
        }

        public void setId(int id) {
                this.id = id;
        }

        public String getEmployee_id() {
                return employee_id;
        }

        public void setEmployee_id(String employee_id) {
                this.employee_id = employee_id;
        }

        public LocalDateTime getStart_date() {
                return start_date;
        }

        public void setStart_date(LocalDateTime start_date) {
                this.start_date = start_date;
        }

        public Double getMonday() {
                return monday;
        }

        public void setMonday(Double monday) {
                this.monday = monday;
        }

        public Double getTuesday() {
                return tuesday;
        }

        public void setTuesday(Double tuesday) {
                this.tuesday = tuesday;
        }

        public Double getWednesday() {
                return wednesday;
        }

        public void setWednesday(Double wednesday) {
                this.wednesday = wednesday;
        }

        public Double getThursday() {
                return thursday;
        }

        public void setThursday(Double thursday) {
                this.thursday = thursday;
        }

        public Double getFriday() {
                return friday;
        }

        public void setFriday(Double friday) {
                this.friday = friday;
        }

        public Double getSaturday() {
                return saturday;
        }

        public void setSaturday(Double saturday) {
                this.saturday = saturday;
        }

        public Double getSunday() {
                return sunday;
        }

        public void setSunday(Double sunday) {
                this.sunday = sunday;
        }

        public Boolean getIs_monthly() {
                return is_monthly;
        }

        public void setIs_monthly(Boolean is_monthly) {
                this.is_monthly = is_monthly;
        }

        public Double getMonthly_hours() {
                return monthly_hours;
        }

        public void setMonthly_hours(Double monthly_hours) {
                this.monthly_hours = monthly_hours;
        }

        public Double getMax_hours_in_month() {
                return max_hours_in_month;
        }

        public void setMax_hours_in_month(Double max_hours_in_month) {
                this.max_hours_in_month = max_hours_in_month;
        }

        public Double getMax_hours_in_week() {
                return max_hours_in_week;
        }

        public void setMax_hours_in_week(Double max_hours_in_week) {
                this.max_hours_in_week = max_hours_in_week;
        }

        public static ZepRegularWorkingTimesBuilder builder() {
                return new ZepRegularWorkingTimesBuilder();
        }

}


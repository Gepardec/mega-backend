package com.gepardec.mega.zep.rest.entity;

import java.time.LocalDateTime;

public class ZepRegularWorkingTimes {
        private int id;
        private String employee_id;
        private LocalDateTime start_date;
        private double monday;
        private double tuesday;
        private double wednesday;
        private double thursday;
        private double friday;
        private double saturday;
        private double sunday;
        private boolean is_monthly;
        private double monthly_hours;
        private double max_hours_in_month;
        private double max_hours_in_week;
}

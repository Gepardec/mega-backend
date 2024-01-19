package com.gepardec.mega.zep.rest.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}

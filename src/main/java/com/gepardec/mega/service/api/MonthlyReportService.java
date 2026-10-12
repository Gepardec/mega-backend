package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.Attendances;

import java.time.YearMonth;

public interface MonthlyReportService {

    Attendances getAttendances(YearMonth payrollMonth);
}

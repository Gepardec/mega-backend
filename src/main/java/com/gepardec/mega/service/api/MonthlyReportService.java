package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.Attendances;
import com.gepardec.mega.domain.model.EmployeeCheck;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;

import java.time.YearMonth;

public interface MonthlyReportService {

    MonthlyReport getMonthEndReportForUser(YearMonth payrollMonth);

    EmployeeCheck getEmployeeCheck(YearMonth payrollMonth);

    Attendances getAttendances(YearMonth payrollMonth);
}

package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;

import java.time.LocalDate;

public interface MonthlyReportService {

    MonthlyReport getMonthEndReportForUser(Employee employee, LocalDate date);
}

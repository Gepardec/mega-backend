package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;

import java.time.LocalDate;

public interface MonthlyReportService {

    MonthlyReport getMonthEndReportForUser();

    MonthlyReport getMonthEndReportForUser(Integer year, Integer month, Employee employee, LocalDate initialDate);

    boolean isMonthCompletedForEmployee(Employee employee, LocalDate date);

    boolean isMonthConfirmedFromEmployee(Employee employee, LocalDate date);
}

package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.MonthlyWarning;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;

import java.util.List;

public interface TimeWarningService {
    List<MonthlyWarning> getAllTimeWarningsForEmployeeAndMonth(List<AbsenceTime> absences, List<ProjectEntry> projectEntries, Employee employee);
}

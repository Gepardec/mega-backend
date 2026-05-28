package com.gepardec.mega.hexagon.worktime.domain.model;

import java.time.YearMonth;
import java.util.List;

public record WorkTimeReport(YearMonth payrollMonth, List<WorkTimeEntry> entries) {
}

package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;

import java.util.List;

public interface WorkTimeWarningCalculator {
    List<WorkTimeWarning> calculate(WorkTimeEvaluation evaluation);
}

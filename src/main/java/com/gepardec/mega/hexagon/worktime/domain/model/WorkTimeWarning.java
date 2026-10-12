package com.gepardec.mega.hexagon.worktime.domain.model;

import java.time.LocalDate;

public record WorkTimeWarning(LocalDate date, WorkTimeWarningType type, Double hours) {
}

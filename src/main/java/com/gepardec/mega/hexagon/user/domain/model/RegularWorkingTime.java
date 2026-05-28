package com.gepardec.mega.hexagon.user.domain.model;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

public record RegularWorkingTime(LocalDate start, Map<DayOfWeek, Duration> workingHours) {
}

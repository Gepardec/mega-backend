package com.gepardec.mega.hexagon.project.domain.model;

import java.time.LocalDate;

public record ZepProjectProfile(int zepId, String name, LocalDate startDate, LocalDate endDate) {
}

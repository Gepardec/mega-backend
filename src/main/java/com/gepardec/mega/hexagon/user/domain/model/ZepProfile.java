package com.gepardec.mega.hexagon.user.domain.model;

import java.time.LocalDate;

public record ZepProfile(
        String username,
        String email,
        String firstname,
        String lastname,
        String title,
        String salutation,
        String workDescription,
        String language,
        LocalDate releaseDate,
        EmploymentPeriods employmentPeriods,
        RegularWorkingTimes regularWorkingTimes
) {
}

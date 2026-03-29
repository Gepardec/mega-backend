package com.gepardec.mega.hexagon.user.domain.model;

import java.time.LocalDate;
import java.util.List;

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
        List<EmploymentPeriod> employmentPeriods,
        List<RegularWorkingTime> regularWorkingTimes
) {
}

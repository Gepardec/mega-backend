package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class FirstDayCurrentPeriodMapper implements Mapper<LocalDate, List<ZepEmploymentPeriod>> {
    @Override
    public LocalDate map(List<ZepEmploymentPeriod> zepEmploymentPeriods) {
        return zepEmploymentPeriods.stream()
                .map(ZepEmploymentPeriod::startDate)
                .map(LocalDateTime::toLocalDate)
                .filter(startDate -> !startDate.isAfter(LocalDate.now())).max(Comparator.naturalOrder())
                .orElse(null);
    }
}

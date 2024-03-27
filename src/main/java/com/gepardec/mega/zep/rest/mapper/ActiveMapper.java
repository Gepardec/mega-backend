package com.gepardec.mega.zep.rest.mapper;

import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ActiveMapper implements Mapper<Boolean, List<ZepEmploymentPeriod>> {
    @Override
    public Boolean map(List<ZepEmploymentPeriod> zepEmploymentPeriods) {
        if (zepEmploymentPeriods == null || zepEmploymentPeriods.isEmpty()) {
            return false;
        }

        return zepEmploymentPeriods.stream()
                .filter(this::startDateIsInPast)
                .anyMatch(this::endDateIsInFuture);
    }

    private boolean startDateIsInPast(ZepEmploymentPeriod zepEmploymentPeriod) {
        if (zepEmploymentPeriod.startDate() == null) {
            return false;
        }

        LocalDate startDate = zepEmploymentPeriod.startDate().toLocalDate();
        LocalDate now = LocalDate.now();

        return !startDate.isAfter(now);
    }

    private boolean endDateIsInFuture(ZepEmploymentPeriod zepEmploymentPeriod) {
        if (zepEmploymentPeriod.endDate() == null) {
            return true;
        }

        LocalDate endDate = zepEmploymentPeriod.endDate().toLocalDate();
        LocalDate now = LocalDate.now();

        return !endDate.isBefore(now);
    }
}

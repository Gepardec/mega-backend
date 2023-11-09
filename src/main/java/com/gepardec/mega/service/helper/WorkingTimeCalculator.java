package com.gepardec.mega.service.helper;

import com.gepardec.mega.domain.model.Employee;
import de.provantis.zep.ProjektzeitType;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Predicate;

@ApplicationScoped
public class WorkingTimeCalculator {

    private static final String BILLABLE_TIME_FORMAT = "HH:mm";

    public String getInternalTimesForEmployee(@Nonnull List<ProjektzeitType> projektzeitTypeList, @Nonnull Employee employee) {
        return getWorkingTimesForEmployee(projektzeitTypeList, employee, Predicate.not(ProjektzeitType::isIstFakturierbar));
    }


    public String getBillableTimesForEmployee(@Nonnull List<ProjektzeitType> projektzeitTypeList, @Nonnull Employee employee) {
        return getWorkingTimesForEmployee(projektzeitTypeList, employee, ProjektzeitType::isIstFakturierbar);
    }


    public String getTotalWorkingTimeForEmployee(@Nonnull List<ProjektzeitType> projektzeitTypeList, @Nonnull Employee employee) {
        return getWorkingTimesForEmployee(projektzeitTypeList, employee, $ -> true);
    }

    private String getWorkingTimesForEmployee(List<ProjektzeitType> projektzeitTypeList, Employee employee, Predicate<ProjektzeitType> billableFilter) {
        Duration totalBillable = projektzeitTypeList.stream()
                .filter(pzt -> pzt.getUserId().equals(employee.getUserId()))
                .filter(billableFilter)
                .map(pzt -> LocalTime.parse(pzt.getDauer()))
                .map(lt -> Duration.between(LocalTime.MIN, lt))
                .reduce(Duration.ZERO, Duration::plus);

        return DurationFormatUtils.formatDuration(totalBillable.toMillis(), BILLABLE_TIME_FORMAT);
    }
}

package com.gepardec.mega.hexagon.project.domain.model;

import com.gepardec.mega.hexagon.project.domain.error.LeistungsnachweisNotApplicableException;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Set;

public record Project(
        ProjectId id,
        int zepId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean billable,
        boolean leistungsnachweisEnabled,
        Set<UserId> leads
) {

    public Project {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(startDate, "startDate must not be null");
        leads = Set.copyOf(Objects.requireNonNull(leads, "leads must not be null"));
        if (leistungsnachweisEnabled && !billable) {
            throw new IllegalArgumentException("leistungsnachweisEnabled cannot be true on a non-billable project");
        }
    }

    public static Project create(ProjectId id, ZepProjectProfile profile) {
        return new Project(id, profile.zepId(), profile.name(), profile.startDate(), profile.endDate(), profile.billable(), profile.billable(), Set.of());
    }

    public Project withSyncedZepData(ZepProjectProfile profile) {
        boolean updatedLeistungsnachweisEnabled = billable == profile.billable()
                ? leistungsnachweisEnabled
                : profile.billable();
        return new Project(id, profile.zepId(), profile.name(), profile.startDate(), profile.endDate(), profile.billable(), updatedLeistungsnachweisEnabled, leads);
    }

    public Project withLeads(Set<UserId> updatedLeads) {
        return new Project(id, zepId, name, startDate, endDate, billable, leistungsnachweisEnabled, updatedLeads);
    }

    public Project withLeistungsnachweisEnabled(boolean enabled) {
        if (enabled && !billable) {
            throw new LeistungsnachweisNotApplicableException(
                    "Leistungsnachweis cannot be enabled on non-billable project " + id.value());
        }
        return new Project(id, zepId, name, startDate, endDate, billable, enabled, leads);
    }

    public boolean isActiveIn(YearMonth month) {
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        boolean startsBeforeMonthEnds = !startDate.isAfter(monthEnd);
        boolean endsAfterMonthStarts = endDate == null || !endDate.isBefore(monthStart);
        return startsBeforeMonthEnds && endsAfterMonthStarts;
    }

    public boolean isLedBy(UserId userId) {
        return leads.contains(userId);
    }

    public boolean isLeistungsnachweisConfigurableFrom(YearMonth month) {
        return billable && (endDate == null || !endDate.isBefore(month.atDay(1)));
    }
}

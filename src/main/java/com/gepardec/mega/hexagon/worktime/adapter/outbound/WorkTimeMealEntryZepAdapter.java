package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WorkTimeMealEntryZepPort;
import com.gepardec.mega.zep.rest.dto.ZepMeal;
import com.gepardec.mega.zep.rest.service.MealService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class WorkTimeMealEntryZepAdapter implements WorkTimeMealEntryZepPort {

    private final MealService mealService;

    @Inject
    public WorkTimeMealEntryZepAdapter(MealService mealService) {
        this.mealService = mealService;
    }

    @Override
    public Set<LocalDate> fetchMealEntryDates(ZepUsername zepUsername, YearMonth month) {
        Objects.requireNonNull(zepUsername, "zepUsername must not be null");
        Objects.requireNonNull(month, "month must not be null");

        return mealService.getZepMealsByEmployeeNameForMonth(zepUsername.value(), month).stream()
                .map(ZepMeal::date)
                .filter(Objects::nonNull)
                .map(OffsetDateTime::toLocalDate)
                .collect(Collectors.toUnmodifiableSet());
    }
}

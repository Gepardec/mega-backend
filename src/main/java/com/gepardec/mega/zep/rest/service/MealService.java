package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepMeal;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class MealService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    public List<ZepMeal> getZepMealsByEmployeeNameForMonth(String employeeName, YearMonth payrollMonth) {
        Log.debug("Loading meal entries from ZEP");

        return Multi.createBy().repeating()
                .uni(AtomicInteger::new, page ->
                        zepEmployeeRestClient.getMealsByUsername(employeeName, page.incrementAndGet())
                                .onFailure().invoke(ex -> Log.warn("Error retrieving meal entries from ZEP", ex))
                )
                .whilst(ZepResponse::hasNext)
                .map(ZepResponse::data)
                .onItem().<ZepMeal>disjoint()
                .collect().asList()
                .await().indefinitely()
                .stream()
                .filter(meal -> meal.date() != null && payrollMonth.equals(YearMonth.from(meal.date())))
                .toList();
    }
}

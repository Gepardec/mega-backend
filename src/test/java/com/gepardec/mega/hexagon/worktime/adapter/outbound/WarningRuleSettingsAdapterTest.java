package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestTransaction
class WarningRuleSettingsAdapterTest {

    @Inject
    WarningRuleSettingsAdapter adapter;

    @Test
    void save_shouldPersistAndReloadSetting() {
        adapter.save(new WarningRuleSetting(WarningRule.MEAL_ENTRY, false, LocalDate.of(2026, 11, 1)));

        assertThat(adapter.findAll()).contains(new WarningRuleSetting(WarningRule.MEAL_ENTRY, false, LocalDate.of(2026, 11, 1)));
    }

    @Test
    void save_shouldPersistNullActiveFrom() {
        adapter.save(new WarningRuleSetting(WarningRule.BILLABLE_PROJECT, true, null));

        assertThat(adapter.findAll()).contains(new WarningRuleSetting(WarningRule.BILLABLE_PROJECT, true, null));
    }
}

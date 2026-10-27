package com.gepardec.mega.hexagon.worktime.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarningRuleSettingsTest {

    private static final LocalDate START = LocalDate.of(2026, 11, 1);

    @Test
    void isActive_shouldReflectEnabledFlag() {
        WarningRuleSettings settings = new WarningRuleSettings(List.of(
                new WarningRuleSetting(WarningRule.WEEKEND, true, null),
                new WarningRuleSetting(WarningRule.HOLIDAY, false, null)));

        assertThat(settings.isActive(WarningRule.WEEKEND)).isTrue();
        assertThat(settings.isActive(WarningRule.HOLIDAY)).isFalse();
    }

    @Test
    void appliesOn_shouldRespectActiveFromBoundary() {
        WarningRuleSettings settings = new WarningRuleSettings(List.of(
                new WarningRuleSetting(WarningRule.MEAL_ENTRY, true, START)));

        assertThat(settings.appliesOn(WarningRule.MEAL_ENTRY, START.minusDays(1))).isFalse();
        assertThat(settings.appliesOn(WarningRule.MEAL_ENTRY, START)).isTrue();
        assertThat(settings.appliesOn(WarningRule.MEAL_ENTRY, START.plusDays(1))).isTrue();
    }

    @Test
    void appliesOn_shouldApplyToAllDatesWithoutActiveFrom() {
        WarningRuleSettings settings = new WarningRuleSettings(List.of(
                new WarningRuleSetting(WarningRule.TIME_OVERLAP, true, null)));

        assertThat(settings.appliesOn(WarningRule.TIME_OVERLAP, LocalDate.of(2020, 1, 1))).isTrue();
    }

    @Test
    void appliesOn_shouldKeepWarningsWithoutDate() {
        WarningRuleSettings settings = new WarningRuleSettings(List.of(
                new WarningRuleSetting(WarningRule.MEAL_ENTRY, true, START)));

        assertThat(settings.appliesOn(WarningRule.MEAL_ENTRY, null)).isTrue();
    }

    @Test
    void appliesOn_shouldBeFalseWhenDisabled() {
        WarningRuleSettings settings = new WarningRuleSettings(List.of(
                new WarningRuleSetting(WarningRule.WEEKEND, false, null)));

        assertThat(settings.appliesOn(WarningRule.WEEKEND, START)).isFalse();
    }

    @Test
    void missingRule_shouldBeTreatedAsDisabledAndListed() {
        WarningRuleSettings settings = new WarningRuleSettings(List.of(
                new WarningRuleSetting(WarningRule.WEEKEND, true, null)));

        assertThat(settings.isActive(WarningRule.HOLIDAY)).isFalse();
        assertThat(settings.appliesOn(WarningRule.HOLIDAY, START)).isFalse();
        assertThat(settings.missingRules())
                .doesNotContain(WarningRule.WEEKEND)
                .contains(WarningRule.HOLIDAY)
                .hasSize(WarningRule.values().length - 1);
    }
}

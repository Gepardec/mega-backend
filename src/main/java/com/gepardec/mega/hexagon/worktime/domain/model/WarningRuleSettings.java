package com.gepardec.mega.hexagon.worktime.domain.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The settings of all warning rules. A rule without a setting is treated as disabled.
 */
public record WarningRuleSettings(Map<WarningRule, WarningRuleSetting> byRule) {

    public WarningRuleSettings {
        Map<WarningRule, WarningRuleSetting> copy = new EnumMap<>(WarningRule.class);
        copy.putAll(byRule);
        byRule = Collections.unmodifiableMap(copy);
    }

    public WarningRuleSettings(List<WarningRuleSetting> settings) {
        this(toMap(settings));
    }

    public boolean isActive(WarningRule rule) {
        return find(rule).map(WarningRuleSetting::enabled).orElse(false);
    }

    public boolean appliesOn(WarningRule rule, LocalDate date) {
        return find(rule)
                .filter(WarningRuleSetting::enabled)
                .map(setting -> date == null || setting.activeFrom() == null || !date.isBefore(setting.activeFrom()))
                .orElse(false);
    }

    public Set<WarningRule> missingRules() {
        Set<WarningRule> missing = EnumSet.allOf(WarningRule.class);
        missing.removeAll(byRule.keySet());
        return missing;
    }

    private Optional<WarningRuleSetting> find(WarningRule rule) {
        return Optional.ofNullable(byRule.get(rule));
    }

    private static Map<WarningRule, WarningRuleSetting> toMap(List<WarningRuleSetting> settings) {
        Map<WarningRule, WarningRuleSetting> map = new EnumMap<>(WarningRule.class);
        settings.forEach(setting -> map.put(setting.rule(), setting));
        return map;
    }
}

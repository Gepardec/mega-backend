package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity(name = "WarningRuleSettingEntity")
@Table(name = "worktime_warning_rule_setting")
public class WarningRuleSettingEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "rule", nullable = false, updatable = false)
    private WarningRule rule;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "active_from")
    private LocalDate activeFrom;

    public WarningRule getRule() {
        return rule;
    }

    public void setRule(WarningRule rule) {
        this.rule = rule;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDate getActiveFrom() {
        return activeFrom;
    }

    public void setActiveFrom(LocalDate activeFrom) {
        this.activeFrom = activeFrom;
    }
}

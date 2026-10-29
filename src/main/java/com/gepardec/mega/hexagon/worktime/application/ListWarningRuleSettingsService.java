package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.worktime.application.port.inbound.ListWarningRuleSettingsUseCase;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WarningRuleSettingsPort;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Comparator;
import java.util.List;

@ApplicationScoped
@Transactional
public class ListWarningRuleSettingsService implements ListWarningRuleSettingsUseCase {
    private final WarningRuleSettingsPort settingsPort;

    @Inject
    public ListWarningRuleSettingsService(WarningRuleSettingsPort settingsPort) {
        this.settingsPort = settingsPort;
    }

    @Override
    public List<WarningRuleSetting> list() {
        return settingsPort.findAll().stream()
                .sorted(Comparator.comparing(WarningRuleSetting::rule))
                .toList();
    }
}

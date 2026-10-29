package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.worktime.application.port.inbound.UpdateWarningRuleSettingUseCase;
import com.gepardec.mega.hexagon.worktime.application.port.outbound.WarningRuleSettingsPort;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Objects;

@ApplicationScoped
@Transactional
public class UpdateWarningRuleSettingService implements UpdateWarningRuleSettingUseCase {
    private final WarningRuleSettingsPort settingsPort;

    @Inject
    public UpdateWarningRuleSettingService(WarningRuleSettingsPort settingsPort) {
        this.settingsPort = settingsPort;
    }

    @Override
    public WarningRuleSetting update(WarningRuleSetting setting) {
        Objects.requireNonNull(setting, "setting must not be null");
        WarningRuleSetting saved = settingsPort.save(setting);
        Log.infof("Warning rule setting updated: rule=%s, enabled=%s, activeFrom=%s",
                saved.rule(), saved.enabled(), saved.activeFrom());
        return saved;
    }
}

package com.gepardec.mega.hexagon.worktime.application.port.inbound;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;

import java.util.List;

public interface ListWarningRuleSettingsUseCase {
    List<WarningRuleSetting> list();
}

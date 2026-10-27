package com.gepardec.mega.hexagon.worktime.application.port.inbound;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;

public interface UpdateWarningRuleSettingUseCase {
    WarningRuleSetting update(WarningRuleSetting setting);
}

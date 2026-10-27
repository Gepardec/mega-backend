package com.gepardec.mega.hexagon.worktime.application.port.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;

import java.util.List;

public interface WarningRuleSettingsPort {

    List<WarningRuleSetting> findAll();

    WarningRuleSetting save(WarningRuleSetting setting);
}

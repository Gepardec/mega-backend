package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.application.port.outbound.WarningRuleSettingsPort;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class WarningRuleSettingsAdapter implements WarningRuleSettingsPort {

    @Inject
    WarningRuleSettingPanacheRepository panache;

    @Inject
    WarningRuleSettingMapper mapper;

    @Override
    public List<WarningRuleSetting> findAll() {
        return panache.listAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public WarningRuleSetting save(WarningRuleSetting setting) {
        WarningRuleSettingEntity entity = panache.findByIdOptional(setting.rule()).orElseGet(() -> {
            WarningRuleSettingEntity created = new WarningRuleSettingEntity();
            created.setRule(setting.rule());
            panache.persist(created);
            return created;
        });
        mapper.updateEntity(setting, entity);
        return mapper.toDomain(entity);
    }
}

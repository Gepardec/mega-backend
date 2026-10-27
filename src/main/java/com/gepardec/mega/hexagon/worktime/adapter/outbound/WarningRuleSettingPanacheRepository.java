package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WarningRuleSettingPanacheRepository implements PanacheRepositoryBase<WarningRuleSettingEntity, WarningRule> {
}

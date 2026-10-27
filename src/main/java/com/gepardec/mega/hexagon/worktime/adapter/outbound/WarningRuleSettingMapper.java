package com.gepardec.mega.hexagon.worktime.adapter.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface WarningRuleSettingMapper {

    WarningRuleSetting toDomain(WarningRuleSettingEntity entity);

    void updateEntity(WarningRuleSetting setting, @MappingTarget WarningRuleSettingEntity entity);
}

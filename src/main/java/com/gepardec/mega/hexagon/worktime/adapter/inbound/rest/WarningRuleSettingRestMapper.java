package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.WarningRuleDto;
import com.gepardec.mega.hexagon.generated.model.WarningRuleSettingDto;
import com.gepardec.mega.hexagon.generated.model.WarningRuleSettingUpdateDto;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRule;
import com.gepardec.mega.hexagon.worktime.domain.model.WarningRuleSetting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface WarningRuleSettingRestMapper {

    WarningRuleSettingDto toDto(WarningRuleSetting setting);

    List<WarningRuleSettingDto> toDto(List<WarningRuleSetting> settings);

    @Mapping(target = "rule", source = "rule")
    @Mapping(target = "enabled", source = "update.enabled")
    @Mapping(target = "activeFrom", source = "update.activeFrom")
    WarningRuleSetting toDomain(WarningRuleDto rule, WarningRuleSettingUpdateDto update);

    WarningRule toDomain(WarningRuleDto rule);

    WarningRuleDto toDto(WarningRule rule);
}

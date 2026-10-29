package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.WorkTimeAdminApi;
import com.gepardec.mega.hexagon.generated.model.WarningRuleDto;
import com.gepardec.mega.hexagon.generated.model.WarningRuleSettingUpdateDto;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.ListWarningRuleSettingsUseCase;
import com.gepardec.mega.hexagon.worktime.application.port.inbound.UpdateWarningRuleSettingUseCase;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.ADMIN)
public class WorkTimeWarningRuleResource implements WorkTimeAdminApi {

    private final ListWarningRuleSettingsUseCase listUseCase;
    private final UpdateWarningRuleSettingUseCase updateUseCase;
    private final WarningRuleSettingRestMapper mapper;

    @Inject
    public WorkTimeWarningRuleResource(ListWarningRuleSettingsUseCase listUseCase,
                                       UpdateWarningRuleSettingUseCase updateUseCase,
                                       WarningRuleSettingRestMapper mapper) {
        this.listUseCase = listUseCase;
        this.updateUseCase = updateUseCase;
        this.mapper = mapper;
    }

    @Override
    public Response listWarningRuleSettings() {
        return Response.ok(mapper.toDto(listUseCase.list())).build();
    }

    @Override
    public Response updateWarningRuleSetting(WarningRuleDto rule, WarningRuleSettingUpdateDto update) {
        return Response.ok(mapper.toDto(updateUseCase.update(mapper.toDomain(rule, update)))).build();
    }
}

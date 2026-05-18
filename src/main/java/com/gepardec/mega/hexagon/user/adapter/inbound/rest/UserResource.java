package com.gepardec.mega.hexagon.user.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.api.UserApi;
import com.gepardec.mega.hexagon.generated.model.ActiveUserDto;
import com.gepardec.mega.hexagon.generated.model.UpdateReleaseDateEntryDto;
import com.gepardec.mega.hexagon.generated.model.UpdateReleaseDatesRequestDto;
import com.gepardec.mega.hexagon.shared.application.security.MegaRolesAllowed;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.user.application.port.inbound.GetActiveUsersUseCase;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDateCommand;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDatesUseCase;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;

@RequestScoped
@Authenticated
public class UserResource implements UserApi {

    private final GetActiveUsersUseCase getActiveUsersUseCase;
    private final UpdateReleaseDatesUseCase updateReleaseDatesUseCase;
    private final UserRestMapper userRestMapper;

    @Inject
    public UserResource(
            GetActiveUsersUseCase getActiveUsersUseCase,
            UpdateReleaseDatesUseCase updateReleaseDatesUseCase,
            UserRestMapper userRestMapper
    ) {
        this.getActiveUsersUseCase = getActiveUsersUseCase;
        this.updateReleaseDatesUseCase = updateReleaseDatesUseCase;
        this.userRestMapper = userRestMapper;
    }

    @Override
    @MegaRolesAllowed(Role.OFFICE_MANAGEMENT)
    public Response getActiveUsers() {
        List<ActiveUserDto> activeUsers = getActiveUsersUseCase.getActiveUsers().stream()
                .map(userRestMapper::toDto)
                .toList();
        return Response.ok(activeUsers).build();
    }

    @Override
    @MegaRolesAllowed(Role.OFFICE_MANAGEMENT)
    public Response updateReleaseDates(UpdateReleaseDatesRequestDto updateReleaseDatesRequestDto) {
        List<UpdateReleaseDateCommand> commands = requestEntries(updateReleaseDatesRequestDto).stream()
                .filter(Objects::nonNull)
                .map(userRestMapper::toCommand)
                .filter(Objects::nonNull)
                .toList();

        return Response.ok(
                userRestMapper.toDto(updateReleaseDatesUseCase.update(commands))
        ).build();
    }

    private List<UpdateReleaseDateEntryDto> requestEntries(UpdateReleaseDatesRequestDto request) {
        if (request == null) {
            return List.of();
        }
        return request.getEntries();
    }
}

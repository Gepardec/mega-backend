package com.gepardec.mega.hexagon.user.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.ActiveUserDto;
import com.gepardec.mega.hexagon.generated.model.UpdateReleaseDateEntryDto;
import com.gepardec.mega.hexagon.generated.model.UpdateReleaseDatesResponseDto;
import com.gepardec.mega.hexagon.generated.model.UserDto;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDateCommand;
import com.gepardec.mega.hexagon.user.application.port.inbound.UpdateReleaseDatesResult;
import com.gepardec.mega.hexagon.user.domain.model.PersonioId;
import com.gepardec.mega.hexagon.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface UserRestMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "fullName", source = "name")
    @Mapping(target = "email", source = "email")
    ActiveUserDto toDto(User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "fullName", source = "name")
    @Mapping(target = "zepUsername", source = "zepUsername")
    @Mapping(target = "personioId", source = "personioId")
    @Mapping(target = "isExternal", source = "external")
    UserDto toUserDto(User user);

    UpdateReleaseDateCommand toCommand(UpdateReleaseDateEntryDto entry);

    UpdateReleaseDatesResponseDto toDto(UpdateReleaseDatesResult result);

    default UUID map(UserId userId) {
        return userId == null ? null : userId.value();
    }

    default UserId map(UUID userId) {
        return userId == null ? null : UserId.of(userId);
    }

    default String map(FullName fullName) {
        return fullName == null ? null : fullName.displayName();
    }

    default String map(Email email) {
        return email == null ? null : email.value();
    }

    default String map(ZepUsername zepUsername) {
        return zepUsername == null ? null : zepUsername.value();
    }

    default Integer map(PersonioId personioId) {
        return personioId == null ? null : personioId.value();
    }
}

package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.UserStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface UserMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "firstname", source = "name.firstname")
    @Mapping(target = "lastname", source = "name.lastname")
    @Mapping(target = "roles", qualifiedByName = "rolesToStrings")
    @Mapping(target = "zepUsername", source = "zepProfile.username")
    void updateEntity(User user, @MappingTarget UserEntity entity);

    default User toDomain(UserEntity entity) {
        return User.reconstitute(
                UserId.of(entity.getId()),
                Email.of(entity.getEmail()),
                FullName.of(entity.getFirstname(), entity.getLastname()),
                UserStatus.valueOf(entity.getStatus()),
                stringsToRoles(entity.getRoles()),
                entity.getZepProfile(),
                entity.getPersonioProfile()
        );
    }

    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<Role> roles) {
        return roles == null ? Set.of() : roles.stream().map(Role::name).collect(Collectors.toSet());
    }

    @Named("stringsToRoles")
    default Set<Role> stringsToRoles(Set<String> strings) {
        return strings == null ? Set.of() : strings.stream().map(Role::valueOf).collect(Collectors.toSet());
    }
}

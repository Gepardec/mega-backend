package com.gepardec.mega.hexagon.user.adapter.outbound;

import com.gepardec.mega.hexagon.user.domain.model.Email;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.FullName;
import com.gepardec.mega.hexagon.user.domain.model.PersonioId;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepUsername;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface UserMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "firstname", source = "name.firstname")
    @Mapping(target = "lastname", source = "name.lastname")
    @Mapping(target = "zepUsername", source = "zepUsername")
    @Mapping(target = "personioId", source = "personioId")
    @Mapping(target = "employmentPeriods", source = "employmentPeriods.employmentPeriods")
    void updateEntity(User user, @MappingTarget UserEntity entity);

    default User toDomain(UserEntity entity) {
        return new User(
                UserId.of(entity.getId()),
                Email.of(entity.getEmail()),
                FullName.of(entity.getFirstname(), entity.getLastname()),
                ZepUsername.of(entity.getZepUsername()),
                toPersonioId(entity.getPersonioId()),
                new EmploymentPeriods(
                        entity.getEmploymentPeriods() == null
                                ? java.util.List.of()
                                : entity.getEmploymentPeriods().stream().map(this::toDomain).toList()
                ),
                entity.getRoles() == null ? Set.of() : Set.copyOf(entity.getRoles())
        );
    }

    default Integer fromPersonioId(PersonioId personioId) {
        return personioId == null ? null : personioId.value();
    }

    default String fromZepUsername(ZepUsername zepUsername) {
        return zepUsername == null ? null : zepUsername.value();
    }

    default PersonioId toPersonioId(Integer personioId) {
        return personioId == null ? null : PersonioId.of(personioId);
    }

    default Set<UserEmploymentPeriodEmbeddable> fromEmploymentPeriods(java.util.List<EmploymentPeriod> employmentPeriods) {
        if (employmentPeriods == null) {
            return Set.of();
        }
        Set<UserEmploymentPeriodEmbeddable> embeddables = new HashSet<>();
        for (EmploymentPeriod employmentPeriod : employmentPeriods) {
            embeddables.add(fromEmploymentPeriod(employmentPeriod));
        }
        return embeddables;
    }

    default UserEmploymentPeriodEmbeddable fromEmploymentPeriod(EmploymentPeriod employmentPeriod) {
        if (employmentPeriod == null) {
            return null;
        }
        return new UserEmploymentPeriodEmbeddable(employmentPeriod.start(), employmentPeriod.end());
    }

    default EmploymentPeriod toDomain(UserEmploymentPeriodEmbeddable employmentPeriod) {
        if (employmentPeriod == null) {
            return null;
        }
        return new EmploymentPeriod(employmentPeriod.getStartDate(), employmentPeriod.getEndDate());
    }
}

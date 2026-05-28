package com.gepardec.mega.hexagon.user.adapter.inbound.rest;

import com.gepardec.mega.hexagon.generated.model.UserDto;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.Role;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.PersonioId;
import com.gepardec.mega.hexagon.user.domain.model.User;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRestMapperTest {

    private final UserRestMapper mapper = Mappers.getMapper(UserRestMapper.class);

    @Test
    void toUserDto_shouldMapAllFields() {
        UUID rawUserId = Instancio.create(UUID.class);
        User user = new User(
                UserId.of(rawUserId),
                Email.of("employee@example.com"),
                FullName.of("Ada", "Lovelace"),
                ZepUsername.of("adal"),
                PersonioId.of(42),
                EmploymentPeriods.empty(),
                Set.of(Role.EMPLOYEE, Role.PROJECT_LEAD),
                LocalDate.of(2026, 4, 30)
        );

        UserDto dto = mapper.toUserDto(user);

        assertThat(dto.getId()).isEqualTo(rawUserId);
        assertThat(dto.getEmail()).isEqualTo("employee@example.com");
        assertThat(dto.getFullName()).isEqualTo("Ada Lovelace");
        assertThat(dto.getZepUsername()).isEqualTo("adal");
        assertThat(dto.getReleaseDate()).isEqualTo(LocalDate.of(2026, 4, 30));
        assertThat(dto.getRoles()).containsExactlyInAnyOrder("EMPLOYEE", "PROJECT_LEAD");
        assertThat(dto.getPersonioId()).isEqualTo(42);
    }

    @Test
    void toUserDto_shouldMapNullableFieldsAsNull() {
        UUID rawUserId = Instancio.create(UUID.class);
        User user = new User(
                UserId.of(rawUserId),
                Email.of("employee@example.com"),
                FullName.of("Ada", "Lovelace"),
                ZepUsername.of("adal"),
                null,
                EmploymentPeriods.empty(),
                Set.of(Role.EMPLOYEE),
                null
        );

        UserDto dto = mapper.toUserDto(user);

        assertThat(dto.getReleaseDate()).isNull();
        assertThat(dto.getPersonioId()).isNull();
    }
}

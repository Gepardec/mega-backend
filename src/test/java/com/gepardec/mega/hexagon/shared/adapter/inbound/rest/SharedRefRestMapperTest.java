package com.gepardec.mega.hexagon.shared.adapter.inbound.rest;

import com.gepardec.mega.application.configuration.ZepConfig;
import com.gepardec.mega.hexagon.generated.model.ProjectRefDto;
import com.gepardec.mega.hexagon.generated.model.UserRefDto;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SharedRefRestMapperTest {

    private static final String PROJECT_URL_PREFIX = "https://zep.example.test/project/";
    private static final String EMPLOYEE_URL_PREFIX = "https://zep.example.test/employee/";

    private final SharedRefRestMapper mapper = Mappers.getMapper(SharedRefRestMapper.class);

    private ZepConfig createZepConfig() {
        ZepConfig zepConfig = mock(ZepConfig.class);
        when(zepConfig.buildProjectUrl(anyInt()))
                .thenAnswer(invocation -> PROJECT_URL_PREFIX + invocation.getArgument(0, Integer.class));
        when(zepConfig.buildEmployeeUrl(any(ZepUsername.class)))
                .thenAnswer(invocation -> EMPLOYEE_URL_PREFIX + invocation.getArgument(0, ZepUsername.class).value());
        return zepConfig;
    }

    @Test
    void toDto_shouldMapProjectRefWithZepUrl() {
        mapper.setZepConfig(createZepConfig());
        ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
        ProjectRef projectRef = new ProjectRef(projectId, 77, "Project Mapper");

        ProjectRefDto dto = mapper.toDto(projectRef);

        assertThat(dto.getId()).isEqualTo(projectId.value());
        assertThat(dto.getName()).isEqualTo("Project Mapper");
        assertThat(dto.getZepUrl()).isEqualTo(PROJECT_URL_PREFIX + "77");
    }

    @Test
    void toDto_shouldMapUserRefWithZepUrlWhenUsernamePresent() {
        mapper.setZepConfig(createZepConfig());
        UserId userId = UserId.of(Instancio.create(UUID.class));
        UserRef userRef = new UserRef(userId, FullName.of("Mapper", "Employee"), ZepUsername.of("mapper.employee"));

        UserRefDto dto = mapper.toDto(userRef);

        assertThat(dto.getId()).isEqualTo(userId.value());
        assertThat(dto.getFullName()).isEqualTo("Mapper Employee");
        assertThat(dto.getZepUrl()).isEqualTo(EMPLOYEE_URL_PREFIX + "mapper.employee");
    }

    @Test
    void toDto_shouldMapUserRefWithNullZepUrlWhenUsernameMissing() {
        mapper.setZepConfig(createZepConfig());
        UserId userId = UserId.of(Instancio.create(UUID.class));
        UserRef userRef = new UserRef(userId, FullName.of("Mapper", "Employee"), null);

        UserRefDto dto = mapper.toDto(userRef);

        assertThat(dto.getId()).isEqualTo(userId.value());
        assertThat(dto.getFullName()).isEqualTo("Mapper Employee");
        assertThat(dto.getZepUrl()).isNull();
    }
}

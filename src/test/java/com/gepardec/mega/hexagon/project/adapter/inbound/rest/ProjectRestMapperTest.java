package com.gepardec.mega.hexagon.project.adapter.inbound.rest;

import com.gepardec.mega.application.configuration.ZepConfig;
import com.gepardec.mega.hexagon.generated.model.ProjectSettingsDto;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.shared.adapter.inbound.rest.SharedRefRestMapper;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectRestMapperTest {

    private static final String PROJECT_URL_PREFIX = "https://zep.example.test/project/";

    private final SharedRefRestMapper sharedRefRestMapper = createSharedRefRestMapper();
    private final ProjectRestMapper mapper = new ProjectRestMapperImpl(sharedRefRestMapper);

    private SharedRefRestMapper createSharedRefRestMapper() {
        SharedRefRestMapper sharedMapper = Mappers.getMapper(SharedRefRestMapper.class);
        ZepConfig zepConfig = mock(ZepConfig.class);
        when(zepConfig.buildProjectUrl(anyInt()))
                .thenAnswer(invocation -> PROJECT_URL_PREFIX + invocation.getArgument(0, Integer.class));
        sharedMapper.setZepConfig(zepConfig);
        return sharedMapper;
    }

    @Test
    void toDto_shouldMapProjectSettingsEntry() {
        ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
        Project project = new Project(projectId, 77, "Spec First", LocalDate.now(), null, true, true, Set.of());

        ProjectSettingsDto dto = mapper.toDto(project);

        assertThat(dto.getProject().getId()).isEqualTo(projectId.value());
        assertThat(dto.getProject().getName()).isEqualTo("Spec First");
        assertThat(dto.getProject().getZepUrl()).isEqualTo(PROJECT_URL_PREFIX + "77");
        assertThat(dto.getLeistungsnachweisEnabled()).isTrue();
    }
}

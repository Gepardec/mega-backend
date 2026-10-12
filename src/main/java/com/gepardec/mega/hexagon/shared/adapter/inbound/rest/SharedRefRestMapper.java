package com.gepardec.mega.hexagon.shared.adapter.inbound.rest;

import com.gepardec.mega.application.configuration.ZepConfig;
import com.gepardec.mega.hexagon.generated.model.ProjectRefDto;
import com.gepardec.mega.hexagon.generated.model.UserRefDto;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import jakarta.inject.Inject;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public abstract class SharedRefRestMapper {

    private ZepConfig zepConfig;

    @Inject
    public void setZepConfig(ZepConfig zepConfig) {
        this.zepConfig = zepConfig;
    }

    public abstract ProjectRefDto toDto(ProjectRef project);

    @AfterMapping
    protected void enrichProjectRef(ProjectRef project, @MappingTarget ProjectRefDto dto) {
        dto.setZepUrl(zepConfig.buildProjectUrl(project.zepId()));
    }

    public abstract UserRefDto toDto(UserRef user);

    @AfterMapping
    protected void enrichUserRef(UserRef user, @MappingTarget UserRefDto dto) {
        dto.setZepUrl(user.zepUsername() != null ? zepConfig.buildEmployeeUrl(user.zepUsername()) : null);
    }

    protected UUID map(ProjectId projectId) {
        return projectId == null ? null : projectId.value();
    }

    protected UUID map(UserId userId) {
        return userId == null ? null : userId.value();
    }

    protected String map(FullName fullName) {
        return fullName == null ? null : fullName.displayName();
    }
}

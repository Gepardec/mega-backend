package com.gepardec.mega.hexagon.monthend.adapter.outbound;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA)
public interface MonthEndProjectSnapshotMapper {

    @Mapping(target = "leadIds", source = "leads", qualifiedByName = "uuidsToUserIds")
    MonthEndProjectSnapshot toSnapshot(Project project);

    @Named("uuidsToUserIds")
    default Set<UserId> uuidsToUserIds(Set<UUID> ids) {
        if (ids == null) {
            return Set.of();
        }
        return ids.stream()
                .map(UserId::of)
                .collect(Collectors.toUnmodifiableSet());
    }
}

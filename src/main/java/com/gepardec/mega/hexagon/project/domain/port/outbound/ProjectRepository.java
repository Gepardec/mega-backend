package com.gepardec.mega.hexagon.project.domain.port.outbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProjectRepository {

    Optional<Project> findByZepId(int zepId);

    List<Project> findAll();

    List<Project> findAllByLead(UserId leadId);

    List<Project> findAllByIds(Set<ProjectId> projectIds);

    void saveAll(List<Project> projects);
}

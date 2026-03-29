package com.gepardec.mega.hexagon.project.domain.port.outbound;

import com.gepardec.mega.hexagon.project.domain.model.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {

    Optional<Project> findByZepId(int zepId);

    List<Project> findAll();

    void saveAll(List<Project> projects);
}

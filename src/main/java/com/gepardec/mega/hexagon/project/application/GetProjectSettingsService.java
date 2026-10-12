package com.gepardec.mega.hexagon.project.application;

import com.gepardec.mega.hexagon.project.application.port.inbound.GetProjectSettingsUseCase;
import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.YearMonth;
import java.util.List;

@ApplicationScoped
@Transactional
public class GetProjectSettingsService implements GetProjectSettingsUseCase {

    private final ProjectRepository projectRepository;
    private final Clock clock;

    @Inject
    public GetProjectSettingsService(ProjectRepository projectRepository, Clock clock) {
        this.projectRepository = projectRepository;
        this.clock = clock;
    }

    @Override
    public List<Project> getLeadProjects(UserId actorId) {
        YearMonth currentMonth = YearMonth.now(clock);
        return projectRepository.findAllByLead(actorId).stream()
                .filter(project -> project.isLeistungsnachweisConfigurableFrom(currentMonth))
                .toList();
    }
}

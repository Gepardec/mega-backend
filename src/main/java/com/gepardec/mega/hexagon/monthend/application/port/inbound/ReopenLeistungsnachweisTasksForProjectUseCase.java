package com.gepardec.mega.hexagon.monthend.application.port.inbound;

import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;

import java.time.YearMonth;

public interface ReopenLeistungsnachweisTasksForProjectUseCase {

    void reopenClosedTasks(ProjectId projectId, YearMonth month);
}

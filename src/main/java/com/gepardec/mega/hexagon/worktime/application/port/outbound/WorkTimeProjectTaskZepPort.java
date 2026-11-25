package com.gepardec.mega.hexagon.worktime.application.port.outbound;

import com.gepardec.mega.hexagon.worktime.domain.model.ProjectTask;

import java.util.List;
import java.util.Set;

public interface WorkTimeProjectTaskZepPort {

    List<ProjectTask> fetchTasks(Set<Integer> projectZepIds);
}

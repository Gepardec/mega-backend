package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.Capacity;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MonthEndScopeTest {

    private final UserId actorId = UserId.of(UUID.randomUUID());

    @Test
    void of_shouldMapEmployeeCapacityToSubjectEmployeeScope() {
        assertThat(MonthEndScope.of(Capacity.EMPLOYEE, actorId))
                .isEqualTo(new MonthEndScope.SubjectEmployee(actorId));
    }

    @Test
    void of_shouldMapControllingCapacityToLedProjectsScope() {
        assertThat(MonthEndScope.of(Capacity.CONTROLLING, actorId))
                .isEqualTo(new MonthEndScope.LedProjects(actorId));
    }
}

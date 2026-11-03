package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.Capacity;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;

import java.util.Objects;

/**
 * The set of month-end records an actor reads in a capacity.
 */
public sealed interface MonthEndScope {

    UserId actorId();

    static MonthEndScope of(Capacity capacity, UserId actorId) {
        Objects.requireNonNull(capacity, "capacity must not be null");
        return switch (capacity) {
            case EMPLOYEE -> new SubjectEmployee(actorId);
            case CONTROLLING -> new LedProjects(actorId);
        };
    }

    /** Records where the actor is the subject employee. */
    record SubjectEmployee(UserId actorId) implements MonthEndScope {

        public SubjectEmployee {
            Objects.requireNonNull(actorId, "actorId must not be null");
        }
    }

    /**
     * Records of the projects the actor is accountable for. For tasks this covers every project on whose shared
     * (any-eligible-actor) tasks the actor is eligible, which includes projects the actor does not lead, such as
     * those whose Leistungsnachweis tasks list the actor as project coordinator. For clarifications it follows the
     * lead snapshot recorded on the clarification.
     */
    record LedProjects(UserId actorId) implements MonthEndScope {

        public LedProjects {
            Objects.requireNonNull(actorId, "actorId must not be null");
        }
    }
}

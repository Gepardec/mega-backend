## Context

The `MonthEndTask` aggregate already models two distinct actor relationships: `eligibleActorIds` (who can complete the task) and `subjectEmployeeId` (who the task is about). The existing status overview service uses `findTasksForActor` on `MonthEndTaskRepository`, which queries only tasks where the actor appears in `eligibleActorIds`. This means employees never see `PROJECT_LEAD_REVIEW` tasks about them — those tasks are visible only to the leads who are eligible to complete them.

The frontend needs a unified view combining tasks the actor can act on with tasks they can only observe. Rather than giving the client the raw `eligibleActorIds` set, the API should compute and return a `canComplete: boolean` per entry — one derived fact the frontend can act on directly.

## Goals / Non-Goals

**Goals:**
- Overview returns tasks where actor is eligible **or** subject employee.
- Each overview entry carries `canComplete: boolean` so the frontend renders action controls or read-only state without further computation.
- `canComplete` is computed server-side: `task.eligibleActorIds().contains(actorId)`.
- `findTasksForActor` is renamed to `findVisibleTasksForActor` to reflect its expanded scope.

**Non-Goals:**
- Exposing `eligibleActorIds` on the API — `canComplete` is sufficient.
- Changing worklist semantics (remains open-only, eligible-only).
- Changing task completion authorization logic.

## Decisions

### `canComplete` computed in the application service, carried in the domain read model

**Decision**: `GetMonthEndStatusOverviewService` computes `canComplete = task.eligibleActorIds().contains(actorId)` when building `MonthEndStatusOverviewItem`. The domain read model carries the flag; the REST mapper passes it through without logic.

**Rationale**: The overview is an actor-scoped read model — it is inherently relative to a specific actor. The service has both `actorId` and the full task (including `eligibleActorIds`) at construction time, making this the natural place for the computation. Keeping this out of the REST mapper avoids leaking domain logic into the adapter.

**Alternative considered**: Compute `canComplete` in the REST mapper from the response data. Rejected because the REST layer would need access to `eligibleActorIds`, which is an internal concern not otherwise exposed via the API.

### Single extended query via `findVisibleTasksForActor` (renamed method)

**Decision**: Replace `findTasksForActor` with `findVisibleTasksForActor` using a single JPQL query that matches tasks where the actor is eligible **or** is the subject employee:

```sql
SELECT DISTINCT task FROM MonthEndTaskEntity task
LEFT JOIN task.eligibleActorIds actor
WHERE task.monthValue = ?1
  AND (actor = ?2 OR task.subjectEmployeeId = ?2)
```

**Rationale**: A single query is simpler than fetching two sets and deduplicating in Java. `DISTINCT` handles the case where an actor appears in both relationships (EMPLOYEE_TIME_CHECK / LEISTUNGSNACHWEIS). The rename makes the broader scope explicit at the port level.

**Alternative considered**: A separate `findTasksWhereActorIsSubjectOnly` method unioned in the service. Rejected — adds complexity and a deduplication step for no benefit.

### `canComplete` is `true` when actor is eligible, regardless of subject relationship

**Decision**: "Eligible always wins." If an actor is both subject and eligible (EMPLOYEE_TIME_CHECK, LEISTUNGSNACHWEIS), `canComplete = true`.

**Rationale**: Eligibility is already the authoritative signal for completion rights in the domain. The subject relationship adds visibility but not action rights.

## Risks / Trade-offs

- **Expanded query result set** — actors will now see more tasks in their overview (subject-only tasks). This is intentional, but frontend consumers must be prepared for the change. The breaking addition of `canComplete` to the response forces a contract version bump, surfacing this at integration time. → Mitigated by the BREAKING label in the proposal.
- **LEFT JOIN semantics** — using `LEFT JOIN` + `DISTINCT` with an `OR` condition on a collection join can produce unexpected duplicates if the ORM handles it differently across Hibernate versions. → Covered by integration tests against H2 in the test suite.

## Open Questions

None — design is fully resolved based on the exploration session.

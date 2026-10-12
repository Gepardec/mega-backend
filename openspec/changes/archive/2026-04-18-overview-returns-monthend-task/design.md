## Context

The month-end status overview currently assembles a pre-enriched view in the application layer:

```
GetEmployee/ProjectLeadMonthEndStatusOverviewService
  └─ AssembleMonthEndStatusOverviewService
       └─ ResolveMonthEndTaskSnapshotLookupService
            ├─ MonthEndProjectSnapshotPort.findByIds()
            └─ MonthEndUserSnapshotPort.findByIds()
       └─ MonthEndTask → MonthEndStatusOverviewItem
            (embeds ProjectRef, UserRef, canComplete)
  → MonthEndStatusOverview { entries: List<MonthEndStatusOverviewItem> }

MonthEndEmployee/ProjectLeadResource
  └─ resolves userRefs for clarifications only (task refs already embedded)
  └─ mapper.toResponse(overview, userRefs, actorId)
```

The clarification path already does this correctly: use cases return raw `MonthEndClarification` objects and the REST adapter resolves all display data. The task path should match.

Target state:

```
GetEmployee/ProjectLeadMonthEndStatusOverviewService
  → MonthEndStatusOverview { entries: List<MonthEndTask> }   ← raw domain

MonthEndEmployee/ProjectLeadResource
  ├─ projectRefs = MonthEndProjectSnapshotPort.findByIds(taskProjectIds, month)
  ├─ userRefs = MonthEndUserSnapshotPort.findByIds(taskSubjectIds ∪ clarificationUserIds, month)
  └─ mapper.toEntry(task, @Context projectRefs, @Context userRefs, @Context actorId)
       └─ @AfterMapping: enrich project, subjectEmployee, canComplete, completedBy
```

## Goals / Non-Goals

**Goals:**
- Move all snapshot resolution and actor-flag computation for tasks to the REST adapter layer
- Delete `MonthEndStatusOverviewItem`, `AssembleMonthEndStatusOverviewService`, `ResolveMonthEndTaskSnapshotLookupService`, `MonthEndTaskSnapshotLookup`
- Leave the external API contract (response shape) completely unchanged

**Non-Goals:**
- Changing any overview query logic or repository methods
- Refactoring the mapper for any path other than the overview task entry
- Changing clarification enrichment (already correct)

## Decisions

**`canBeCompletedBy(UserId actorId)` as a domain method on `MonthEndTask`**
Symmetric to `MonthEndClarification.canBeResolvedBy(actorId)`. The mapper calls `task.canBeCompletedBy(actorId)` in `@AfterMapping`, same as `clarification.canBeResolvedBy(actorId)` for clarification entries. Alternative — inline `task.eligibleActorIds().contains(actorId)` in the mapper — was rejected: a named method is more readable, testable, and consistent with the clarification pattern.

**One batch userRef lookup (tasks + clarifications merged)**
The REST adapter collects `subjectEmployeeId` from every task (filtering nulls) plus all user IDs from all clarifications, then calls `userSnapshotPort.findByIds()` once. This avoids two separate round-trips and is consistent with the existing batch pattern used for clarification-only lookups. The snapshot port is already designed for set-based lookup.

**Inject `MonthEndProjectSnapshotPort` directly in the REST resource**
Both REST resources already inject `MonthEndUserSnapshotPort` directly for clarification enrichment. Following the same pattern for `MonthEndProjectSnapshotPort` is consistent and avoids introducing a new use-case method just to proxy a read-only snapshot lookup.

**`MonthEndStatusOverview` domain record changes its `entries` field type**
Changing `List<MonthEndStatusOverviewItem>` to `List<MonthEndTask>` in the domain record is the minimal structural change. The record's purpose remains the same — it is the use-case output carrying tasks and clarifications for a given actor and month. No new record is introduced.

**`ResolveMonthEndTaskSnapshotLookupService` and `MonthEndTaskSnapshotLookup` deleted outright**
After `drop-monthend-worklist` they have exactly one caller: `AssembleMonthEndStatusOverviewService`, which is itself deleted by this change. No deprecation markers are needed.

## Risks / Trade-offs

- **`@AfterMapping` in mapper grows** → The mapper already uses `@AfterMapping` for clarification enrichment; adding a parallel one for task entries follows the established pattern. Complexity is localised and symmetrical.
- **Null `subjectEmployeeId` on `ABRECHNUNG` tasks** → Collecting task subject IDs for the batch userRef lookup must filter nulls. The existing `MonthEndTaskSnapshotLookup.userFor()` already handles this; the replacement logic in the REST resource must do the same.
- **ProjectRef lookup could return fewer results than expected** → If a project snapshot is missing for a task's `projectId`, the mapper will encounter a null in the context map. The `@AfterMapping` should match the defensive handling pattern already established — throw `IllegalStateException` with the project ID, same as `MonthEndTaskSnapshotLookup.projectFor()` did previously.

## Migration Plan

No data migration. No schema changes. Standard redeploy. Rollback: revert the PR.

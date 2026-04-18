## Why

`MonthEndStatusOverviewItem` sits in `domain/model/` carrying `ProjectRef` and `UserRef` — presentation-layer enrichments that have no business being in the domain. The `AssembleMonthEndStatusOverviewService` compounds this by calling outbound snapshot ports from the application layer to populate those references. With the worklist removal (`drop-monthend-worklist`) there are now no other callers of this infrastructure, making this the right moment to clean it up and align the overview fully with the pattern established for clarifications: the use case returns raw domain objects, the REST adapter resolves display references and computes actor-specific flags.

## What Changes

- Add `MonthEndTask.canBeCompletedBy(UserId actorId)` domain method — returns `true` iff `actorId` is in `eligibleActorIds`; symmetric to `MonthEndClarification.canBeResolvedBy(actorId)`
- Change `MonthEndStatusOverview.entries` from `List<MonthEndStatusOverviewItem>` to `List<MonthEndTask>`
- Simplify `GetEmployeeMonthEndStatusOverviewService` and `GetProjectLeadMonthEndStatusOverviewService` — remove `AssembleMonthEndStatusOverviewService` dependency; directly assemble `MonthEndStatusOverview` from loaded tasks and clarifications
- Update overview endpoints in `MonthEndEmployeeResource` and `MonthEndProjectLeadResource`:
  - Inject `MonthEndProjectSnapshotPort` (same pattern as existing `MonthEndUserSnapshotPort`)
  - Resolve `projectRefs` from task project IDs via `MonthEndProjectSnapshotPort.findByIds()`
  - Resolve `userRefs` in one batch: task subject employee IDs merged with clarification user IDs
  - Pass `projectRefs`, `userRefs`, and `actorId` to the mapper
- Update `MonthEndRestMapper`: remove `toResponse(MonthEndStatusOverviewItem)`, add `toEntry(MonthEndTask, @Context projectRefs, @Context userRefs, @Context actorId)` with `@AfterMapping` enrichment
- Delete `MonthEndStatusOverviewItem`, `AssembleMonthEndStatusOverviewService`, `ResolveMonthEndTaskSnapshotLookupService`, `MonthEndTaskSnapshotLookup` and their tests

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `monthend-status-overview`: clarify that `canComplete` is computed by the REST adapter via `task.canBeCompletedBy(actorId)`, not pre-assembled by an application service
- `shared-user-project-refs`: update UserRef and ProjectRef monthend scenarios — references are now on `MonthEndTask` (via REST mapper enrichment) rather than `MonthEndStatusOverviewItem`

## Impact

- **Code**: 4 classes deleted, 2 application services simplified, 2 REST resources updated, mapper updated
- **Tests**: 2 test classes deleted (`AssembleMonthEndStatusOverviewServiceTest`, `ResolveMonthEndTaskSnapshotLookupServiceTest`); overview service tests updated
- **API**: No change — endpoint contracts and response shapes are identical
- **Architecture**: Overview path is now consistent with the clarification enrichment pattern throughout

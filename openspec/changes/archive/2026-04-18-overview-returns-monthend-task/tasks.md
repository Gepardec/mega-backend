## 1. Extend Domain Model

- [x] 1.1 Add `canBeCompletedBy(UserId actorId)` method to `MonthEndTask` — returns `true` iff `actorId` is in `eligibleActorIds`
- [x] 1.2 Change `MonthEndStatusOverview.entries` field type from `List<MonthEndStatusOverviewItem>` to `List<MonthEndTask>`

## 2. Simplify Application Services

- [x] 2.1 Update `GetEmployeeMonthEndStatusOverviewService`: remove `AssembleMonthEndStatusOverviewService` dependency; directly construct `MonthEndStatusOverview` from loaded tasks and clarifications
- [x] 2.2 Update `GetProjectLeadMonthEndStatusOverviewService`: same simplification as 2.1

## 3. Update REST Resources

- [x] 3.1 Inject `MonthEndProjectSnapshotPort` into `MonthEndEmployeeResource`
- [x] 3.2 Update `getEmployeeMonthEndStatusOverview()`: resolve `projectRefs` from task project IDs; merge task subject employee IDs with clarification user IDs for a single `userSnapshotPort.findByIds()` call; pass both maps and `actorId` to the mapper
- [x] 3.3 Inject `MonthEndProjectSnapshotPort` into `MonthEndProjectLeadResource`
- [x] 3.4 Update `getProjectLeadMonthEndStatusOverview()`: same resolution logic as 3.2

## 4. Update REST Mapper

- [x] 4.1 Remove `toResponse(MonthEndStatusOverviewItem item)` from `MonthEndRestMapper`
- [x] 4.2 Add `toEntry(MonthEndTask task, @Context Map<ProjectId, ProjectRef> projectRefs, @Context Map<UserId, UserRef> userRefs, @Context UserId actorId)` to `MonthEndRestMapper`
- [x] 4.3 Add `@AfterMapping` method to enrich the mapped entry: set `project` from `projectRefs`, `subjectEmployee` from `userRefs` (nullable), `canComplete` via `task.canBeCompletedBy(actorId)`, `completedBy` from `userRefs` (nullable)
- [x] 4.4 Update `MonthEndStatusOverviewResponse toResponse(MonthEndStatusOverview, ...)` to iterate `List<MonthEndTask>` entries using the new `toEntry` mapping

## 5. Delete Obsolete Classes

- [x] 5.1 Delete `MonthEndStatusOverviewItem.java`
- [x] 5.2 Delete `AssembleMonthEndStatusOverviewService.java`
- [x] 5.3 Delete `ResolveMonthEndTaskSnapshotLookupService.java`
- [x] 5.4 Delete `MonthEndTaskSnapshotLookup.java`

## 6. Delete Obsolete Tests

- [x] 6.1 Delete `AssembleMonthEndStatusOverviewServiceTest.java`
- [x] 6.2 Delete `ResolveMonthEndTaskSnapshotLookupServiceTest.java`
- [x] 6.3 Update `GetEmployeeMonthEndStatusOverviewServiceTest` — remove mock of `AssembleMonthEndStatusOverviewService`, assert on raw `MonthEndTask` entries in the returned overview
- [x] 6.4 Update `GetProjectLeadMonthEndStatusOverviewServiceTest` — same as 6.3

## 7. Update Specs

- [x] 7.1 Apply delta to `openspec/specs/monthend-status-overview/spec.md` — add no-pre-enrichment rule to "Status overview is derived..." requirement; update "Status overview entries expose whether the actor can complete the task" to reference `task.canBeCompletedBy(actorId)`
- [x] 7.2 Apply delta to `openspec/specs/shared-user-project-refs/spec.md` — update UserRef and ProjectRef monthend scenarios to reference REST adapter enrichment of `MonthEndTask` entries rather than `MonthEndStatusOverviewItem`

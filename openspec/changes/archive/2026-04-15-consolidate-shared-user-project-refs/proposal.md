## Why

Seven near-duplicate snapshot and reference types across the `monthend` and `worktime` modules represent the same user and project data in slightly different shapes, forcing adapters and mappers to maintain redundant mappings and keeping activeness filtering scattered across application services instead of enforced at the port boundary.

## What Changes

- Add `UserRef { UserId id, FullName fullName, ZepUsername zepUsername }` to `shared/domain/model/` as the canonical cross-module user reference
- Add `ProjectRef { ProjectId id, int zepId, String name }` to `shared/domain/model/` as the canonical cross-module project reference
- Move `FullName` and `ZepUsername` value objects from `user/domain/model/` to `shared/domain/model/`
- Delete `MonthEndUserSnapshot`, `MonthEndEmployee`, `WorkTimeUserSnapshot`, `WorkTimeEmployee` — all replaced by `UserRef`
- Delete `MonthEndProject`, `WorkTimeProject`, `WorkTimeProjectSnapshot` — all replaced by `ProjectRef`
- Change user and project snapshot port signatures from `findAll()` to `findActiveIn(YearMonth month)` — activeness filtering moves from application service to adapter layer
- Trim `MonthEndProjectSnapshot`: remove `startDate`/`endDate` (no longer needed once active filtering moves to the port); retain `{ id, zepId, name, billable, leadIds }`
- Remove `isActiveIn()` methods from `MonthEndUserSnapshot` and `MonthEndProjectSnapshot` (behaviour moves to adapters)
- Simplify `MonthEndWorklistMapper`: remove `toProject()` and `toSubjectEmployee()` projection methods; `MonthEndWorklistItem` holds `UserRef` and `MonthEndProjectSnapshot` directly

## Capabilities

### New Capabilities

- `shared-user-project-refs`: Canonical cross-module reference types (`UserRef`, `ProjectRef`) in the shared kernel, and the rule that ports returning users/projects to other modules expose only active records with activeness filtering enforced at the adapter layer

### Modified Capabilities

- `user-aggregate`: `FullName` and `ZepUsername` relocate to `shared/domain/model/`; the `User` aggregate retains all other fields and behaviour
- `project-aggregate`: `MonthEndProjectSnapshot` loses `startDate`/`endDate`; `ProjectId` import origin changes from `project/domain/model/` to `shared/domain/model/` (already done in consolidate-hexagon-architecture)

## Impact

- Import updates across `monthend` and `worktime` modules — mechanical find/replace of deleted types
- Outbound adapter implementations for user/project snapshot ports must push activeness filtering into the underlying query (DB-level preferred, in-memory within the adapter acceptable)
- `GenerateMonthEndTasksService`: `.filter(user -> user.isActiveIn(month))` and the equivalent project filter removed; application service receives pre-filtered lists from the port
- No API contract changes, no DB schema changes
- No impact on legacy `com.gepardec.mega` package

## Why

An architectural review of the hexagonal backend identified four structural misplacements where classes live in the wrong layer or module — inbound ports in the domain layer, a domain service in the application layer, an inbound adapter in the application layer, and shared identity types siloed inside individual module domains. Correcting these now keeps the architecture honest before the codebase grows further.

## What Changes

- **Inbound ports moved to `application/`**: Use case interfaces (`*UseCase`) currently live in `*.domain.port.inbound`. They are driver port contracts — they define how the outside world drives the application, not domain concepts. Move all inbound port interfaces into their module's `application/` package, co-located with their implementations.
- **`MonthEndTaskPlanningService` reclassified as domain service**: This service contains pure task-planning business rules (which task types to create, billability conditions, lead requirements) with no I/O or port dependencies. Move from `monthend/application/` to `monthend/domain/services/`.
- **`SyncScheduler` reclassified as inbound adapter**: The unified sync scheduler is a Quarkus `@Scheduled` component that drives use cases. It is an inbound adapter, not an application service. Move from `application/schedule/` to `shared/adapter/inbound/`.
- **Shared identity types promoted to shared kernel**: `UserId` and `Email` live in `user/domain/model/`; `ProjectId` lives in `project/domain/model/`. All three are used across every module (monthend, worktime, project, user). Move them to `shared/domain/model/` — the shared kernel of the single bounded context.

All changes are package moves only. No logic, method signatures, or behaviour changes.

## Capabilities

### New Capabilities

*(none)*

### Modified Capabilities

- `hexagon-boundary-conventions`: Two requirements need updates — shared kernel ownership must now explicitly name `UserId`, `ProjectId`, `Email` as shared kernel residents and reference `shared/domain/model/` as their home; a new requirement codifies that inbound ports (driver port interfaces) belong in the application layer.
- `project-aggregate`: The requirement constraining `Project` class imports must be updated — `ProjectId` moves from `hexagon.project.domain.model` to `hexagon.shared.domain.model`, so the permitted import origins change.

## Impact

- All Java files that import `UserId`, `Email`, or `ProjectId` require import updates (mechanical, widespread).
- All Java files that import any `*.domain.port.inbound.*` use case interface require import updates.
- `ArchitectureTest.java` package path strings for inbound ports must be updated.
- Both `src/main` and `src/test` trees affected.
- No API contract changes. No database schema changes.

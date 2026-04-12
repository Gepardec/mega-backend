## Why

The project hexagon slice still uses the pre-reshape style that the user domain just moved away from: a mutable aggregate, manually constructed application services, and older sync/reconcile flow conventions. Bringing `project` in line now reduces architectural drift inside `com.gepardec.mega.hexagon` and makes future cross-domain work easier to reason about.

## What Changes

- Reshape the hexagon `Project` aggregate from a mutable class into an immutable record-oriented model with explicit state-transition methods.
- Remove temporary JavaBean-style compatibility accessors from the project aggregate and update downstream call sites to use direct record accessors.
- Type project leads consistently as `UserId` references inside the project domain instead of raw `UUID` collections.
- Move `SyncProjectsService` and `ReconcileLeadsService` onto CDI-managed application-service boundaries with transactions owned by the service layer.
- Update the unified `SyncScheduler` to inject project use cases instead of manually constructing project services.
- Align project sync and lead reconciliation implementation style with the reshaped user domain by deriving new aggregate instances and persisting only created or changed state.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `project-aggregate`: change the aggregate to an immutable record-oriented shape with typed lead references and native record accessors
- `project-sync`: move sync to a CDI-managed transactional application service and align the update flow with immutable project state
- `reconcile-leads`: move lead reconciliation to a CDI-managed transactional application service and align lead replacement with immutable project state

## Impact

- Affects `src/main/java/com/gepardec/mega/hexagon/project`, `src/main/java/com/gepardec/mega/hexagon/application/schedule`, and downstream monthend/worktime code that reads `Project`.
- Affects project domain, application, mapper, repository, scheduler, and test code.
- Does not require REST API changes or database schema changes.
- Should avoid adapter and port renames that belong to the active `standardize-hexagon-shared-boundaries` change.

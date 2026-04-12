## Context

The archived change `2026-04-13-reshape-hexagon-user-sync` established a new baseline for hexagon slices: immutable record-oriented domain models, CDI-managed application services, and sync flows that derive new aggregate instances instead of mutating them in place. The project slice still diverges from that baseline.

Today, `Project` is a mutable class, project leads are handled as raw `UUID` collections in the implementation, and `SyncScheduler` manually constructs `SyncProjectsService` and `ReconcileLeadsService` instead of injecting them as application services. That divergence is visible in the domain model, mapper/repository code, scheduler wiring, and tests.

This change also needs to coexist with the active `standardize-hexagon-shared-boundaries` work. That change is already exploring renames and longer-term shared identity ownership, so this proposal should focus on the project reshape itself and avoid consuming the naming refactor prematurely.

## Goals / Non-Goals

**Goals:**
- Align the project slice with the user-domain reshape rules: immutable record aggregate, CDI-managed service boundary, and similar sync/reconcile implementation style.
- Model project leads in the domain as `UserId` references instead of raw `UUID` collections.
- Remove compatibility `getX()` wrappers and switch affected call sites to native record accessors.
- Keep the externally visible project behavior stable: same scheduler flow and persistence schema, while extending project sync reporting with an `unchanged` counter.

**Non-Goals:**
- Rename ports or adapters as part of the active shared-boundary standardization work.
- Introduce new project capabilities, REST endpoints, or persistence tables.
- Change the scheduler cadence or the ordering of user sync, project sync, and lead reconciliation.
- Redesign monthend or worktime domain models beyond the call-site adjustments needed for the new `Project` shape.

## Decisions

### 1. Model `Project` as an immutable record with native record accessors

`Project` will become a record-oriented aggregate with direct component accessors such as `id()`, `zepId()`, `name()`, and `leads()`. State transitions such as project resync and lead replacement will be expressed as methods that return a new `Project` instance.

Why this decision:
- It directly mirrors the reshape rule the user wants applied from the user domain.
- It removes in-place mutation from project sync and lead reconciliation.
- It makes equality and change detection straightforward when comparing existing and synchronized project state.

Alternatives considered:
- Keep the mutable class and only annotate the services: rejected because the project slice would still diverge from the new domain-model style.
- Keep `getX()` compatibility wrappers on the record: rejected because the user explicitly does not want them and they dilute the point of the reshape.

### 2. Keep the persistence schema stable and move type normalization into the mapper layer

The database shape will stay as-is, including UUID storage for project lead references in `hexagon_project_leads`. The domain model will use `Set<UserId>`, and the project mapper/repository layer will convert between `UserId` and persisted UUID values.

Why this decision:
- It brings the domain model in line with the existing OpenSpec intent without forcing a Liquibase change.
- It keeps the scope focused on the project slice reshape rather than a data migration.
- It minimizes rollout and rollback risk.

Alternatives considered:
- Keep raw `UUID` in the domain to avoid mapper changes: rejected because it preserves the typing mismatch the reshape is supposed to remove.
- Add a schema change just to store a different lead type: rejected because persistence already stores the needed identifier and no database behavior needs to change.

### 3. Put CDI and transaction boundaries on project application services

`SyncProjectsService` and `ReconcileLeadsService` will become CDI-managed application services that own their transaction boundaries. `SyncScheduler` will inject `SyncProjectsUseCase` and `ReconcileLeadsUseCase` instead of constructing service implementations manually.

Why this decision:
- It matches the reshaped user sync and the existing monthend application-service style.
- It makes lifecycle and transaction handling explicit at the service boundary instead of hiding wiring inside the scheduler.
- It keeps the aggregate and inbound port interfaces framework-free while letting the service implementation integrate with Quarkus naturally.

Alternatives considered:
- Keep manual construction inside `SyncScheduler`: rejected because it continues the divergence from the user slice and bypasses CDI/service lifecycle management.
- Push transactions down into repository adapters only: rejected because the intended unit of work is the use-case invocation.

### 4. Align sync and reconciliation flow with the user-domain update style

Project sync and lead reconciliation will derive new aggregate instances from current state plus incoming ZEP data, compare existing and derived state, and persist only created or changed aggregates. `ProjectSyncResult` will expose `created`, `updated`, and `unchanged` counts so the scheduler can report skipped writes explicitly, while `ReconcileLeadsResult` keeps its existing shape.

Why this decision:
- It matches the control-flow style introduced in the user sync reshape and makes no-op sync outcomes visible.
- It prevents records from being treated like mutable containers.
- It avoids unnecessary writes when project state or lead assignments are unchanged.

Alternatives considered:
- Persist every project on every sync/reconcile pass: rejected because it works against the new immutable/equality-driven style.
- Keep project sync results limited to `created` and `updated`: rejected because the implementation now distinguishes unchanged projects explicitly and the scheduler log benefits from reporting that outcome.

### 5. Defer boundary naming cleanup to the active shared-boundary change

This proposal will not rename `UserLookupPort`, `ProjectRepositoryAdapter`, or other project-side abstractions even where the active `standardize-hexagon-shared-boundaries` change recommends follow-up cleanup.

Why this decision:
- It avoids mixing two refactors with overlapping file touch sets.
- It keeps this change narrowly focused on the project reshape rules requested by the user.
- It reduces merge friction with the in-progress boundary-standardization work.

Alternatives considered:
- Fold the naming refactor into this proposal: rejected because it broadens scope and duplicates work already being explored elsewhere.

## Risks / Trade-offs

- [Risk] Removing `getX()` wrappers will touch a wide set of project call sites in tests and downstream code. -> Mitigation: update project, monthend, and worktime call sites together and rely on targeted compilation/tests to catch stragglers.
- [Risk] Record equality can cause unexpected update detection if set normalization is inconsistent. -> Mitigation: normalize lead sets with `Set.copyOf(...)` in the record constructor and mapper conversions.
- [Risk] The active `standardize-hexagon-shared-boundaries` change may later rename some of the same ports and adapters. -> Mitigation: keep names stable here and treat any rename as a follow-up on top of the reshaped project model.
- [Risk] Changing the service boundary to CDI may expose test setup assumptions that relied on manual construction. -> Mitigation: keep constructor injection and update unit tests to instantiate the services directly where appropriate.

## Migration Plan

1. Convert `Project` and its mapper/repository usage to the new record-oriented shape with `UserId` lead references.
2. Refactor `SyncProjectsService` and `ReconcileLeadsService` to use immutable project derivation and CDI-managed transaction boundaries.
3. Update `SyncScheduler` to inject the project use cases.
4. Update downstream project consumers and tests to use record component accessors.
5. Run targeted project, monthend, and worktime tests to verify that project sync and lead reconciliation behavior stays stable.

Rollback strategy:
- This is a code-only refactor with no schema migration, so rollback is a straightforward revert of the affected source and test files.

## Open Questions

- None currently.

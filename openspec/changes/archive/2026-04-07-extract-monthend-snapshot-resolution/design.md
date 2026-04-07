## Context

`GetMonthEndStatusOverviewService`, `GetEmployeeMonthEndWorklistService`, and `GetProjectLeadMonthEndWorklistService` all enrich `MonthEndTask` query results with project and subject-employee references before mapping them to read models. After the recent worklist enrichment change, each service now repeats the same four responsibilities:

- collect project ids and nullable subject-employee ids from a task list
- batch load matching snapshots through outbound ports
- build lookup maps keyed by `ProjectId` and `UserId`
- fail fast when a required snapshot is missing

This logic lives in the application layer because it orchestrates outbound ports and prepares query-model input for MapStruct mappers. The domain model and external API stay unchanged.

## Goals / Non-Goals

**Goals:**
- remove duplicated snapshot resolution logic from the three month-end query services
- preserve the current batch-loading behavior and current failure semantics for missing snapshots
- keep the mapping call sites simple so the services focus on selecting tasks and assembling the final read model
- make the shared resolution rules unit-testable in one place

**Non-Goals:**
- changing the shape of worklist or status overview responses
- moving the logic into the domain layer
- reworking the existing `ResolveMonthEndEmployeeProjectContextService`, which solves a different validation use case
- introducing new snapshot repository operations or changing persistence adapters

## Decisions

### Create a dedicated application-layer task snapshot resolver
Introduce a new application-scoped collaborator dedicated to resolving snapshot references for a list of `MonthEndTask` records used by query services.

Rationale:
- the logic depends on outbound ports, so it belongs in the application layer rather than the domain
- the three callers share the same lookup semantics and should not each rebuild the same maps
- a dedicated type gives this concern a clear name and keeps the query services thin

Alternative considered:
- extend `ResolveMonthEndEmployeeProjectContextService` to cover this use case. Rejected because that service resolves a single month-aware validation context and also checks project assignment rules, which are unrelated to read-model enrichment.

### Return an immutable lookup object instead of raw maps
The resolver should return a small immutable lookup/value object that encapsulates the loaded snapshots and exposes methods such as `projectFor(ProjectId)` and `subjectEmployeeFor(UserId)`.

Rationale:
- keeps "missing snapshot" checks centralized instead of leaking map access back into each caller
- avoids repeating null-handling for absent subject employees
- makes the returned structure intention-revealing and easy to test

Alternative considered:
- return `Map<ProjectId, MonthEndProjectSnapshot>` and `Map<UserId, MonthEndUserSnapshot>` directly. Rejected because it would still duplicate validation and access logic in each service.

### Preserve current exception behavior in this refactor
The resolver should keep the existing fail-fast behavior when a referenced project or non-null subject employee snapshot is missing, including equivalent message content.

Rationale:
- this proposal is intentionally a refactor, not a behavior change
- preserving current semantics avoids hidden API or test regressions

Alternative considered:
- replace `IllegalStateException` with new domain-specific exceptions. Rejected for now because it would expand the scope from refactoring into behavior and contract changes.

### Update tests around the new seam
Add dedicated unit tests for the resolver and simplify the existing service tests so they verify task selection and mapping outcomes while relying on the shared resolver behavior once.

Rationale:
- the duplicated logic currently requires repeated test setup
- one focused test suite around the resolver gives better coverage for edge cases such as null subject employees and missing snapshots

## Risks / Trade-offs

- Shared helper becomes a dumping ground for unrelated query logic -> Mitigation: keep the resolver limited to loading and validating task snapshot references only.
- Refactor accidentally changes exception timing or message text -> Mitigation: add focused tests that assert the current failure paths.
- Another resolver overlaps with `ResolveMonthEndEmployeeProjectContextService` -> Mitigation: document the different responsibility boundaries and keep the APIs intentionally distinct.

## Migration Plan

No runtime migration is required. The change is an internal refactor that can be released with the normal application deployment and rolled back by reverting the code change if needed.

## Open Questions

No open questions at the proposal level. If we later want richer error types for missing snapshots, that should be proposed as a separate behavior-focused change.

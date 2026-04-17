## Context

`MonthEndStatusOverview` currently holds only task entries. Clarifications live in a separate `MonthEndClarification` aggregate scoped to a month, project, and subject employee — the same axes as tasks. The two overview services (`GetEmployeeMonthEndStatusOverviewService`, `GetProjectLeadMonthEndStatusOverviewService`) are character-for-character identical except for the repository method called. The worklist services share the same pattern. Adding clarifications to both services without extracting a shared assembler would quadruple the duplication.

`MonthEndClarification` already exposes `canBeResolvedBy(UserId)` as a domain method, making `canResolve` straightforward to compute — parallel to how `canComplete` is computed from `task.eligibleActorIds().contains(actorId)`.

`MonthEndTaskSnapshotLookup` lives in the application package but has no application-layer imports; it is a pure data carrier populated by `ResolveMonthEndTaskSnapshotLookupService`.

## Goals / Non-Goals

**Goals:**
- Include all clarifications (OPEN and DONE) related to the overview actor's scope in `MonthEndStatusOverview`.
- Expose `canResolve` on each clarification item using the existing domain method.
- Extract a shared `AssembleMonthEndStatusOverviewService` to eliminate duplication before it compounds.
- Keep `MonthEndWorklistClarificationItem` and its open-only semantics unchanged.

**Non-Goals:**
- Task-scoping clarifications (clarifications remain project+employee scoped).
- Changing task generation, completion, or any other month-end use case.
- Changing the worklist or its clarification semantics.
- Exposing `eligibleProjectLeadIds` from the clarification aggregate in the overview item.

## Decisions

### D1: Application-layer assembler, not a domain service

**Decision:** `AssembleMonthEndStatusOverviewService` lives in the application package alongside `ResolveMonthEndTaskSnapshotLookupService`.

**Rationale:** The assembly is primarily structural read-model composition, not a business invariant or domain operation spanning aggregates in the DDD sense. `MonthEndTaskSnapshotLookup` also lives in the application package; co-locating the assembler avoids moving that class just to enable a domain placement. The one domain rule involved (`canComplete`, `canResolve`) is trivially delegated to existing domain methods. This is consistent with `ResolveMonthEndTaskSnapshotLookupService` as the existing precedent for shared application-layer helpers.

**Alternative considered:** Domain service with `MonthEndTaskSnapshotLookup` moved to the domain package — rejected because the snapshot lookup is infrastructure plumbing (a cache of resolved port results), not a domain concept; moving it would blur the domain boundary without adding expressiveness.

---

### D2: All clarification statuses in the overview (not open-only)

**Decision:** `findAllEmployeeClarifications` and `findAllProjectLeadClarifications` return both `OPEN` and `DONE` clarifications.

**Rationale:** The overview is status-oriented — it exists precisely to show the full picture, including resolved context. A DONE clarification is meaningful: it shows a follow-up question that was raised and resolved, which is relevant to understanding why a task's state looks the way it does. The open-only filter is appropriate for the worklist (actionable focus) but wrong for the overview.

**Alternative considered:** Open-only (reuse existing `findOpen*` queries) — rejected because DONE clarifications provide context that consumers of a status overview need.

---

### D3: New `MonthEndOverviewClarificationItem` read model with resolved user references

**Decision:** Introduce `MonthEndOverviewClarificationItem` with `UserRef` values for `subjectEmployee`, `createdBy`, and nullable `resolvedBy`, alongside full resolution fields (`resolutionNote`, `resolvedAt`) and `canResolve`.

**Rationale:** `MonthEndWorklistClarificationItem` lacks resolution fields and `canResolve` — it was designed for actionable, open-only display. The overview needs resolution fields for DONE clarifications, `canResolve` for open ones, and nested user display data so API consumers do not need follow-up lookups for clarification subject, creator, or resolver labels. Rather than retrofitting the worklist item (breaking its semantics), a dedicated read model keeps each item type aligned with its consumer's needs.

**Alternative considered:** Embed `MonthEndClarification` directly — rejected because it exposes `eligibleProjectLeadIds` (a domain internal used for authorization checks) to read-model consumers, and conflates the aggregate with a view type.

---

### D4: `canResolve` is computed by the assembler using the existing domain method

**Decision:** `canResolve = clarification.canBeResolvedBy(actorId)` — called in the assembler during item construction.

**Rationale:** `canBeResolvedBy` already encodes the correct rule: employee-created clarifications are resolvable by eligible project leads; lead-created clarifications are resolvable by the subject employee. Recomputing the rule in the assembler would duplicate domain logic. DONE clarifications naturally return `false` via `canBeResolvedBy` since resolution is idempotent.

**Alternative considered:** A separate `canResolve` field on `MonthEndClarification` — rejected as redundant; the method already exists and is tested.

## Risks / Trade-offs

- **New repository queries are not filtered by status** — returning all records (OPEN + DONE) may be a larger result set than the open-only queries. For typical month-end volumes this is not a concern; if clarification volume grows significantly, pagination would need to be considered at that time.
- **Overview clarification scope is actor-based, not task-based** — clarifications are matched to the actor's scope (subject employee for employee overview, led projects for lead overview) rather than strictly to the fetched task list. This is consistent with how tasks are queried (same scope) and avoids a second join. Any clarification in the actor's scope for the month appears in the overview even if its corresponding task happens to be absent (e.g., not yet generated). This is acceptable — clarifications carry their own project+employee context.

## Context

The month-end domain already distinguishes between the write model for `MonthEndTask` and read-side worklist queries over open tasks. That split turned out to be useful: `MonthEndWorklist` is an execution-oriented view of what a person still has to do, while the planned matrix/dashboard is a broader status-oriented overview.

The current query side exposes two actor-specific worklists:
- employee-owned open tasks for a user and month
- lead-eligible open tasks for a user and month

That is sufficient for the focused "what is still open?" experience, but it is not sufficient for the month-end dashboard. The dashboard needs to show both `OPEN` and `DONE` tasks, and for shared lead tasks it needs to preserve visibility after completion rather than letting them disappear the way they should in a worklist.

## Goals / Non-Goals

**Goals:**
- Add a dedicated month-end status-overview use case for the matrix/dashboard view.
- Preserve the current open-only worklist semantics and keep them separate from all-status overview concerns.
- Expose both `OPEN` and `DONE` month-end tasks relevant to the requesting actor and month.
- Provide the business context and status details needed to render a status-oriented dashboard over month-end tasks.
- Reuse the existing month-end task and completion model without introducing a new aggregate or persistence structure.
- Limit implementation scope to the use case, its read model, and the supporting query/service logic.

**Non-Goals:**
- Replacing `MonthEndWorklist` or redefining it to include completed tasks.
- Designing the exact frontend layout, filters, or non-month-end columns of the dashboard.
- Adding REST resources, controllers, or other inbound delivery adapters.
- Changing task generation or task completion rules.
- Introducing a new write model or stored dashboard projection.

## Decisions

### 1. Introduce a dedicated `MonthEndStatusOverview` query use case

The backend will expose a separate read-side use case for the month-end status overview. It represents the month-end tasks relevant to an actor and month for dashboard rendering, including both actionable and already completed obligations.

This keeps the language crisp:
- `worklist` remains the actor-specific open-task query concept already modeled in the domain.
- `status overview` becomes the broader monitoring and coordination view used by the matrix/dashboard.

Alternative considered:
- Broaden the existing worklist into the dashboard query.
- Rejected because that would collapse two distinct user jobs, execution and monitoring, into one contract and weaken the current open-only worklist semantics.

### 1a. Limit this change to the use-case and service boundary

This change will stop at the use-case boundary. It should define the overview query contract, the read model, and the service logic needed to assemble the overview from `MonthEndTask` data, but it should not introduce any HTTP, REST, or controller layer yet.

Alternative considered:
- Expose the overview immediately through a REST controller.
- Rejected because the current goal is to settle the domain/application query contract first and avoid coupling the change to a delivery adapter.

### 2. Assemble the overview from all relevant month-end tasks, not only open ones

The overview application service will query the same `MonthEndTask` source of truth but include both `OPEN` and `DONE` tasks that are relevant to the requesting actor and month. For lead-eligible tasks, relevance still comes from the eligible actor set even after completion.

This keeps query orchestration in the application layer, where it belongs, and avoids asking a future UI to infer missing completed state from an open-only API.

Alternative considered:
- Change the existing open-task queries to include completed tasks.
- Rejected because the worklist contract would lose its focused "still to do" meaning.

### 3. Expose status and completer information in the overview model

The overview read model will expose the task identity, task type, task status, project reference, subject employee reference when present, and completing actor when the task is done.

Alternative considered:
- Return only a binary visual state and omit who completed a shared task.
- Rejected because the dashboard would lose important context for lead-eligible tasks that remain visible after another eligible lead completed them.

### 4. Keep the overview matrix-ready but UI-neutral

The overview contract will provide status entries and business context that the UI can group into a matrix or dashboard, but it will not hard-code specific layout concerns such as exact column ordering, visual grouping widgets, or non-month-end fields.

This allows the domain/application contract to stay stable while still giving a later frontend enough data to render employee/project rows and task-type status cells.

Alternative considered:
- Shape the backend response as a literal copy of one specific screen design.
- Rejected because the overview should support the matrix/dashboard use case without freezing one presentation as the domain contract.

### 5. Avoid write-side and persistence changes

This change is read-side only. The existing `MonthEndTask` aggregate, completion flow, and task persistence remain the source of truth.

Alternative considered:
- Introduce a separate overview aggregate or stored dashboard projection.
- Rejected because the overview does not protect new invariants and can be derived from the current task model.

## Risks / Trade-offs

- [Overview queries are broader than worklist queries] -> Mitigation: keep overview queries read-side only and scoped by actor and month.
- [Shared lead tasks can be confusing without completer context] -> Mitigation: include `completedBy` in overview entries for done tasks.
- [Later delivery adapters may want different grouping or ordering] -> Mitigation: keep the use-case response matrix-ready but presentation-neutral.

## Migration Plan

- Add a new `monthend-status-overview` capability spec.
- Implement a new query use case and read model for the month-end status overview.
- Reuse the existing `MonthEndTask` model and existing completion use case.
- Add unit and integration tests for mixed `OPEN` and `DONE` overview scenarios while keeping the worklist open-only.
- Defer any REST/controller work to a later change.

No data migration or persistence rollout is required because this change does not alter the write model.

## Open Questions

- None at proposal time. If the future UI needs additional presentation-only grouping or filtering, that can be captured without changing the core overview semantics.

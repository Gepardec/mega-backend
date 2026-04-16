## Why

The `MonthEndStatusOverview` currently surfaces tasks only, omitting clarifications that are directly related to those tasks. Consumers need the full picture — including whether they can resolve each clarification (`canResolve`) — to act without switching to a separate endpoint. Additionally, the two overview services are character-for-character identical except for the repository method called; adding clarifications would quadruple this duplication, so we extract a shared assembler first.

## What Changes

- Add `MonthEndOverviewClarificationItem` domain read model — full clarification fields including resolution info (`resolutionNote`, `resolvedBy`, `resolvedAt`) and a computed `canResolve` boolean. Distinct from `MonthEndWorklistClarificationItem` which remains open-only with no resolution fields.
- Extend `MonthEndStatusOverview` with `List<MonthEndOverviewClarificationItem> clarifications` — all clarifications (OPEN and DONE) related to the fetched tasks' project+employee scope.
- Add `findAllEmployeeClarifications` and `findAllProjectLeadClarifications` to `MonthEndClarificationRepository` — same scope as the existing `findOpen*` queries but without the OPEN status filter.
- Add `AssembleMonthEndStatusOverviewService` (application layer) — shared assembler that takes pre-fetched tasks, clarifications, and a resolved snapshot lookup and produces a `MonthEndStatusOverview`. Computes `canComplete` and `canResolve` using existing domain methods on `MonthEndTask` and `MonthEndClarification`.
- Simplify `GetEmployeeMonthEndStatusOverviewService` and `GetProjectLeadMonthEndStatusOverviewService` to fetch → delegate → return.

## Capabilities

### New Capabilities

*(none)*

### Modified Capabilities

- `monthend-status-overview`: Extend spec to require a clarifications list on the overview, `canResolve` semantics, and all clarification statuses (not just open).
- `monthend-clarifications`: Extend spec to document the two new repository query methods.
- `monthend-rest-api`: Extend the employee and project-lead overview endpoint scenarios to include clarifications in the response with `canResolve` and resolution fields.

## Impact

- `MonthEndClarificationRepository` port: two new methods
- `MonthEndClarificationRepositoryAdapter`: two new query implementations
- New `MonthEndOverviewClarificationItem` domain model
- `MonthEndStatusOverview` record: new `clarifications` field
- New `AssembleMonthEndStatusOverviewService` application service
- `GetEmployeeMonthEndStatusOverviewService`: simplified
- `GetProjectLeadMonthEndStatusOverviewService`: simplified
- OpenAPI spec: overview response models updated to include clarification list with `canResolve` and resolution fields
- Tests: assembler unit test; updated overview service tests; new adapter tests for both new queries

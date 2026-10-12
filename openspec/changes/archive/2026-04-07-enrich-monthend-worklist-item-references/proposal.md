## Why

`MonthEndWorklistItem` exposes raw `ProjectId` and `UserId` fields, which are meaningless to users without additional lookups. `MonthEndStatusOverviewItem` already solved this by embedding named projections, so the worklist should follow the same pattern for consistency and usability.

## What Changes

- Rename `MonthEndStatusOverviewProject` → `MonthEndProject` (shared, reusable projection for project id + name)
- Rename `MonthEndStatusOverviewSubjectEmployee` → `MonthEndEmployee` (shared, reusable projection for user id + full name)
- Update `MonthEndStatusOverviewItem` to reference the renamed types
- Update `MonthEndWorklistItem` to replace `ProjectId projectId` with `MonthEndProject project` and `UserId subjectEmployeeId` with `MonthEndEmployee subjectEmployee`
- Update the OpenAPI schema `MonthEndWorklistTask` to replace `projectId: UUID` and `subjectEmployeeId: UUID` with `project: MonthEndProjectReference` and `subjectEmployee: MonthEndSubjectEmployeeReference` (reusing schemas that already exist)
- Update all usages (mappers, factories, tests) accordingly

## Capabilities

### New Capabilities

_(none — no new spec-level capability is introduced)_

### Modified Capabilities

- `monthend-task-worklist`: The worklist item now exposes enriched project and employee references instead of raw IDs.
- `monthend-rest-api`: The `MonthEndWorklistTask` REST response shape changes from raw UUID fields to nested project and subject employee objects.

## Impact

- Domain model: `MonthEndStatusOverviewProject`, `MonthEndStatusOverviewSubjectEmployee`, `MonthEndStatusOverviewItem`, `MonthEndWorklistItem`
- OpenAPI spec: `monthend.openapi.yaml` — `MonthEndWorklistTask` schema
- Mappers that produce these types
- Any constructors / factory methods / tests that instantiate `MonthEndWorklistItem` or assert on `MonthEndWorklistTask` response fields

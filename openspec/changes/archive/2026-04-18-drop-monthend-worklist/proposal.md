## Why

The month-end worklist feature was built at an early stage of the hexagonal backend, before the status overview existed. The frontend has never consumed it and there are no plans to add worklist-based views. The status overview already covers all relevant data. Retaining the worklist adds dead code, misleading specs, and unnecessary complexity.

## What Changes

- **BREAKING** Remove `GET /monthend/employee/worklist` endpoint
- **BREAKING** Remove `GET /monthend/project-lead/worklist` endpoint
- Delete `MonthEndWorklist`, `MonthEndWorklistItem`, `MonthEndWorklistClarificationItem` domain models
- Delete `GetEmployeeMonthEndWorklistUseCase` and `GetProjectLeadMonthEndWorklistUseCase` inbound ports
- Delete `GetEmployeeMonthEndWorklistService` and `GetProjectLeadMonthEndWorklistService` application services
- Delete `MonthEndWorklistServicesTest`
- Remove worklist mapper methods from `MonthEndRestMapper`
- Remove worklist OpenAPI paths and schemas (`MonthEndWorklistTask`, `MonthEndWorklistClarification`, `MonthEndWorklistResponse`)
- Delete `openspec/specs/monthend-task-worklist/spec.md`
- Update related specs to remove worklist references (no requirement changes, only cleanup)

## Capabilities

### New Capabilities

_(none — this is a pure deletion change)_

### Modified Capabilities

- `monthend-rest-api`: Remove worklist endpoint requirements and scenarios
- `monthend-clarifications`: Remove note that open-only clarification query methods are retained for worklist use
- `monthend-status-overview`: Remove reference to "not changing open-only worklist semantics"
- `shared-user-project-refs`: Remove `MonthEndWorklistItem` references from scenarios

## Impact

- **API**: Two endpoints removed — any client calling the worklist endpoints will receive 404
- **Code**: ~7 classes deleted, worklist methods removed from REST resources and mapper
- **Specs**: `monthend-task-worklist` spec deleted; four related specs trimmed
- **Tests**: `MonthEndWorklistServicesTest` deleted

## Why

The latest month-end query changes introduced the same project and subject-employee snapshot resolution logic in three application services. Keeping that logic duplicated makes future changes to lookup rules, null-handling, or failure behavior easy to miss and harder to test consistently.

## What Changes

- Extract the shared month-end task snapshot loading and validation logic from `GetMonthEndStatusOverviewService`, `GetEmployeeMonthEndWorklistService`, and `GetProjectLeadMonthEndWorklistService` into a dedicated application-layer collaborator.
- Centralize batch loading of project snapshots and nullable subject-employee snapshots for a task list, including the existing "missing snapshot" failure behavior.
- Update the three query services to delegate snapshot resolution to the shared collaborator before mapping tasks into overview or worklist items.
- Add focused unit tests for the extracted resolver and adjust existing service tests to verify delegation-oriented behavior without changing externally visible results.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
None.

## Impact

- `GetMonthEndStatusOverviewService`, `GetEmployeeMonthEndWorklistService`, and `GetProjectLeadMonthEndWorklistService`
- New shared application service/value object(s) for resolved month-end task snapshot context
- Existing month-end snapshot outbound ports: `MonthEndProjectSnapshotPort`, `MonthEndUserSnapshotPort`
- Unit tests around month-end query services and the extracted resolver

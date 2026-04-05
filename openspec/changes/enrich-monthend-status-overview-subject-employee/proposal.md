## Why

The month-end status overview already exposes project display data through a nested object, but it still exposes the reviewed or assigned employee only as a raw `subjectEmployeeId`. That is not enough for the UI to render a usable employee label, so the overview should expose the subject employee in the same structured way as the project.

## What Changes

- Enrich month-end status overview entries with subject employee display data so callers receive the employee's full name together with the identifier.
- **BREAKING** Replace the flat `subjectEmployeeId` overview field with a nullable nested subject employee reference object that contains the employee id and full name.
- Resolve subject employee display data explicitly in bulk for overview queries instead of leaving name lookup to clients.
- Update the shared month-end REST contract and generated response models so overview entries expose the nested subject employee object alongside the existing nested project object.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `monthend-status-overview`: overview entries now expose a nullable nested subject employee reference object with id and full name instead of a flat `subjectEmployeeId`.
- `monthend-rest-api`: the shared status overview response now exposes the nested subject employee reference object returned by the application layer.

## Impact

- Affects the month-end application query path for `GetMonthEndStatusOverviewUseCase`.
- Affects the month-end user snapshot port, its adapter and mapping, and the overview read model mapping.
- Affects the canonical month-end OpenAPI document and generated shared status overview response models.
- Requires test updates for application, REST mapping, and endpoint coverage around enriched overview entries.

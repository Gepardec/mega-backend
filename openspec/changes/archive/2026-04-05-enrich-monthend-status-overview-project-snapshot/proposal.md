## Why

The month-end status overview currently returns only task metadata and a project identifier, which is not enough for the UI to render a usable project label. We need the overview to expose project snapshot data, starting with the project name, without pushing JPA relationship navigation into the month-end aggregate persistence model.

## What Changes

- Enrich month-end status overview entries with project snapshot data needed for UI rendering, starting with the project name.
- Resolve project snapshot data explicitly in bulk for overview queries instead of loading projects one-by-one after reading tasks.
- Keep `MonthEndTask` and `MonthEndClarification` persistence modeled by `ProjectId` references rather than introducing JPA object relationships for this feature.
- Update the shared month-end REST contract and generated response models so overview entries expose the added project snapshot fields.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `monthend-status-overview`: overview entries now include project snapshot data for matrix and dashboard rendering, not just a project reference.
- `monthend-rest-api`: the shared status overview response now exposes the additional project snapshot fields returned by the application layer.

## Impact

- Affects the month-end application query path for `GetMonthEndStatusOverviewUseCase`.
- Affects the month-end project snapshot port, its adapter, and the overview read model mapping.
- Affects the canonical month-end OpenAPI document and generated shared status overview response models.
- Requires test updates for application and REST mapping/endpoint coverage around enriched overview entries.

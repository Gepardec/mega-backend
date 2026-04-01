## Why

The planned month-end matrix/dashboard needs a broad status-oriented read model that shows both open and completed obligations in one place. The existing `MonthEndWorklist` already serves a different job well, a focused open-only list of actionable tasks, so the overview should be modeled as a separate capability instead of redefining the worklist.

## What Changes

- Add a new `monthend-status-overview` use case for the month-end matrix/dashboard view.
- Keep the existing actor-specific worklists open-only and focused on actionable month-end tasks.
- Define how the status overview exposes both open and completed month-end tasks relevant to the requesting actor and month.
- Define the business context and status details the overview use case must expose so a later UI can render the month-end dashboard without turning the overview into a write model.

## Capabilities

### New Capabilities
- `monthend-status-overview`: Defines a status-oriented month-end overview that presents open and completed month-end obligations for the matrix/dashboard view.

### Modified Capabilities
None.

## Impact

- Affects the `com.gepardec.mega.hexagon.monthend` query side, especially the overview use case, read model, repository contract, and service implementation.
- Preserves the existing `monthend-task-worklist` behavior and avoids broadening it into an all-status dashboard query.
- Introduces a dedicated overview contract that can coexist with the focused worklist experience.
- Does not include REST resources, controllers, or other inbound delivery adapters in this change.

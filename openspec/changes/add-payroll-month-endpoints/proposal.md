## Why

When the frontend loads, it needs to know which payroll month to display before fetching any data — this month is not always the current calendar month. The legacy backend has a `PayrollMonthProvider` concept for this, but it lives in the REST layer and has never been migrated to the hexagon. Without it, the hexagon's monthend and worktime endpoints cannot be used as the primary data source on initial page load.

## What Changes

- Add `GET /monthend/payroll-month/employee` endpoint — resolves the active payroll month for the authenticated employee based on their open monthend tasks
- Add `GET /monthend/payroll-month/project-lead` endpoint — resolves the active payroll month for the authenticated project lead (always previous month)
- Add `GetEmployeePayrollMonthUseCase` and `GetProjectLeadPayrollMonthUseCase` to the `monthend` application layer
- Add corresponding service implementations in the `monthend` application layer

## Capabilities

### New Capabilities
- `payroll-month`: Rules for resolving the active payroll month per actor role, and the REST endpoints that expose it

### Modified Capabilities
- `monthend-rest-api`: Two new endpoints added to the monthend REST surface

## Impact

- New endpoints in `com.gepardec.mega.hexagon.monthend`
- New use cases and services in `monthend.application` and `monthend.application.port.inbound`
- `MonthEndTaskRepository` (existing outbound port) used as-is — no new ports needed
- No changes to the legacy backend
- Frontend can replace the legacy `GET /worker/payrollMonth` call with `GET /monthend/payroll-month/employee`, and use the new project-lead endpoint as a new capability

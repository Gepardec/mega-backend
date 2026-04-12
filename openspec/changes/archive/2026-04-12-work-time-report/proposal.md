## Why

The frontend needs to display work time breakdowns per payroll month — both from an employee's perspective (time spread across their projects) and a project lead's perspective (time contributed by each employee across all the lead's projects). This data is currently unavailable through the new hexagonal API and must be fetched in a way that lets the frontend flexibly pivot into either view without requiring two separate response shapes.

## What Changes

- Introduce a new `worktime` bounded context in `com.gepardec.mega.hexagon.worktime`
- Add two REST endpoints under `/worktime` that both return a unified flat response model:
  - `GET /worktime/employee/{payrollMonth}` — the authenticated employee's work time across all their projects
  - `GET /worktime/projects/{payrollMonth}` — work time across ALL projects the authenticated project lead leads, covering all employees on those projects
- The flat model contains one entry per (employee × project) attendance aggregate for the requested month; each entry also carries the employee's total monthly hours so the client can compute definition-B percentages (`hours on project X / employee's total hours`)
- Billability is determined per attendance record (ZEP `billable` flag), not per project flag
- Project scope for the lead endpoint is determined by `Project.leads` (all projects where the caller's `UserId` is in the leads set); no monthend eligibility dependency
- The project use case fetches employee data in a three-phase parallel approach: resolve lead's projects from DB, discover employees per project from ZEP, then bulk-fetch all unique employees' complete attendance data in a single parallel round — each attendance record fetched exactly once

## Capabilities

### New Capabilities

- `worktime-report`: Fetch aggregated work time per payroll month as a flat (employee × project) list, queryable from an employee scope or a project-lead scope (all lead's projects)
- `worktime-rest-api`: REST API contract conventions for the worktime bounded context — spec-first OpenAPI, auth-from-context, and role-based access rules shared across all current and future worktime endpoints

### Modified Capabilities

- `monthend-rest-api`: Clarify that work time data is NOT part of the monthend status overview response; it is exposed via dedicated `/worktime` endpoints that the client calls separately

## Impact

- New package tree: `com.gepardec.mega.hexagon.worktime` (domain, application, adapter layers)
- New inbound REST adapter with OpenAPI-generated interface
- New outbound ZEP port with methods for employee attendance queries and project membership discovery
- Project scope resolution depends on `ProjectRepository` (already available via the `project` bounded context)
- No changes to existing monthend endpoints or domain

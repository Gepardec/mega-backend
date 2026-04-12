## Context

The frontend needs work time data per payroll month in two views: an employee's time spread across their projects, and a project lead's overview of time contributed by each employee across all the lead's projects. Neither view is currently available through the hexagonal API.

The existing legacy `ProjectManagementResourceImpl` computes similar data but is tightly coupled to the legacy step-entry workflow. The new implementation lives in a clean `worktime` bounded context in `com.gepardec.mega.hexagon.worktime`, following the same hexagonal patterns already established by the `monthend` and `project` contexts.

ZEP is the source of truth for attendance data. Billability is per attendance record (ZEP `billable` flag). The percentage of hours spent on a project uses the legacy semantics: `(employee hours on project X) / (employee total hours across ALL projects that month)` — this requires knowing each employee's full monthly picture, not just their hours on the lead's projects.

## Goals / Non-Goals

**Goals:**
- Two REST endpoints (`/worktime/employee/{month}` and `/worktime/projects/{month}`) returning the same flat `WorkTimeReport` response
- A flat (employee × project) entry list the frontend can pivot, group, and aggregate freely
- Each entry carries `employeeMonthTotalHours` so the client can compute definition-B percentages without a second request
- Per-booking billability from the ZEP attendance `billable` flag
- Project scope for the lead endpoint derived from `Project.leads` (all projects where caller is a lead)
- Spec-first OpenAPI contract matching the `monthend` pattern

**Non-Goals:**
- Server-side totals, percentages, or pre-grouped views (computed client-side)
- Caching of ZEP attendance data
- Historical or multi-month queries
- Any changes to the monthend domain or endpoints

## Decisions

### Decision: Flat (employee × project) response model with employeeMonthTotalHours

**Chosen**: Return a list of `{ employee, project, billableHours, nonBillableHours, employeeMonthTotalHours }` entries with no aggregated totals or percentages.

**Rationale**: Both frontend views require the same underlying data matrix. `employeeMonthTotalHours` is the employee's total across ALL their projects for the month — not just the lead's projects. This is the correct denominator for the legacy percentage semantics (`hours on project X / employee total hours`) and cannot be derived from the response entries alone in the project view (since entries only cover the lead's projects, not every project the employee works on). Including it as a field per entry makes it explicit and avoids a second client request.

**Alternative considered**: Exclude `employeeMonthTotalHours`, force client to call the employee endpoint for each employee to compute percentages. Rejected — cross-endpoint orchestration in the client is poor UX and would require elevated access the client may not have.

### Decision: Project lead endpoint covers all lead's projects, not a single project

**Chosen**: `GET /worktime/projects/{payrollMonth}` returns entries for every project where the caller is in `Project.leads`, covering all employees on all those projects.

**Rationale**: The monthend overview already gives project leads an all-projects picture. A per-project work time endpoint would force the client to make N calls (one per project) and manually aggregate, defeating the purpose of the flat model. Returning all projects in one call aligns with the overview pattern, enables client-side pivoting between projects, and naturally enables the efficient three-phase ZEP fetch strategy.

**Alternative considered**: `GET /worktime/projects/{projectId}/{payrollMonth}` for single-project queries. Rejected — misaligns with the overview scope, requires N client calls for the full picture, and cannot amortize ZEP call overhead across projects.

### Decision: Three-phase parallel ZEP fetch for the project use case

**Chosen**:
1. **DB** — `ProjectRepository.findAllByLead(callerId)` → all projects where caller is a lead (with ZEP project IDs)
2. **ZEP Phase 1** (parallel) — `fetchProjectMembership(zepProjectId, month)` for each project → union of ZEP employee IDs across all lead's projects
3. **ZEP Phase 2** (parallel) — `fetchAttendancesForEmployee(zepEmployeeId, month)` for each unique employee → complete attendance data for the month

**Why this is optimal**: Each attendance record is fetched exactly once. `employeeMonthTotalHours` is derived from Phase 2 data (sum of all an employee's durations regardless of project). Project-specific hours are derived by filtering Phase 2 data to the lead's project ZEP IDs. The sequential dependency is reduced to: DB → Phase 1 → Phase 2 (three rounds, but Phases 1 and 2 are each fully parallel within themselves).

**Alternative considered** (Option A): `project_id` query to discover employees + `employee_id` queries per employee. Rejected — project attendance data would be fetched twice (once in the project query, once in each employee query), and the project query cannot start until after employee lists are resolved.

**Alternative considered**: Single `employee_id` query per use case invocation. Rejected for the project use case — there is no way to know which employees to query without first discovering project membership.

### Decision: Employee use case uses a single ZEP call

**Chosen**: `fetchAttendancesForEmployee(zepEmployeeId, month)` — one call returning all the employee's attendances for the month across all their projects. Group by ZEP project ID in Java, resolve project display names from `ProjectRepository`.

**Rationale**: The employee's ZEP ID and all their project memberships are resolved in a single call. No sequential dependency.

### Decision: Single application OpenAPI source tree bundled for generation

**Chosen**: Maintain one canonical application OpenAPI source tree rooted at `src/main/resources/openapi/openapi.yaml`. Split paths, schemas, parameters, and responses into referenced files, lint and bundle that source tree during the build, and generate Java API interfaces and HTTP models from the bundled artifact. Worktime contributes its endpoints and schemas to that shared contract instead of introducing a second top-level `worktime.openapi.yaml`.

**Rationale**: The service exposes one HTTP API surface and the current build already assumes a single OpenAPI generator input. Adding a separate `worktime.openapi.yaml` would either require multiple generator executions with extra package/output coordination or push worktime-generated types into a misleading monthend-specific package layout. A single canonical source tree keeps security schemes, common parameters, and shared responses defined once, while still allowing bounded-context-level source organization through `$ref`.

**Consequence**: Because the bundled artifact contains endpoints from multiple bounded contexts, generated transport types should live in an application-level generated package rather than a monthend-specific package. The worktime and monthend REST adapters remain in their own bounded contexts and simply implement the generated interfaces from that shared transport package.

**Alternative considered**: Add a dedicated `worktime.openapi.yaml` and wire a second OpenAPI generator execution in Maven. Rejected — technically possible, but it creates avoidable build friction in a single Quarkus app and duplicates contract-generation wiring that can be centralized once.

### Decision: Project scope from Project.leads, not monthend eligibility

**Chosen**: The project lead endpoint resolves scope by querying `ProjectRepository` for all projects where the caller's `UserId` appears in `Project.leads`.

**Rationale**: Work time is a factual record — a lead should see their teams' hours regardless of whether monthend tasks exist yet. Monthend eligibility is a process state that should not gate access to raw work time data. This keeps the `worktime` and `monthend` contexts cleanly independent.

**Alternative considered**: Derive project scope from monthend task eligibility. Rejected — couples `worktime` to `monthend` context; work time has independent value outside the monthend process.

## Risks / Trade-offs

- **ZEP call volume for the project use case**: N(projects) + M(unique employees) ZEP calls per request. For a lead with many projects and large teams this could be significant. → Acceptable for now; response-level caching can be added if needed.
- **Phase 2 employee data includes hours outside lead's projects**: the employee_id query returns all attendances for the month, not just on the lead's projects. This is intentional — it provides `employeeMonthTotalHours` — but it means more data is transferred than strictly needed for the entry list. → Acceptable trade-off for correct percentage semantics.
- **No pagination**: for large teams the response could be large. → Acceptable for monthly granularity; revisit if needed.

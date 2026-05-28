## Context

The monthend REST layer currently consists of four resource classes: `MonthEndEmployeeResource`, `MonthEndProjectLeadResource`, `MonthEndSharedResource`, and `MonthEndOpsResource`, each implementing a separate generated OpenAPI interface. The split originated from class-level role annotations (`@MegaRolesAllowed`) which force role separation into the class hierarchy.

Every user in the system holds the `EMPLOYEE` role; project leads hold both `EMPLOYEE` and `PROJECT_LEAD`. Because a project lead is simultaneously an employee, the frontend has two distinct pages — an employee page and a project-lead page — each requiring a different data set from the same person for the same month.

## Goals / Non-Goals

**Goals:**
- One `MonthEndResource` class, one `MonthEndApi` generated interface, one `MonthEnd` OpenAPI tag
- Two explicit overview paths — `/employee` and `/project-lead` suffixed — so actors with both roles can request either view independently
- Month encoded in path for overview endpoints (GET, no body) and `generate` (ops, month is the only operand)
- Eliminate duplicated `resolveProjectRefs`/`resolveUserRefs` helper methods
- Merged `CreateClarificationRequest` DTO (replaces both employee and project-lead variants)

**Non-Goals:**
- Changes to application services, domain model, or persistence layer
- Changes to endpoints that have no role-split (`complete`, `delete`, `update text`, `resolve`)
- Versioning or backwards-compatible URL aliasing

## Decisions

### Explicit overview paths per view; no role dispatch for overview

The status overview is split into two paths:

```
GET /monthend/{month}/status-overview/employee      @MegaRolesAllowed(EMPLOYEE)
GET /monthend/{month}/status-overview/project-lead  @MegaRolesAllowed(PROJECT_LEAD)
```

Each delegates unconditionally to its dedicated use case. A project lead can call both — the employee page calls the `/employee` path, the project-lead page calls the `/project-lead` path. No runtime role check inside the method body.

**Alternative considered:** Single `GET /monthend/{month}/status-overview` with server-side role dispatch (`if hasRole(PROJECT_LEAD)`). Rejected — a user holding both roles can never retrieve the employee view, since the project-lead branch always wins. A query parameter (`?view=employee/project-lead`) was also considered and rejected: the parameter would dispatch to fundamentally different use cases rather than filtering, which misrepresents query parameter semantics.

Role-specific access control (`@MegaRolesAllowed`) is method-level throughout `MonthEndResource`. All other methods (clarification create, preparations, shared actions) are unchanged by this decision.

### `@Authenticated` at class level; `@Tenant("mega-cron")` at method level for ops

The ops endpoint (`generateMonthEndTasks`) uses a different OIDC tenant. Quarkus `@Tenant` at method level is supported per the multitenancy guide. The risk is that class-level `@Authenticated` evaluates the cron token against the default tenant before tenant selection occurs.

Decision: attempt the merge first. If the cron token is rejected by the default tenant `@Authenticated` guard, extract `MonthEndOpsResource` back to its own class (trivial change).

### Month in path for GET and ops; in body for POST operations with complex payloads

`GET /monthend/{month}/status-overview` — month must be in the path or query since GET has no body; path is cleaner and cacheable.

`POST /monthend/{month}/generate` — month was the sole field in `GenerateMonthEndTasksRequest`; moving it to the path eliminates the request body entirely.

`POST /monthend/clarifications` and `POST /monthend/preparations` — these carry `projectId`, `text`, and other fields; month stays in the body alongside its companions to avoid a split between path and body state.

### Merged `CreateClarificationRequest` DTO

`CreateEmployeeClarificationRequest` (fields: `month`, `projectId`, `text`) and `CreateProjectLeadClarificationRequest` (fields: `month`, `projectId`, `text`, `subjectEmployeeId?`) are replaced by a single `CreateClarificationRequest` with optional `subjectEmployeeId`.

Dispatch logic:
- Actor holds `PROJECT_LEAD` + `subjectEmployeeId` present → use provided ID
- Actor holds `PROJECT_LEAD` + `subjectEmployeeId` absent → `null` (project-level clarification)
- Actor does not hold `PROJECT_LEAD` → `subjectEmployeeId = actorId` always (provided value ignored)

### Single `MonthEnd` OpenAPI tag

`MonthEndEmployee`, `MonthEndProjectLead`, `MonthEndShared`, `MonthEndOps` tags are replaced by `MonthEnd`. This collapses the four generated API interfaces into one `MonthEndApi`.

## Risks / Trade-offs

- **`@Authenticated` + `@Tenant` interaction** → If the default-tenant authentication guard fires before tenant resolution on the ops method, the cron token will be rejected. Mitigation: test early; extract ops back to its own class if needed (no domain changes required).
- **BREAKING path changes** → All monthend URLs change. Frontend and any other consumers must update. No parallel alias paths — the old structure is removed outright.

## Migration Plan

1. Update `openapi/paths/monthend.yaml` — new paths, single tag, merged DTO reference
2. Update `openapi/schemas/monthend.yaml` — replace two clarification request schemas with `CreateClarificationRequest`; drop `GenerateMonthEndTasksRequest`
3. Regenerate OpenAPI-generated sources
4. Implement `MonthEndResource`; delete the four old resource classes
5. Delete the four old generated interface references from the codebase
6. Update frontend API client URLs (separate frontend task)

Rollback: revert the OpenAPI YAML and resource class changes; the domain layer is untouched.

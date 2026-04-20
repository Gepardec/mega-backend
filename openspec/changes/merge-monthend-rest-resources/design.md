## Context

The monthend REST layer currently consists of four resource classes: `MonthEndEmployeeResource`, `MonthEndProjectLeadResource`, `MonthEndSharedResource`, and `MonthEndOpsResource`, each implementing a separate generated OpenAPI interface. The split originated from class-level role annotations (`@MegaRolesAllowed`) which force role separation into the class hierarchy.

Every user in the system holds the `EMPLOYEE` role; project leads hold both `EMPLOYEE` and `PROJECT_LEAD`. This means clients must know a user's role before constructing the URL to call — a leaky API design. The role information is already present in the JWT and available server-side.

## Goals / Non-Goals

**Goals:**
- One `MonthEndResource` class, one `MonthEndApi` generated interface, one `MonthEnd` OpenAPI tag
- Role-dispatch inside methods where employee vs. project-lead behaviour diverges
- Month encoded in path for `status-overview` (GET, no body) and `generate` (ops, month is the only operand)
- Eliminate duplicated `resolveProjectRefs`/`resolveUserRefs` helper methods
- Merged `CreateClarificationRequest` DTO (replaces both employee and project-lead variants)

**Non-Goals:**
- Changes to application services, domain model, or persistence layer
- Changes to endpoints that have no role-split (`complete`, `delete`, `update text`, `resolve`)
- Versioning or backwards-compatible URL aliasing

## Decisions

### Role dispatch via `AuthenticatedActorContext` at method level

`MonthEndResource` receives class-level `@Authenticated`. Methods that diverge per role check `authenticatedActorContext.hasRole(Role.PROJECT_LEAD)` and delegate to the appropriate use case:

```java
// GET /monthend/{month}/status-overview
if (authenticatedActorContext.hasRole(Role.PROJECT_LEAD)) {
    overview = getProjectLeadOverviewUseCase.getOverview(actorId, month);
} else {
    overview = getEmployeeOverviewUseCase.getOverview(actorId, month);
}
```

Role-specific access control (`@MegaRolesAllowed`) moves from class-level to method-level. Previously employee-only methods keep `@MegaRolesAllowed(Role.EMPLOYEE)` (which all users satisfy); previously project-lead-only methods get `@MegaRolesAllowed(Role.PROJECT_LEAD)`.

**Alternative considered:** Keep separate resource classes but extract shared helpers via a base class or utility bean. Rejected — it solves duplication but leaves client friction and the multi-interface proliferation in place.

### `@Authenticated` at class level; `@Tenant("mega-cron")` at method level for ops

The ops endpoint (`generateMonthEndTasks`) uses a different OIDC tenant. Quarkus `@Tenant` at method level is supported per the multitenancy guide. The risk is that class-level `@Authenticated` evaluates the cron token against the default tenant before tenant selection occurs.

Decision: attempt the merge first. If the cron token is rejected by the default tenant `@Authenticated` guard, extract `MonthEndOpsResource` back to its own class (trivial change).

### Month in path for GET and ops; in body for POST operations with complex payloads

`GET /monthend/{month}/status-overview` — month must be in the path or query since GET has no body; path is cleaner and cacheable.

`POST /monthend/{month}/generate` — month was the sole field in `GenerateMonthEndTasksRequest`; moving it to the path eliminates the request body entirely.

`POST /monthend/clarifications` and `POST /monthend/preparations` — these carry `projectId`, `text`, and other fields; month stays in the body alongside its companions to avoid a split between path and body state.

### Merged `CreateClarificationRequest` DTO

`CreateEmployeeClarificationRequest` (fields: `month`, `projectId`, `text`) and `CreateProjectLeadClarificationRequest` (fields: `month`, `projectId`, `text`, `subjectEmployeeId?`) are replaced by a single `CreateClarificationRequest` with optional `subjectEmployeeId`.

Dispatch logic: if the actor holds `PROJECT_LEAD` and `subjectEmployeeId` is present, use it; otherwise `subjectEmployeeId = actorId`.

### Single `MonthEnd` OpenAPI tag

`MonthEndEmployee`, `MonthEndProjectLead`, `MonthEndShared`, `MonthEndOps` tags are replaced by `MonthEnd`. This collapses the four generated API interfaces into one `MonthEndApi`.

## Risks / Trade-offs

- **`@Authenticated` + `@Tenant` interaction** → If the default-tenant authentication guard fires before tenant resolution on the ops method, the cron token will be rejected. Mitigation: test early; extract ops back to its own class if needed (no domain changes required).
- **BREAKING path changes** → All monthend URLs change. Frontend and any other consumers must update. No parallel alias paths — the old structure is removed outright.
- **Role dispatch branching in resource methods** → Adds an `if/else` branch to two methods. Acceptable given the trivial nature of the check and the reduction in overall class count.

## Migration Plan

1. Update `openapi/paths/monthend.yaml` — new paths, single tag, merged DTO reference
2. Update `openapi/schemas/monthend.yaml` — replace two clarification request schemas with `CreateClarificationRequest`; drop `GenerateMonthEndTasksRequest`
3. Regenerate OpenAPI-generated sources
4. Implement `MonthEndResource`; delete the four old resource classes
5. Delete the four old generated interface references from the codebase
6. Update frontend API client URLs (separate frontend task)

Rollback: revert the OpenAPI YAML and resource class changes; the domain layer is untouched.

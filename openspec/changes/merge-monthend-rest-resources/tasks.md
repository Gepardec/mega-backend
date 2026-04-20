## 1. OpenAPI Contract

- [x] 1.1 Update `openapi/paths/monthend.yaml`: replace `/monthend/employee/status-overview` and `/monthend/project-lead/status-overview` with `GET /monthend/{month}/status-overview`; add `{month}` path parameter
- [x] 1.2 Update `openapi/paths/monthend.yaml`: replace `/monthend/employee/clarifications` and `/monthend/project-lead/clarifications` with a single `POST /monthend/clarifications`
- [x] 1.3 Update `openapi/paths/monthend.yaml`: rename `/monthend/ops/generation` to `POST /monthend/{month}/generate`; drop the request body
- [x] 1.4 Consolidate all tags to `MonthEnd` across all path entries in `monthend.yaml`
- [x] 1.5 Update `openapi/schemas/monthend.yaml`: replace `CreateEmployeeClarificationRequest` and `CreateProjectLeadClarificationRequest` with a single `CreateClarificationRequest` (fields: `month`, `projectId`, `text`, optional `subjectEmployeeId`)
- [x] 1.6 Remove `GenerateMonthEndTasksRequest` schema from `openapi/schemas/monthend.yaml`

## 2. Code Generation

- [x] 2.1 Run the OpenAPI generator (`mvn generate-sources` or equivalent) and verify a single `MonthEndApi` interface is generated alongside the updated model classes

## 3. Resource Implementation

- [x] 3.1 Create `MonthEndResource` implementing `MonthEndApi`, annotated `@RequestScoped @Authenticated`; inject all use cases and ports previously spread across the four resource classes
- [x] 3.2 Implement `getMonthEndStatusOverview(@PathParam month)`: role-dispatch via `authenticatedActorContext.hasRole(Role.PROJECT_LEAD)` to the appropriate use case; consolidate the `resolveProjectRefs` / `resolveUserRefs` helpers as private methods
- [x] 3.3 Implement `createMonthEndClarification(CreateClarificationRequest)`: set `subjectEmployeeId = actorId` unless actor holds `PROJECT_LEAD` and request supplies a non-null `subjectEmployeeId`; annotate `@MegaRolesAllowed(Role.EMPLOYEE)`
- [x] 3.4 Implement `prepareMonthEndProject(PrepareMonthEndProjectRequest)`: delegate to `PrematureMonthEndPreparationUseCase`; annotate `@MegaRolesAllowed(Role.EMPLOYEE)`
- [x] 3.5 Migrate the four shared methods (`completeMonthEndTask`, `resolveMonthEndClarification`, `deleteMonthEndClarification`, `updateMonthEndClarificationText`) from `MonthEndSharedResource` unchanged
- [x] 3.6 Implement `generateMonthEndTasks(@PathParam month)`: annotate `@Tenant("mega-cron") @RolesAllowed("mega-cron:sync")`; no request body

## 4. Cleanup

- [x] 4.1 Delete `MonthEndEmployeeResource`, `MonthEndProjectLeadResource`, `MonthEndSharedResource`, `MonthEndOpsResource`
- [x] 4.2 Verify no remaining references to the deleted classes or old generated interfaces (`MonthEndEmployeeApi`, `MonthEndProjectLeadApi`, `MonthEndSharedApi`, `MonthEndOpsApi`)

## 5. Tests

- [x] 5.1 Create `MonthEndResourceTest` covering: employee gets employee overview, project-lead gets project-lead overview, clarification creation with and without `subjectEmployeeId`, ops generation endpoint
- [x] 5.2 Delete or migrate tests from `MonthEndEmployeeAndProjectLeadResourceTest`, `MonthEndSharedResourceTest`, `MonthEndOpsResourceTest`
- [x] 5.3 Run the full test suite and fix any compilation or runtime failures caused by the path and DTO changes

## 6. Overview Path Correction

- [ ] 6.1 Update `openapi/paths/monthend.yaml`: replace `GET /monthend/{month}/status-overview` with two paths — `GET /monthend/{month}/status-overview/employee` and `GET /monthend/{month}/status-overview/project-lead` — each with its own `operationId`
- [ ] 6.2 Run the OpenAPI generator and verify two separate methods are generated in `MonthEndApi`
- [ ] 6.3 In `MonthEndResource`: replace `getMonthEndStatusOverview` with `getEmployeeMonthEndStatusOverview` (delegates unconditionally to the employee use case, `@MegaRolesAllowed(Role.EMPLOYEE)`) and `getProjectLeadMonthEndStatusOverview` (delegates unconditionally to the project-lead use case, `@MegaRolesAllowed(Role.PROJECT_LEAD)`)
- [ ] 6.4 Update `MonthEndResourceTest`: add test that a project-lead calling `/employee` gets employee view; add test that a caller without project-lead role gets 403 on `/project-lead`

## 1. OpenAPI Contract

- [ ] 1.1 Update `openapi/paths/monthend.yaml`: replace `/monthend/employee/status-overview` and `/monthend/project-lead/status-overview` with `GET /monthend/{month}/status-overview`; add `{month}` path parameter
- [ ] 1.2 Update `openapi/paths/monthend.yaml`: replace `/monthend/employee/clarifications` and `/monthend/project-lead/clarifications` with a single `POST /monthend/clarifications`
- [ ] 1.3 Update `openapi/paths/monthend.yaml`: rename `/monthend/ops/generation` to `POST /monthend/{month}/generate`; drop the request body
- [ ] 1.4 Consolidate all tags to `MonthEnd` across all path entries in `monthend.yaml`
- [ ] 1.5 Update `openapi/schemas/monthend.yaml`: replace `CreateEmployeeClarificationRequest` and `CreateProjectLeadClarificationRequest` with a single `CreateClarificationRequest` (fields: `month`, `projectId`, `text`, optional `subjectEmployeeId`)
- [ ] 1.6 Remove `GenerateMonthEndTasksRequest` schema from `openapi/schemas/monthend.yaml`

## 2. Code Generation

- [ ] 2.1 Run the OpenAPI generator (`mvn generate-sources` or equivalent) and verify a single `MonthEndApi` interface is generated alongside the updated model classes

## 3. Resource Implementation

- [ ] 3.1 Create `MonthEndResource` implementing `MonthEndApi`, annotated `@RequestScoped @Authenticated`; inject all use cases and ports previously spread across the four resource classes
- [ ] 3.2 Implement `getMonthEndStatusOverview(@PathParam month)`: role-dispatch via `authenticatedActorContext.hasRole(Role.PROJECT_LEAD)` to the appropriate use case; consolidate the `resolveProjectRefs` / `resolveUserRefs` helpers as private methods
- [ ] 3.3 Implement `createMonthEndClarification(CreateClarificationRequest)`: set `subjectEmployeeId = actorId` unless actor holds `PROJECT_LEAD` and request supplies a non-null `subjectEmployeeId`; annotate `@MegaRolesAllowed(Role.EMPLOYEE)`
- [ ] 3.4 Implement `prepareMonthEndProject(PrepareMonthEndProjectRequest)`: delegate to `PrematureMonthEndPreparationUseCase`; annotate `@MegaRolesAllowed(Role.EMPLOYEE)`
- [ ] 3.5 Migrate the four shared methods (`completeMonthEndTask`, `resolveMonthEndClarification`, `deleteMonthEndClarification`, `updateMonthEndClarificationText`) from `MonthEndSharedResource` unchanged
- [ ] 3.6 Implement `generateMonthEndTasks(@PathParam month)`: annotate `@Tenant("mega-cron") @RolesAllowed("mega-cron:sync")`; no request body

## 4. Cleanup

- [ ] 4.1 Delete `MonthEndEmployeeResource`, `MonthEndProjectLeadResource`, `MonthEndSharedResource`, `MonthEndOpsResource`
- [ ] 4.2 Verify no remaining references to the deleted classes or old generated interfaces (`MonthEndEmployeeApi`, `MonthEndProjectLeadApi`, `MonthEndSharedApi`, `MonthEndOpsApi`)

## 5. Tests

- [ ] 5.1 Create `MonthEndResourceTest` covering: employee gets employee overview, project-lead gets project-lead overview, clarification creation with and without `subjectEmployeeId`, ops generation endpoint
- [ ] 5.2 Delete or migrate tests from `MonthEndEmployeeAndProjectLeadResourceTest`, `MonthEndSharedResourceTest`, `MonthEndOpsResourceTest`
- [ ] 5.3 Run the full test suite and fix any compilation or runtime failures caused by the path and DTO changes

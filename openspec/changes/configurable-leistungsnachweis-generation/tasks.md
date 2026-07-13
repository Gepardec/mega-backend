## 1. Domain: flag on the Project aggregate

- [x] 1.1 Add `boolean leistungsnachweisEnabled` field to the `Project` record; update the canonical constructor and null/validation checks
- [x] 1.2 Set `leistungsnachweisEnabled = true` in `Project.create(ProjectId, ZepProjectProfile)`
- [x] 1.3 Preserve the flag in `withSyncedZepData(...)` and `withLeads(...)`
- [x] 1.4 Add `withLeistungsnachweisEnabled(boolean)` returning a new instance with all other fields preserved
- [x] 1.5 Unit-test create default (true), resync preservation, lead-sync preservation, and toggle transition

## 2. Persistence

- [x] 2.1 Add `leistungsnachweis_enabled` boolean column (not null) to `ProjectEntity`
- [x] 2.2 Add a Liquibase changelog adding the column with `defaultValueBoolean: true`; register it in `changelog-master.xml`
- [x] 2.3 Update the Project entity↔domain MapStruct mapper to carry the flag both ways
- [ ] 2.4 Integration-test round-trip persistence and that existing rows backfill to `true`

## 3. Month-end snapshot + generation gating

- [ ] 3.1 Add `boolean leistungsnachweisEnabled` to `MonthEndProjectSnapshot`
- [ ] 3.2 Update `MonthEndProjectSnapshotMapper` to map the flag from `Project`
- [ ] 3.3 Gate `LEISTUNGSNACHWEIS` creation in `MonthEndTaskPlanningService.planProjectTasks` on `project.leistungsnachweisEnabled()` (in addition to billable + active leads)
- [ ] 3.4 Unit-test planning: flag=true generates Leistungsnachweis; flag=false suppresses it while `PROJECT_LEAD_REVIEW` and `ABRECHNUNG` are still generated
- [ ] 3.5 Verify (test) that disabling the flag does not affect already-generated tasks and only the next run is affected

## 4. Application: use cases (project BC)

- [ ] 4.1 Add inbound port `GetLeadProjectsUseCase` returning the caller's led projects; implement service using `ProjectRepository.findAllByLead(actorId)`
- [ ] 4.2 Add inbound port `SetLeistungsnachweisEnabledUseCase(projectId, enabled, actorId)`; implement service: load project (not-found error if absent), assert `project.leads().contains(actorId)` else authorization error, apply `withLeistungsnachweisEnabled`, save
- [ ] 4.3 Unit-test the toggle service: success path, non-lead rejection (flag unchanged), unknown project

## 5. Inbound REST adapter (project BC)

- [ ] 5.1 Add OpenAPI paths file for projects (`GET /projects`, `PUT /projects/{projectId}/leistungsnachweis-enabled`) and register it in `openapi.yaml`
- [ ] 5.2 Add OpenAPI schemas: project list item (`id`, `zepId`, `name`, `billable`, `leistungsnachweisEnabled`) and toggle request (`enabled`)
- [ ] 5.3 Implement `ProjectResource` (first inbound adapter in the project BC) against the generated `ProjectApi`, with `@MegaRolesAllowed(Role.PROJECT_LEAD)` and actor from `AuthenticatedActorContext`
- [ ] 5.4 Add REST mapper (domain → project list DTO) via MapStruct
- [ ] 5.5 Map the not-found and authorization domain errors to appropriate HTTP responses (`404` / `403`)

## 6. Verification

- [ ] 6.1 REST test `GET /projects`: returns only the caller's led projects; empty list; `403` for non-lead role
- [ ] 6.2 REST test `PUT .../leistungsnachweis-enabled`: lead toggles own project; `403` for wrong-project lead; not-found for unknown project
- [ ] 6.3 Run `mvn clean package` (ArchUnit + full suite) and confirm green

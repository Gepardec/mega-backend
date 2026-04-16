## 1. Domain Model

- [ ] 1.1 Add `MonthEndOverviewClarificationItem` record to `domain/model/` with fields: `clarificationId`, `projectId`, `subjectEmployeeId`, `createdBy`, `creatorSide`, `status`, `text`, `canResolve`, `resolutionNote`, `resolvedBy`, `resolvedAt`, `createdAt`, `lastModifiedAt`
- [ ] 1.2 Extend `MonthEndStatusOverview` record with `List<MonthEndOverviewClarificationItem> clarifications`; update compact constructor to include null-check and defensive copy

## 2. Repository Port

- [ ] 2.1 Add `findAllEmployeeClarifications(UserId employeeId, YearMonth month)` to `MonthEndClarificationRepository` port
- [ ] 2.2 Add `findAllProjectLeadClarifications(UserId leadId, YearMonth month)` to `MonthEndClarificationRepository` port

## 3. Repository Adapter

- [ ] 3.1 Implement `findAllEmployeeClarifications` in `MonthEndClarificationRepositoryAdapter` (same as `findOpenEmployeeClarifications` but without status filter)
- [ ] 3.2 Implement `findAllProjectLeadClarifications` in `MonthEndClarificationRepositoryAdapter` (same as `findOpenProjectLeadClarifications` but without status filter)
- [ ] 3.3 Add adapter tests for both new queries in `MonthEndClarificationRepositoryAdapterTest`: verify OPEN and DONE results are both returned, other-month and out-of-scope clarifications excluded

## 4. Application Assembler

- [ ] 4.1 Add `AssembleMonthEndStatusOverviewService` in the application package; constructor-inject `ResolveMonthEndTaskSnapshotLookupService`
- [ ] 4.2 Implement `assemble(List<MonthEndTask> tasks, List<MonthEndClarification> clarifications, UserId actorId, YearMonth month)` — resolves snapshot lookup, maps tasks to `MonthEndStatusOverviewItem` (with `canComplete`), maps clarifications to `MonthEndOverviewClarificationItem` (with `canResolve` via `clarification.canBeResolvedBy(actorId)`), returns `MonthEndStatusOverview`
- [ ] 4.3 Add `AssembleMonthEndStatusOverviewServiceTest` unit test: verify `canComplete` true/false per task, `canResolve` true/false per clarification (employee-created vs lead-created), DONE clarification resolution fields mapped, empty tasks + empty clarifications returns empty overview

## 5. Overview Services

- [ ] 5.1 Simplify `GetEmployeeMonthEndStatusOverviewService`: inject `MonthEndClarificationRepository` and `AssembleMonthEndStatusOverviewService`; replace body with fetch tasks → fetch clarifications via `findAllEmployeeClarifications` → delegate to assembler
- [ ] 5.2 Simplify `GetProjectLeadMonthEndStatusOverviewService`: inject `MonthEndClarificationRepository` and `AssembleMonthEndStatusOverviewService`; replace body with fetch tasks → fetch clarifications via `findAllProjectLeadClarifications` → delegate to assembler
- [ ] 5.3 Update `GetEmployeeMonthEndStatusOverviewServiceTest` to cover the clarifications list and `canResolve` flag
- [ ] 5.4 Update `GetProjectLeadMonthEndStatusOverviewServiceTest` to cover the clarifications list and `canResolve` flag

## 6. OpenAPI Contract

- [ ] 6.1 Update the canonical OpenAPI document: add `MonthEndOverviewClarificationItem` schema (with `canResolve`, resolution fields); extend the employee and project-lead status overview response schemas to include a `clarifications` array

## 7. Spec Sync

- [ ] 7.1 Sync delta spec: update `openspec/specs/monthend-status-overview/spec.md` with the clarifications-in-overview and `canResolve` requirements
- [ ] 7.2 Sync delta spec: update `openspec/specs/monthend-clarifications/spec.md` with the two new full-status query requirements
- [ ] 7.3 Sync delta spec: update `openspec/specs/monthend-rest-api/spec.md` with the modified employee and project-lead overview endpoint scenarios

## 1. Database Migration

- [ ] 1.1 Add Liquibase changeset: set `subject_employee_id` nullable on `monthend_clarification`
- [ ] 1.2 Add Liquibase changeset: drop `creator_side` column from `monthend_clarification`

## 2. Domain Model

- [ ] 2.1 Remove `creatorSide` field and `MonthEndClarificationSide` enum from `MonthEndClarification` record
- [ ] 2.2 Make `subjectEmployeeId` nullable (`UserId?`) in `MonthEndClarification` record
- [ ] 2.3 Replace `validateCreator()` logic: `createdBy` must be in `isInvolved()` set; if `subjectEmployeeId` is null, `createdBy` must be in `eligibleProjectLeadIds`
- [ ] 2.4 Replace `isActorOnCreatorSide()` with `isInvolved(actor)`: eligible lead OR (subjectEmployeeId present AND equals actor)
- [ ] 2.5 Replace `canEditText(actor)` with: `isOpen() AND actor == createdBy`
- [ ] 2.6 Replace `canBeResolvedBy(actor)` with: `isOpen() AND isInvolved(actor) AND actor != createdBy`
- [ ] 2.7 Add `canDelete(actor)`: `isOpen() AND actor == createdBy`
- [ ] 2.8 Update `MonthEndClarification.create()` factory — remove `creatorSide` parameter, add nullable `subjectEmployeeId`
- [ ] 2.9 Update `MonthEndClarificationSide` removal: delete the enum file

## 3. Project-Level Context

- [ ] 3.1 Add `MonthEndProjectContext` record (month, project snapshot, eligibleProjectLeadIds) to `monthend.domain.model`
- [ ] 3.2 Implement `MonthEndProjectContextService` in `monthend.domain.services` — resolves project snapshot and active leads for `(month, projectId)`, throws `MonthEndProjectContextNotFoundException` if project not found

## 4. Persistence Adapter

- [ ] 4.1 Remove `creatorSide` / `MonthEndClarificationSide` from `MonthEndClarificationEntity`
- [ ] 4.2 Make `subjectEmployeeId` nullable in `MonthEndClarificationEntity`
- [ ] 4.3 Update `MonthEndClarificationMapper` — remove `creatorSide` mapping
- [ ] 4.4 Add `delete(MonthEndClarificationId id)` to `MonthEndClarificationRepository` port
- [ ] 4.5 Implement `delete` in `MonthEndClarificationRepositoryAdapter` (hard delete row + cascade join-table rows)

## 5. Application Layer — Create Use Case

- [ ] 5.1 Remove `creatorSide` parameter from `CreateMonthEndClarificationUseCase` interface
- [ ] 5.2 Update `CreateMonthEndClarificationService` — branch on presence/absence of `subjectEmployeeId`: use `MonthEndEmployeeProjectContextService` when present, `MonthEndProjectContextService` when absent
- [ ] 5.3 Remove `Objects.requireNonNull(subjectEmployeeId, ...)` guard in the service; validate instead that if null, actor is in eligible leads

## 6. Application Layer — Delete Use Case

- [ ] 6.1 Create `DeleteMonthEndClarificationUseCase` port interface
- [ ] 6.2 Implement `DeleteMonthEndClarificationService`: load clarification by id, call `canDelete(actorId)`, throw `MonthEndActorNotAuthorizedException` if actor is not creator, throw `MonthEndClarificationClosedException` if status is `DONE`, call `repository.delete(id)`

## 7. REST Layer

- [ ] 7.1 Add optional `subjectEmployeeId` field to `CreateProjectLeadClarificationRequest` in the OpenAPI spec
- [ ] 7.2 Remove `creatorSide` from all clarification response models in the OpenAPI spec (`MonthEndClarificationResponse`, `MonthEndWorklistClarification`, `MonthEndOverviewClarificationEntry`)
- [ ] 7.3 Add DELETE `/month-end/clarifications/{id}` endpoint to the shared API spec
- [ ] 7.4 Update `MonthEndRestMapper` — remove `creatorSide` mapping from request/response mappers
- [ ] 7.5 Implement DELETE endpoint in `MonthEndSharedResource` — map to `DeleteMonthEndClarificationUseCase`
- [ ] 7.6 Update `MonthEndProjectLeadResource` create clarification handler to pass `subjectEmployeeId` (nullable) from request

## 8. Tests

- [ ] 8.1 Update `MonthEndClarificationTest` — remove all `creatorSide`-based assertions; add project-level creation scenarios; add `canDelete` scenarios
- [ ] 8.2 Update `CreateMonthEndClarificationServiceTest` — remove `creatorSide` parameter; add project-level creation test (no subject employee, uses `MonthEndProjectContextService`)
- [ ] 8.3 Add `MonthEndProjectContextServiceTest` — valid context, inactive project, inactive leads excluded
- [ ] 8.4 Add `DeleteMonthEndClarificationServiceTest` — creator deletes open successfully, creator rejected when done, non-creator rejected
- [ ] 8.5 Update `UpdateMonthEndClarificationServiceTest` — verify creator-only edit rule for all three scenarios
- [ ] 8.6 Update `CompleteMonthEndClarificationServiceTest` — verify involved-non-creator resolution for all three scenarios (including cross-lead resolution)
- [ ] 8.7 Update `MonthEndClarificationRepositoryAdapterTest` — add delete test; verify project-level records appear in lead queries
- [ ] 8.8 Update `MonthEndSharedResourceTest` and `MonthEndEmployeeAndProjectLeadResourceTest` — add DELETE endpoint test, update request/response assertions

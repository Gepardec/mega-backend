## 1. Outbound Port: MonthEndTaskRepository

- [ ] 1.1 Add `existsForSubjectEmployee(YearMonth month, ProjectId projectId, UserId subjectEmployeeId)` to `MonthEndTaskRepository` port
- [ ] 1.2 Implement `existsForSubjectEmployee` in `MonthEndTaskRepositoryAdapter`
- [ ] 1.3 Remove `findByBusinessKey(MonthEndTaskKey businessKey)` from `MonthEndTaskRepository` port
- [ ] 1.4 Remove `findByBusinessKey` implementation from `MonthEndTaskRepositoryAdapter`

## 2. Inbound Port: PrematureMonthEndPreparationUseCase

- [ ] 2.1 Change `prepare` signature: remove `ProjectId projectId` parameter, change return type from `MonthEndPreparationResult` to `void`

## 3. Application Service: PrematureMonthEndPreparationService

- [ ] 3.1 Replace injected `MonthEndEmployeeProjectContextService` with `MonthEndProjectSnapshotPort`, `MonthEndUserSnapshotPort`, and `MonthEndProjectAssignmentPort`
- [ ] 3.2 Rewrite `prepare`: load `activeUsersById` once; resolve actor or throw; filter active projects to those the actor is assigned to
- [ ] 3.3 For each assigned project: guard with `existsForSubjectEmployee`; on skip, `continue`
- [ ] 3.4 On no existing tasks: call `planEmployeeOwnedTasks`, `save` each task directly (no `ensureTask`)
- [ ] 3.5 After saving tasks: resolve `eligibleLeadIds` from `project.leadIds() ∩ activeUsersById`; save `MonthEndClarification.create(...)` for that project
- [ ] 3.6 Track `totalTasks` and `projectsNewlyPrepared` counters; emit log: `"Employee %s prematurely prepared %d tasks across %d projects for month %s"`
- [ ] 3.7 Remove the `ensureTask` private method

## 4. Domain Model Cleanup

- [ ] 4.1 Delete `MonthEndPreparationResult` record class

## 5. REST Adapter

- [ ] 5.1 Update `MonthEndResource.prepareMonthEndProject()`: remove `projectId` from use case call, change response to `Response.noContent().build()`
- [ ] 5.2 Remove `toDto(MonthEndPreparationResult, Map<UserId, UserRef>, UserId)` overload from `MonthEndRestMapper`

## 6. OpenAPI Contract

- [ ] 6.1 In `schemas/monthend.yaml`: remove `projectId` from `PrepareMonthEndProjectRequest.required` and `PrepareMonthEndProjectRequest.properties`; add `clarificationText` to `required`; remove `nullable: true` from `clarificationText`
- [ ] 6.2 In `paths/monthend.yaml`: change `POST /monthend/preparations` response from `200` to `204 No Content` (no response body schema)
- [ ] 6.3 Regenerate OpenAPI-derived Java sources (`PrepareMonthEndProjectRequestDto` and `MonthEndApi`) to reflect schema changes

## 7. Tests: Unit

- [ ] 7.1 Rewrite `PrematureMonthEndPreparationServiceTest`: cover fan-out across multiple projects, `existsForSubjectEmployee` skip logic, clarification fan-out, log output, actor-not-found error
- [ ] 7.2 Add unit test for `MonthEndTaskRepositoryAdapter.existsForSubjectEmployee`

## 8. Tests: Integration and REST

- [ ] 8.1 Update `MonthEndIT.monthEndFlow_shouldPrepareOwnProjectBeforeScheduledGenerationWithoutDuplicates`: remove `project.id()` from `prepare()` call; add `clarificationText`; assert task count without referencing `MonthEndPreparationResult`
- [ ] 8.2 Update `MonthEndIT.monthEndFlow_shouldExposeClarificationAndAllowPreparedTasksToCompleteBeforeScheduledGeneration`: remove `project.id()` from `prepare()` call; assert clarification fan-out if employee is on multiple projects
- [ ] 8.3 Update `MonthEndResourceTest` preparation test: remove `projectId` from request body; assert 204 response; verify `clarificationText` is required

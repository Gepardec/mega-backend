## Why

The `PrematureMonthEndPreparationUseCase` currently requires the employee to supply a specific `projectId`, limiting early preparation to one project at a time. Employees leaving on vacation need their obligations prepared across all their active projects in a single action, with a clarification reason sent to each project's leads.

## What Changes

- **BREAKING** `PrematureMonthEndPreparationUseCase.prepare()` drops the `projectId` parameter and returns `void` instead of `MonthEndPreparationResult`
- The use case now fans out across all projects the actor is assigned to in the given month, generating employee-owned tasks and one clarification per project context where no tasks exist yet
- `clarificationText` becomes required (was optional/nullable)
- `POST /monthend/preparations` response changes from `200 + body` to `204 No Content`
- `projectId` is removed from the `PrepareMonthEndProjectRequest` OpenAPI schema
- `MonthEndPreparationResult` domain record is deleted
- `MonthEndTaskRepository.findByBusinessKey()` is deleted (no remaining callers)
- New outbound port method `MonthEndTaskRepository.existsForSubjectEmployee(month, projectId, subjectEmployeeId)` guards against re-preparation of already-prepared project contexts
- Clarification is only created for a project if no tasks exist yet for that month/project/subject combination (idempotency guard)

## Capabilities

### New Capabilities

_(none — this change adapts existing behaviour only)_

### Modified Capabilities

- `monthend-self-service-preparation`: requirement changes from single-project to all-assigned-projects, clarification text becomes required, idempotency rule now applies at project-context level
- `monthend-rest-api`: `POST /monthend/preparations` request no longer includes `projectId`, response changes to 204 No Content

## Impact

- **Inbound port**: `PrematureMonthEndPreparationUseCase` — signature change
- **Application service**: `PrematureMonthEndPreparationService` — full rewrite; drops dependency on `MonthEndEmployeeProjectContextService`, gains direct dependencies on `MonthEndProjectSnapshotPort`, `MonthEndUserSnapshotPort`, `MonthEndProjectAssignmentPort`
- **Outbound port + adapter**: `MonthEndTaskRepository` — remove `findByBusinessKey`, add `existsForSubjectEmployee`
- **Domain model**: `MonthEndPreparationResult` deleted
- **REST adapter**: `MonthEndResource.prepareMonthEndProject()` — 204 response, no body; `MonthEndRestMapper` — remove `toDto(MonthEndPreparationResult, ...)` overload
- **OpenAPI contract**: `PrepareMonthEndProjectRequest` schema updated; path response updated
- **Tests**: `PrematureMonthEndPreparationServiceTest` rewritten; two `MonthEndIT` tests updated; `MonthEndResourceTest` preparation test updated

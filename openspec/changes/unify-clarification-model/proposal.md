## Why

The current `MonthEndClarification` model is limited to employee-scoped clarifications and encodes authorization through a `creatorSide` enum that couples visibility, editability, and resolution logic in an indirect and brittle way. Adding project-level clarifications (lead-to-lead) requires rethinking the model from first principles so that all three clarification scenarios — lead-for-employee, employee-for-leads, lead-for-leads — are handled by one clean aggregate.

## What Changes

- **BREAKING** Remove `creatorSide: EMPLOYEE | PROJECT_LEAD` from the `MonthEndClarification` aggregate and all persistence/API surfaces
- **BREAKING** Make `subjectEmployeeId` nullable — `null` signals a project-level clarification with no subject employee
- Replace side-based authorization logic with three universal rules: creator edits, any other involved party resolves, creator deletes
- Add project-level clarification creation — leads can open a clarification scoped to just the project (no subject employee), visible and resolvable only by eligible leads
- Add a new project-context resolution path for project-level creation that skips the employee lookup and assignment check
- Add a hard-delete use case — any creator (employee or lead) may permanently remove their own clarification while it is `OPEN`; done clarifications cannot be deleted

## Capabilities

### New Capabilities

None — the project-level context service is an internal mechanism of clarification creation and is specified within `monthend-clarifications`.

### Modified Capabilities

- `monthend-clarifications`: Unify authorization rules, make `subjectEmployeeId` optional, remove `creatorSide`, add project-level scenario, add project-level context resolution, add delete use case
- `monthend-rest-api`: Lead clarification creation allows optional subject employee; shared API gains DELETE endpoint; clarification response entries expose nullable `subjectEmployee`; creator-side/resolver-side language replaced

## Impact

- `MonthEndClarification` domain model (field removal, nullable field, new authorization methods)
- `MonthEndClarificationEntity` and `monthend_clarification` DB table (nullable column, dropped column — Liquibase migration)
- `CreateMonthEndClarificationService` and `CreateMonthEndClarificationUseCase` — project-level path added
- New `DeleteMonthEndClarificationUseCase` + `DeleteMonthEndClarificationService`
- New `delete(MonthEndClarificationId)` method on `MonthEndClarificationRepository`
- REST API: `CreateProjectLeadClarificationRequest` gains optional `subjectEmployeeId`; response models lose `creatorSide`; new DELETE endpoint on shared API
- All existing clarification tests — authorization assertions need updating to reflect new rules

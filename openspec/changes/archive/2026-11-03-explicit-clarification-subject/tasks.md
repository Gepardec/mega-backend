# Tasks

## 1. API contract (OpenAPI)

- [x] 1.1 In `openapi/schemas/monthend.yaml`, add a `description` to `CreateClarificationRequest.subjectEmployeeId` stating that omitting it (or sending `null`) creates a project-level clarification, which only an eligible project lead of the project may create, and that callers send their own id for a clarification about themselves. Align the `createMonthEndClarification` summary/description in `openapi/paths/monthend.yaml`. Verify with `mvn clean compile` that the generated `CreateClarificationRequestDto` is unchanged apart from documentation.

## 2. Application — explicit subject

- [x] 2.1 In `CreateMonthEndClarificationService.resolveSubject`, when `requestedSubjectId` is `null`, return `null` (project-level) only if the actor is an eligible lead of the project. Otherwise throw `MonthEndActorNotAuthorizedException`, before any persistence or event, so the REST layer answers `403` and the aggregate's creator validation (which would answer `400`) is never reached. Leave the explicit-subject branch unchanged. Verify with `CreateMonthEndClarificationServiceTest`: replace `create_shouldUseActorAsSubject_whenNonLeadOmitsSubject` with a case asserting `MonthEndActorNotAuthorizedException`, no save and no `ClarificationCreatedEvent`; add a case where a non-lead names themselves and the clarification is created with them as subject; the existing lead cases (explicit subject, project-level, self as subject) pass unchanged.

## 3. REST adapter

- [x] 3.1 Update `MonthEndResourceTest`: `createMonthEndClarification_shouldReturnCreatedClarificationForEmployee` sends the employee's own id as `subjectEmployeeId` and asserts it is passed through to the use case. Add a case where the use case rejects a non-lead's omitted subject and the API returns `403`. Verify by running `mvn test -Dtest=MonthEndResourceTest`.
- [x] 3.2 Update `MonthEndIT` so every employee-created clarification sends `subjectEmployeeId`, and add an end-to-end case where a non-lead omits it, gets `403`, and no clarification is persisted. Verify by running `mvn test -Dtest=MonthEndIT`.

## 4. Verification

- [x] 4.1 Run `mvn test` and confirm the full suite, including `ArchitectureTest`, passes.

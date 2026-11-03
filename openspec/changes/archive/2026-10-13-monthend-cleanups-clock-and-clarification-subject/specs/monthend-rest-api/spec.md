# Spec Delta

## MODIFIED Requirements

### Requirement: Project-lead monthend clarification creation uses a unified endpoint with optional subject
The system SHALL allow project leads to create monthend clarifications via the unified `POST /monthend/clarifications` endpoint. The subject of the clarification SHALL be determined by whether the authenticated actor is an eligible project lead of the requested project in the requested month, not by the actor's global roles. When the actor is an eligible lead and a `subjectEmployeeId` is provided, the clarification SHALL be scoped to that employee. When the actor is an eligible lead and no `subjectEmployeeId` is provided, the clarification SHALL be project-level with `subjectEmployeeId = null`. When the actor is not an eligible lead and no `subjectEmployeeId` is provided, the authenticated actor SHALL be the subject. When the actor is not an eligible lead and the `subjectEmployeeId` references another user, the API SHALL reject the request with `403 Forbidden`.

#### Scenario: Project lead creates a clarification with an explicit subject employee
- **WHEN** an authenticated eligible project lead of the requested project submits a `CreateClarificationRequest` to `POST /monthend/clarifications` with a `subjectEmployeeId`
- **THEN** the API creates a monthend clarification with the authenticated lead as creator and the specified employee as subject
- **THEN** the API returns the created clarification as a fully enriched clarification response including UserRefs for all involved user references and `canResolve`, `canEditText`, and `canDelete` flags evaluated for the authenticated lead

#### Scenario: Project lead creates a project-level clarification without subject
- **WHEN** an authenticated eligible project lead of the requested project submits a `CreateClarificationRequest` to `POST /monthend/clarifications` without a `subjectEmployeeId`
- **THEN** the API creates a monthend clarification with the authenticated lead as creator and a null subject employee
- **THEN** the API returns the created clarification as a fully enriched clarification response

#### Scenario: Project lead creates a clarification for themselves in a project they lead
- **WHEN** an authenticated eligible project lead of the requested project submits a `CreateClarificationRequest` to `POST /monthend/clarifications` with their own id as `subjectEmployeeId`
- **THEN** the API creates a monthend clarification with the authenticated lead as both creator and subject

#### Scenario: Actor without lead eligibility for the project becomes the subject
- **WHEN** an authenticated actor who is not an eligible project lead of the requested project submits a `CreateClarificationRequest` to `POST /monthend/clarifications` without a `subjectEmployeeId`
- **THEN** the API creates the clarification with the authenticated actor as both creator and subject
- **THEN** this applies regardless of whether the actor holds the project-lead role for other projects

#### Scenario: Employee cannot create a clarification scoped to another employee
- **WHEN** an authenticated actor who is not an eligible project lead of the requested project submits a `CreateClarificationRequest` to `POST /monthend/clarifications` with a `subjectEmployeeId` referencing another user
- **THEN** the API rejects the request with `403 Forbidden`
- **THEN** no clarification is created

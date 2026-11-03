## ADDED Requirements

### Requirement: Monthend clarification creation takes an explicit subject
`POST /monthend/clarifications` SHALL take the clarification's subject only from `subjectEmployeeId` and SHALL NOT infer it from the caller. An omitted or `null` subject SHALL mean a project-level clarification, which only an eligible project lead of the requested project and month may create. A non-lead may name only themselves as subject. Every other request SHALL be rejected with `403 Forbidden`.

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

#### Scenario: Employee creates a clarification about themselves
- **WHEN** an authenticated actor who is not an eligible project lead of the requested project submits a `CreateClarificationRequest` to `POST /monthend/clarifications` with their own id as `subjectEmployeeId`
- **THEN** the API creates a monthend clarification with the authenticated actor as both creator and subject
- **THEN** the API returns `201 Created` with the fully enriched clarification response

#### Scenario: Non-lead cannot create a project-level clarification
- **WHEN** an authenticated actor who is not an eligible project lead of the requested project submits a `CreateClarificationRequest` to `POST /monthend/clarifications` without a `subjectEmployeeId`
- **THEN** the API rejects the request with `403 Forbidden`
- **THEN** no clarification is created
- **THEN** this applies regardless of whether the actor holds the project-lead role for other projects

#### Scenario: Employee cannot create a clarification scoped to another employee
- **WHEN** an authenticated actor who is not an eligible project lead of the requested project submits a `CreateClarificationRequest` to `POST /monthend/clarifications` with a `subjectEmployeeId` referencing another user
- **THEN** the API rejects the request with `403 Forbidden`
- **THEN** no clarification is created

## REMOVED Requirements

### Requirement: Project-lead monthend clarification creation uses a unified endpoint with optional subject
**Reason**: When `subjectEmployeeId` was omitted, the subject depended on whether the caller led the project. A caller who both leads and works on a project could not create a clarification about themselves without knowing that rule. The subject is now always explicit.
**Migration**: Send the caller's own id as `subjectEmployeeId` to create a clarification about oneself. Omit it only for a project-level clarification created by an eligible lead. A non-lead who omits it now gets `403` instead of becoming the subject.

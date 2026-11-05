# monthend-rest-api Specification

## Purpose
TBD - created by archiving change add-spec-first-monthend-api. Update Purpose after archive.

## Requirements

### Requirement: Monthend REST API contract is defined spec-first
The system SHALL define the monthend REST API in a single canonical OpenAPI document. Java API interfaces and HTTP models for monthend endpoints SHALL be generated from that contract and used by the REST adapter implementation.

#### Scenario: Generated API types follow the canonical contract
- **WHEN** the monthend REST contract is updated
- **THEN** the OpenAPI document is updated as the canonical source
- **THEN** the generated Java API interfaces and models reflect that contract for the monthend REST adapter layer

#### Scenario: Monthend REST adapters implement generated interfaces
- **WHEN** the monthend REST API is implemented
- **THEN** handwritten monthend REST adapters implement the generated Java API interfaces instead of defining separate handwritten endpoint signatures

### Requirement: Actor-scoped monthend endpoints derive the acting user from authentication
The system SHALL treat the authenticated caller as the acting monthend actor for all actor-scoped monthend REST endpoints. Actor-scoped requests MUST NOT require a caller-supplied actor identifier for the status overview, payroll month, task completion, project-lead bulk task completion, employee bulk task completion, clarification edit, clarification resolve, employee self-service preparation, or clarification deletion.

#### Scenario: Task completion is attributed to the authenticated caller
- **WHEN** an authenticated eligible actor completes a monthend task through the REST API
- **THEN** the completion uses the authenticated actor as the acting user
- **THEN** the request does not require a separate actor identifier

#### Scenario: Bulk task completion is attributed to the authenticated caller
- **WHEN** an authenticated actor completes monthend tasks through either bulk task completion endpoint
- **THEN** every completed task records the authenticated actor as the completing actor
- **THEN** the request does not require or accept a separate actor identifier

#### Scenario: Self-service preparation acts on the authenticated employee context across all assigned projects
- **WHEN** an authenticated employee prepares monthend obligations through the REST API
- **THEN** the API uses the authenticated employee as the acting user for preparation
- **THEN** the request does not require a project identifier — the system discovers all projects the employee is assigned to
- **THEN** the request requires a non-blank clarification text

#### Scenario: Capacity-scoped reads are resolved for the authenticated caller
- **WHEN** an authenticated actor requests the status overview or the payroll month in either capacity
- **THEN** the API resolves the data for the authenticated actor
- **THEN** the request does not require or accept an actor identifier

### Requirement: Employee self-service preparation endpoint accepts month and clarification text only
The system SHALL provide `POST /monthend/generate-prematurely` accepting a request body with `month` and `clarificationText` as required fields. The endpoint SHALL NOT accept a `projectId` field. The endpoint SHALL return `204 No Content` on success.

#### Scenario: Employee prepares month-end obligations with clarification
- **WHEN** an authenticated employee submits `{ "month": "2026-03", "clarificationText": "I am leaving before the scheduled run." }` to `POST /monthend/generate-prematurely`
- **THEN** the API prepares employee-owned tasks for all projects the employee is assigned to in that month
- **THEN** the API returns `204 No Content`

#### Scenario: Request without clarification text is rejected
- **WHEN** an authenticated employee submits a preparation request without a `clarificationText` field or with a blank value
- **THEN** the API rejects the request with a 400 Bad Request response

### Requirement: Shared monthend endpoints expose monthend actions
The system SHALL provide shared monthend REST endpoints that allow authenticated employee or project-lead actors to complete monthend tasks, edit open clarification text when they are the creator, resolve clarifications when they are an involved party other than the creator, and delete their own open clarifications.

#### Scenario: Eligible actor completes a monthend task
- **WHEN** an authenticated eligible actor submits a task completion request
- **THEN** the API completes the monthend task through the existing completion flow
- **THEN** the API returns the resulting task state including completion metadata when present

#### Scenario: Creator edits their open clarification
- **WHEN** the authenticated creator of an open clarification submits a clarification text update
- **THEN** the API updates the clarification text through the existing clarification update flow
- **THEN** the API returns the updated clarification as a fully enriched clarification response including UserRefs and `canResolve`, `canEditText`, and `canDelete` flags evaluated for the authenticated actor

#### Scenario: Eligible actor resolves a clarification
- **WHEN** an authenticated involved party who is not the creator submits a clarification resolution request with an optional resolution note
- **THEN** the API resolves the clarification through the existing clarification completion flow
- **THEN** the API returns the resolved clarification as a fully enriched clarification response including UserRefs and `canResolve`, `canEditText`, and `canDelete` flags evaluated for the authenticated actor

#### Scenario: Creator deletes their open clarification
- **WHEN** the authenticated creator of an open clarification submits a clarification deletion request
- **THEN** the API permanently removes the clarification
- **THEN** the API returns a 204 No Content response

#### Scenario: Creator cannot delete a done clarification
- **WHEN** the authenticated creator of a done clarification submits a clarification deletion request
- **THEN** the API rejects the request

### Requirement: Clarification responses are enriched by the REST adapter using the authenticated actor
The REST adapter SHALL be responsible for all clarification response enrichment. For every endpoint that returns a clarification (create, update, complete, and overview), the REST adapter SHALL resolve user display information by calling `MonthEndUserSnapshotPort.findByIds()` with the set of UserIds referenced in the clarification, and SHALL evaluate capability flags (`canResolve`, `canEditText`, `canDelete`) by invoking the corresponding domain methods on the `MonthEndClarification` aggregate with the `actorId` derived from the authentication context. No application service SHALL perform this enrichment.

#### Scenario: Mutation response carries enriched clarification without re-fetch
- **WHEN** an authenticated actor creates, updates, or completes a clarification
- **THEN** the response contains the same fully enriched clarification shape as an overview entry
- **THEN** the client can update its local overview state directly from the mutation response without issuing a separate overview request

#### Scenario: Capability flags reflect the authenticated actor's permissions
- **WHEN** the REST adapter maps a clarification to its generated `Dto`
- **THEN** `canEditText` is `true` only when the authenticated actor is the creator and the clarification is open
- **THEN** `canResolve` is `true` only when the authenticated actor is an involved party, is not the creator, and the clarification is open
- **THEN** `canDelete` is `true` only when the authenticated actor is the creator and the clarification is open

### Requirement: Work time data is not embedded in the monthend status overview response
The monthend status overview endpoint SHALL NOT include work time aggregations in its response. Work time per payroll month SHALL be fetched via the dedicated `/worktime/{payrollMonth}` endpoint in the same capacity as the overview. The frontend SHALL call the work time endpoint independently to obtain billable/non-billable hour breakdowns alongside the monthend overview.

#### Scenario: Monthend overview response contains no work time fields
- **WHEN** an authenticated actor requests the monthend status overview
- **THEN** the response does NOT contain billable hours, non-billable hours, or work time entries
- **THEN** work time data is available exclusively through `GET /worktime/{payrollMonth}?capacity=EMPLOYEE|CONTROLLING`

### Requirement: Monthend endpoint access follows employee, project-lead, and ops roles
The system SHALL secure monthend REST endpoints by operation. The status overview and payroll month endpoints MUST require the role of the requested capacity. The clarification and preparation creation endpoints MUST require at least the employee role. The employee bulk task completion endpoint MUST require the employee role. The project-lead bulk task completion endpoint MUST require the project-lead role. Internal generation endpoints MUST require the internal sync or cron role defined for operational endpoints.

#### Scenario: Unauthenticated caller cannot access monthend endpoints
- **WHEN** an unauthenticated caller requests any actor-scoped monthend endpoint
- **THEN** the API rejects the request as unauthorized

#### Scenario: Employee can access the employee status overview endpoint
- **WHEN** an authenticated employee requests `GET /monthend/{month}/status-overview?capacity=EMPLOYEE`
- **THEN** the API accepts the request and returns the employee-capacity overview

#### Scenario: Non-project-lead cannot access the project-lead status overview endpoint
- **WHEN** an authenticated actor without the project-lead role requests `GET /monthend/{month}/status-overview?capacity=CONTROLLING`
- **THEN** the API rejects the request as forbidden

#### Scenario: Non-project-lead cannot access the payroll month in the controlling capacity
- **WHEN** an authenticated actor without the project-lead role requests `GET /monthend/payroll-month?capacity=CONTROLLING`
- **THEN** the API rejects the request as forbidden

#### Scenario: Project lead can access both status overview endpoints
- **WHEN** an authenticated project lead requests `GET /monthend/{month}/status-overview?capacity=CONTROLLING`
- **THEN** the API accepts the request and returns the controlling-capacity overview
- **WHEN** the same project lead requests `GET /monthend/{month}/status-overview?capacity=EMPLOYEE`
- **THEN** the API accepts the request and returns the employee-capacity overview scoped to that lead

#### Scenario: Non-project-lead cannot access the project-lead bulk task completion endpoint
- **WHEN** an authenticated actor without the project-lead role requests `POST /monthend/{month}/projects/{projectId}/tasks/complete`
- **THEN** the API rejects the request as forbidden

#### Scenario: Caller without the employee role cannot access the employee bulk task completion endpoint
- **WHEN** an authenticated caller without the employee role requests `POST /monthend/{month}/my-tasks/complete`
- **THEN** the API rejects the request as forbidden

#### Scenario: Project lead can access both bulk task completion endpoints
- **WHEN** an authenticated project lead requests `POST /monthend/{month}/projects/{projectId}/tasks/complete`
- **THEN** the API accepts the request and evaluates it against the controlling scope
- **WHEN** the same project lead requests `POST /monthend/{month}/my-tasks/complete`
- **THEN** the API accepts the request and completes only time-check tasks for which the lead is the subject employee

#### Scenario: Employee can access shared monthend endpoint
- **WHEN** an authenticated employee requests a shared actor-scoped monthend endpoint
- **THEN** the API accepts the request and evaluates the action against the existing monthend eligibility rules

#### Scenario: Project lead can access shared monthend endpoint
- **WHEN** an authenticated project lead requests a shared actor-scoped monthend endpoint
- **THEN** the API accepts the request and evaluates the action against the existing monthend eligibility rules

#### Scenario: Non-ops caller cannot access generation endpoint
- **WHEN** an authenticated caller without the internal sync or cron role requests `POST /monthend/{month}/generate`
- **THEN** the API rejects the request as forbidden

### Requirement: Internal monthend generation is available through the same API contract
The system SHALL provide an internal monthend generation endpoint in the same OpenAPI contract for operational callers. The endpoint SHALL trigger monthend task generation for the requested month and return the generation result using generated `Dto` types. The month SHALL be encoded in the path.

#### Scenario: Ops caller triggers generation for a month
- **WHEN** an authenticated internal ops caller submits `POST /monthend/{month}/generate`
- **THEN** the API triggers monthend task generation for that month
- **THEN** the API returns the generation result including created and skipped counts

### Requirement: Machine-to-machine monthend endpoints are grouped under a distinct contract tag
The contract SHALL group monthend endpoints secured by the internal client-credentials scheme under the `Cron` tag, distinct from every tag carried by actor-facing monthend endpoints, so that consumers can exclude them by tag without enumerating their paths. No endpoint reachable by an authenticated employee or project-lead session SHALL carry the `Cron` tag.

#### Scenario: Generation endpoint does not share a tag with actor-facing endpoints
- **WHEN** the contract is inspected for the tags carried by `POST /monthend/{month}/generate`
- **THEN** the endpoint carries the `Cron` tag
- **THEN** the endpoint carries no tag that any employee-facing or project-lead-facing monthend endpoint also carries

#### Scenario: A further client-credentials endpoint is added to the contract
- **WHEN** a monthend endpoint secured by the internal client-credentials scheme is added
- **THEN** the endpoint carries the `Cron` tag
- **THEN** the endpoint carries no tag that any employee-facing or project-lead-facing monthend endpoint also carries

### Requirement: Project-lead bulk completion rejects invalid requests
The project-lead bulk completion endpoint SHALL respond with `422` and the code `MONTHEND_TASK_TYPE_NOT_BULK_COMPLETABLE` for any `type` other than `LEISTUNGSNACHWEIS` or `PROJECT_LEAD_REVIEW`, and with `403` when the caller is not an eligible actor on at least one task of the requested type of the referenced project in that month, including when the project is unknown or has no tasks of that type. A caller who is eligible on tasks of the requested type but has none of them open SHALL receive `200` with an empty `completed` array.

#### Scenario: Unsupported task type is rejected
- **WHEN** a project lead submits the request with `type` set to `EMPLOYEE_TIME_CHECK` or `ABRECHNUNG`
- **THEN** the API rejects the request with `422` and the code `MONTHEND_TASK_TYPE_NOT_BULK_COMPLETABLE`

#### Scenario: Caller who does not lead the project is forbidden
- **WHEN** an authenticated project lead submits `{ "type": "PROJECT_LEAD_REVIEW" }` for a project they do not lead in that month
- **THEN** the API rejects the request with `403`

#### Scenario: Unknown project is forbidden
- **WHEN** an authenticated project lead submits the request with a `projectId` that has no month-end tasks for that month
- **THEN** the API rejects the request with `403`

#### Scenario: Lead requesting Leistungsnachweis is forbidden
- **WHEN** an authenticated project lead without the project coordinator role submits `{ "type": "LEISTUNGSNACHWEIS" }` for a project they lead, whose `LEISTUNGSNACHWEIS` tasks list project coordinators as eligible actors
- **THEN** the API rejects the request with `403`

#### Scenario: Project without tasks of the requested type is forbidden
- **WHEN** an authenticated project coordinator submits `{ "type": "LEISTUNGSNACHWEIS" }` for a project that has no `LEISTUNGSNACHWEIS` tasks in that month
- **THEN** the API rejects the request with `403`

#### Scenario: Lead with no tasks of the requested type receives an empty result
- **WHEN** an authenticated project lead submits `{ "type": "PROJECT_LEAD_REVIEW" }` for a project they lead, is eligible on its `PROJECT_LEAD_REVIEW` tasks in that month, and all of those tasks are already `DONE`
- **THEN** the API returns `200` with an empty `completed` array

### Requirement: Bulk completion responses list only newly completed tasks
Both bulk completion endpoints SHALL respond with `{ "completed": [ ... ] }`, where each entry uses the same task shape returned by single-task completion (`POST /monthend/tasks/{taskId}/complete`) and only tasks newly transitioned to `DONE` are included. Neither endpoint SHALL accept an actor or subject employee identifier.

#### Scenario: Already-completed tasks are omitted from the response
- **WHEN** some tasks in the requested scope of either endpoint are already `DONE`
- **THEN** the response `completed` array contains only the tasks newly transitioned to `DONE`

#### Scenario: Re-issuing a completed request returns an empty completed list
- **WHEN** a caller re-submits a bulk request to either endpoint whose in-scope tasks are all already `DONE`
- **THEN** the API returns `200` with an empty `completed` array

### Requirement: Monthend status overview is available per capacity
The system SHALL provide `GET /monthend/{month}/status-overview` with a required `capacity` query parameter. With `capacity=EMPLOYEE` it SHALL return the tasks and clarifications where the authenticated actor is the subject. With `capacity=CONTROLLING` it SHALL return all tasks and clarifications for projects the authenticated actor leads. Both capacities SHALL return the same response shape.

#### Scenario: Actor retrieves their overview in the employee capacity
- **WHEN** an authenticated actor requests `GET /monthend/{month}/status-overview?capacity=EMPLOYEE`
- **THEN** the API returns tasks where the actor is the subject, including both open and completed tasks
- **THEN** the API returns all clarifications where the actor is the subject for that month, including both open and resolved clarifications

#### Scenario: Project lead retrieves their overview in the controlling capacity
- **WHEN** an authenticated project lead requests `GET /monthend/{month}/status-overview?capacity=CONTROLLING`
- **THEN** the API returns all tasks for projects the lead leads, including both open and completed tasks
- **THEN** the API returns all clarifications for projects the lead leads for that month, including both open and resolved clarifications

#### Scenario: Overview task entries carry display references and the capacity-aware completion flag
- **WHEN** an authenticated actor requests the status overview in either capacity
- **THEN** each overview task entry includes a nested project object containing the project identifier and project name
- **THEN** each overview task entry includes a nullable nested subject employee object containing the employee identifier and full name
- **THEN** each overview task entry includes a `canComplete` field that is `true` only if the actor is eligible to complete that task and the task type is a duty of the requested capacity, and `false` otherwise

#### Scenario: Overview clarification entries carry display references and actor-based capability flags
- **WHEN** an authenticated actor requests the status overview in either capacity
- **THEN** each overview clarification entry includes a nullable nested employee reference object for `subjectEmployee` and a nested employee reference object for `createdBy`
- **THEN** each resolved overview clarification entry includes a nested employee reference object for `resolvedBy` and the resolution fields
- **THEN** each overview clarification entry includes a `canResolve` field set to `true` if the actor is eligible to resolve that clarification and `false` otherwise
- **THEN** each overview clarification entry includes `canEditText` and `canDelete` fields set to `true` if the actor is the creator of that clarification and it is open, `false` otherwise
- **THEN** the clarification capability flags are the same in both capacities

#### Scenario: Project lead reads their own employee overview
- **WHEN** an authenticated project lead requests `GET /monthend/{month}/status-overview?capacity=EMPLOYEE`
- **THEN** the API returns only tasks and clarifications where the lead is the subject employee
- **THEN** the response is identical in shape to the overview in the controlling capacity

#### Scenario: Status overview without a capacity is rejected
- **WHEN** an authenticated actor requests `GET /monthend/{month}/status-overview` without the `capacity` query parameter
- **THEN** the API rejects the request with `400 Bad Request`

#### Scenario: Status overview with an invalid month is rejected
- **WHEN** an authenticated actor requests the status overview with a malformed `month`
- **THEN** the API rejects the request with `400 Bad Request`

### Requirement: Payroll month is available per capacity
The system SHALL provide `GET /monthend/payroll-month` with a required `capacity` query parameter. It SHALL return the payroll month resolved for the authenticated actor by the resolution rule of the requested capacity, as defined in the `payroll-month` capability: the employee rule for `capacity=EMPLOYEE`, the project-lead rule for `capacity=CONTROLLING`.

#### Scenario: Actor retrieves their payroll month in the employee capacity
- **WHEN** an authenticated actor requests `GET /monthend/payroll-month?capacity=EMPLOYEE`
- **THEN** the API returns the payroll month resolved by the employee rule as a `YearMonth` string in `yyyy-MM` format

#### Scenario: Project lead retrieves their payroll month in the controlling capacity
- **WHEN** an authenticated project lead requests `GET /monthend/payroll-month?capacity=CONTROLLING`
- **THEN** the API returns the payroll month resolved by the project-lead rule as a `YearMonth` string in `yyyy-MM` format

#### Scenario: The two capacities may resolve different months for the same actor
- **WHEN** an authenticated project lead requests the payroll month once with `capacity=EMPLOYEE` and once with `capacity=CONTROLLING`
- **THEN** each response reflects the rule of its capacity applied to the authenticated lead
- **THEN** the two responses may differ

#### Scenario: Payroll month without a capacity is rejected
- **WHEN** an authenticated actor requests `GET /monthend/payroll-month` without the `capacity` query parameter
- **THEN** the API rejects the request with `400 Bad Request`

#### Scenario: Unauthenticated caller cannot access the payroll month endpoint
- **WHEN** an unauthenticated caller requests `GET /monthend/payroll-month`
- **THEN** the API rejects the request as unauthorized

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

### Requirement: Project leads bulk complete a project's tasks of one type
The system SHALL provide `POST /monthend/{month}/projects/{projectId}/tasks/complete`, where `month` is in `yyyy-MM` format and `projectId` identifies the project, requiring the project-lead role and a request body with the required field `type`. On success it SHALL return `200` after completing the caller's open tasks of that type for that project and month. Project coordinators, who are assumed to hold the project-lead role, use the same endpoint to complete `LEISTUNGSNACHWEIS` tasks.

#### Scenario: Project lead completes a whole task column for a project
- **WHEN** an authenticated project lead submits `{ "type": "PROJECT_LEAD_REVIEW" }` to `POST /monthend/2026-06/projects/<uuid>/tasks/complete` for a project they lead in that month
- **THEN** the API completes every open `PROJECT_LEAD_REVIEW` task for that project and month the lead is eligible for
- **THEN** the API returns `200` with a `completed` array containing one entry per newly completed task
- **THEN** each completed task records the authenticated caller as the completing actor

#### Scenario: Coordinator completes the Leistungsnachweis column for a project
- **WHEN** an authenticated project coordinator submits `{ "type": "LEISTUNGSNACHWEIS" }` to `POST /monthend/2026-12/projects/<uuid>/tasks/complete` for a project whose `LEISTUNGSNACHWEIS` tasks list them as eligible actor
- **THEN** the API completes every open `LEISTUNGSNACHWEIS` task for that project and month
- **THEN** the API returns `200` with a `completed` array containing one entry per newly completed task
- **THEN** each completed task records the authenticated coordinator as the completing actor

#### Scenario: Bulk completion agrees with the status overview in the controlling capacity
- **WHEN** the status overview requested with `capacity=CONTROLLING` returns a task of type `LEISTUNGSNACHWEIS` or `PROJECT_LEAD_REVIEW` with `canComplete` set to `true` for the authenticated caller
- **THEN** a bulk completion request by that caller for that task's project, type, and month is accepted and completes that task if it is still open

### Requirement: Employees bulk complete their own time checks
The system SHALL provide `POST /monthend/{month}/my-tasks/complete`, where `month` is in `yyyy-MM` format, requiring the employee role and taking no request body. It SHALL complete the caller's open `EMPLOYEE_TIME_CHECK` tasks in that month across all their projects.

#### Scenario: Employee completes all their time checks for a month
- **WHEN** an authenticated employee sends `POST /monthend/2026-06/my-tasks/complete`
- **THEN** the API completes every open `EMPLOYEE_TIME_CHECK` task in that month whose subject is the caller, across all their projects
- **THEN** the API returns `200` with a `completed` array containing one entry per newly completed task
- **THEN** each completed task records the authenticated caller as the completing actor

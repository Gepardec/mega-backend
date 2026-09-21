## ADDED Requirements

### Requirement: Project leads can bulk complete tasks via a role-suffixed endpoint
The system SHALL provide `POST /monthend/{month}/tasks/complete/project-lead`, where `month` is in `yyyy-MM` format, requiring the project-lead role and a request body with required fields `projectId` and `type`. On success it SHALL return `200` after completing the caller's open tasks of that type for that project and month.

#### Scenario: Project lead completes a whole task column for a project
- **WHEN** an authenticated project lead submits `{ "projectId": "<uuid>", "type": "PROJECT_LEAD_REVIEW" }` to `POST /monthend/2026-06/tasks/complete/project-lead` for a project they lead in that month
- **THEN** the API completes every open `PROJECT_LEAD_REVIEW` task for that project and month the lead is eligible for
- **THEN** the API returns `200` with a `completed` array containing one entry per newly completed task
- **THEN** each completed task records the authenticated caller as the completing actor

#### Scenario: Bulk completion agrees with the project-lead status overview
- **WHEN** the project-lead status overview returns a task of type `LEISTUNGSNACHWEIS` or `PROJECT_LEAD_REVIEW` with `canComplete` set to `true` for the authenticated lead
- **THEN** a project-lead bulk completion request by that lead for that task's project, type, and month is accepted and completes that task if it is still open

### Requirement: Project-lead bulk completion rejects invalid requests
The project-lead bulk completion endpoint SHALL respond with `400` for any `type` other than `LEISTUNGSNACHWEIS` or `PROJECT_LEAD_REVIEW`, and with `403` when the caller does not lead the referenced project in that month, including when the project is unknown. A caller who leads the project but has no open tasks of the requested type SHALL receive `200` with an empty `completed` array.

#### Scenario: Unsupported task type is rejected
- **WHEN** a project lead submits the request with `type` set to `EMPLOYEE_TIME_CHECK` or `ABRECHNUNG`
- **THEN** the API rejects the request with `400`

#### Scenario: Caller who does not lead the project is forbidden
- **WHEN** an authenticated project lead submits the request for a project they do not lead in that month
- **THEN** the API rejects the request with `403`

#### Scenario: Unknown project is forbidden
- **WHEN** an authenticated project lead submits the request with a `projectId` that has no month-end tasks for that month
- **THEN** the API rejects the request with `403`

#### Scenario: Lead with no tasks of the requested type receives an empty result
- **WHEN** an authenticated project lead submits `{ "projectId": "<uuid>", "type": "LEISTUNGSNACHWEIS" }` for a project they lead that has no `LEISTUNGSNACHWEIS` tasks in that month
- **THEN** the API returns `200` with an empty `completed` array

### Requirement: Employees can bulk complete their time checks via a role-suffixed endpoint
The system SHALL provide `POST /monthend/{month}/tasks/complete/employee`, where `month` is in `yyyy-MM` format, requiring the employee role and taking no request body. It SHALL complete the caller's open `EMPLOYEE_TIME_CHECK` tasks in that month across all their projects.

#### Scenario: Employee completes all their time checks for a month
- **WHEN** an authenticated employee sends `POST /monthend/2026-06/tasks/complete/employee`
- **THEN** the API completes every open `EMPLOYEE_TIME_CHECK` task in that month whose subject is the caller, across all their projects
- **THEN** the API returns `200` with a `completed` array containing one entry per newly completed task
- **THEN** each completed task records the authenticated caller as the completing actor

### Requirement: Bulk completion responses list only newly completed tasks
Both bulk completion endpoints SHALL respond with `{ "completed": [ ... ] }`, where each entry uses the same task shape returned by single-task completion (`POST /monthend/tasks/{taskId}/complete`) and only tasks newly transitioned to `DONE` are included. Neither endpoint SHALL accept an actor or subject employee identifier.

#### Scenario: Already-completed tasks are omitted from the response
- **WHEN** some tasks in the requested scope of either endpoint are already `DONE`
- **THEN** the response `completed` array contains only the tasks newly transitioned to `DONE`

#### Scenario: Re-issuing a completed request returns an empty completed list
- **WHEN** a caller re-submits a bulk request to either endpoint whose in-scope tasks are all already `DONE`
- **THEN** the API returns `200` with an empty `completed` array

## MODIFIED Requirements

### Requirement: Actor-scoped monthend endpoints derive the acting user from authentication
The system SHALL treat the authenticated caller as the acting monthend actor for all actor-scoped monthend REST endpoints. Actor-scoped requests MUST NOT require a caller-supplied actor identifier for employee status overview, project-lead status overview, task completion, project-lead bulk task completion, employee bulk task completion, clarification edit, clarification resolve, employee self-service preparation, or clarification deletion.

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

### Requirement: Monthend endpoint access follows employee, project-lead, and ops roles
The system SHALL secure monthend REST endpoints by operation. The status overview endpoint and clarification/preparation creation endpoints MUST require at least the employee role. The employee bulk task completion endpoint MUST require the employee role. The project-lead bulk task completion endpoint MUST require the project-lead role. Internal generation endpoints MUST require the internal sync or cron role defined for operational endpoints.

#### Scenario: Unauthenticated caller cannot access monthend endpoints
- **WHEN** an unauthenticated caller requests any actor-scoped monthend endpoint
- **THEN** the API rejects the request as unauthorized

#### Scenario: Employee can access the employee status overview endpoint
- **WHEN** an authenticated employee requests `GET /monthend/{month}/status-overview/employee`
- **THEN** the API accepts the request and returns the employee view

#### Scenario: Non-project-lead cannot access the project-lead status overview endpoint
- **WHEN** an authenticated actor without the project-lead role requests `GET /monthend/{month}/status-overview/project-lead`
- **THEN** the API rejects the request as forbidden

#### Scenario: Project lead can access both status overview endpoints
- **WHEN** an authenticated project lead requests `GET /monthend/{month}/status-overview/project-lead`
- **THEN** the API accepts the request and returns the project-lead view
- **WHEN** the same project lead requests `GET /monthend/{month}/status-overview/employee`
- **THEN** the API accepts the request and returns the employee view scoped to that lead

#### Scenario: Non-project-lead cannot access the project-lead bulk task completion endpoint
- **WHEN** an authenticated actor without the project-lead role requests `POST /monthend/{month}/tasks/complete/project-lead`
- **THEN** the API rejects the request as forbidden

#### Scenario: Caller without the employee role cannot access the employee bulk task completion endpoint
- **WHEN** an authenticated caller without the employee role requests `POST /monthend/{month}/tasks/complete/employee`
- **THEN** the API rejects the request as forbidden

#### Scenario: Project lead can access both bulk task completion endpoints
- **WHEN** an authenticated project lead requests `POST /monthend/{month}/tasks/complete/project-lead`
- **THEN** the API accepts the request and evaluates it against the project-lead scope
- **WHEN** the same project lead requests `POST /monthend/{month}/tasks/complete/employee`
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

## REMOVED Requirements

### Requirement: Scoped bulk task completion is available via a single endpoint
**Reason**: Replaced by "Project leads can bulk complete tasks via a role-suffixed endpoint", "Project-lead bulk completion rejects invalid requests", and the shared "Bulk completion responses list only newly completed tasks". A caller who does not lead the project, including for an unknown project, now receives `403` instead of `400`, based on task eligibility recorded at generation time.
**Migration**: See the requirements above. Clients that treated `400` as "unknown project" must treat `403` as "not a project you lead this month". Path, request fields, and response shape are unchanged.

### Requirement: Employees can bulk complete their own time-check tasks via a single endpoint
**Reason**: Replaced by "Employees can bulk complete their time checks via a role-suffixed endpoint" and the shared "Bulk completion responses list only newly completed tasks". The endpoint no longer takes a request body, and the optional `projectId` is removed: a project-scoped request completed exactly one task, which single-task completion already covers.
**Migration**: See the requirements above. Clients send the request without a body. To complete the time check for one project, use `POST /monthend/tasks/{taskId}/complete` with the task ID from the status overview. A `projectId` still sent in a body is ignored, and the request then completes all of the caller's open time checks for the month.

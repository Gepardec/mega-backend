## ADDED Requirements

### Requirement: Project leads bulk complete a project's tasks of one type
The system SHALL provide `POST /monthend/{month}/projects/{projectId}/tasks/complete`, where `month` is in `yyyy-MM` format and `projectId` identifies the project, requiring the project-lead role and a request body with the required field `type`. On success it SHALL return `200` after completing the caller's open tasks of that type for that project and month.

#### Scenario: Project lead completes a whole task column for a project
- **WHEN** an authenticated project lead submits `{ "type": "PROJECT_LEAD_REVIEW" }` to `POST /monthend/2026-06/projects/<uuid>/tasks/complete` for a project they lead in that month
- **THEN** the API completes every open `PROJECT_LEAD_REVIEW` task for that project and month the lead is eligible for
- **THEN** the API returns `200` with a `completed` array containing one entry per newly completed task
- **THEN** each completed task records the authenticated caller as the completing actor

#### Scenario: Bulk completion agrees with the status overview in the controlling capacity
- **WHEN** the status overview requested with `capacity=CONTROLLING` returns a task of type `LEISTUNGSNACHWEIS` or `PROJECT_LEAD_REVIEW` with `canComplete` set to `true` for the authenticated lead
- **THEN** a bulk completion request by that lead for that task's project, type, and month is accepted and completes that task if it is still open

### Requirement: Employees bulk complete their own time checks
The system SHALL provide `POST /monthend/{month}/my-tasks/complete`, where `month` is in `yyyy-MM` format, requiring the employee role and taking no request body. It SHALL complete the caller's open `EMPLOYEE_TIME_CHECK` tasks in that month across all their projects.

#### Scenario: Employee completes all their time checks for a month
- **WHEN** an authenticated employee sends `POST /monthend/2026-06/my-tasks/complete`
- **THEN** the API completes every open `EMPLOYEE_TIME_CHECK` task in that month whose subject is the caller, across all their projects
- **THEN** the API returns `200` with a `completed` array containing one entry per newly completed task
- **THEN** each completed task records the authenticated caller as the completing actor

## MODIFIED Requirements

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

## REMOVED Requirements

### Requirement: Project leads can bulk complete tasks via a role-suffixed endpoint
**Reason**: Command paths are named by what they act on, not by the caller's role. The project is the resource being acted on and moves into the path.
**Migration**: Replace `POST /monthend/{month}/tasks/complete/project-lead` with body `{ "projectId": "<uuid>", "type": "<type>" }` by `POST /monthend/{month}/projects/<uuid>/tasks/complete` with body `{ "type": "<type>" }`. Responses and status codes are unchanged.

### Requirement: Employees can bulk complete their time checks via a role-suffixed endpoint
**Reason**: Command paths are named by what they act on, not by the caller's role.
**Migration**: Replace `POST /monthend/{month}/tasks/complete/employee` with `POST /monthend/{month}/my-tasks/complete`. It still takes no body, and responses and status codes are unchanged.

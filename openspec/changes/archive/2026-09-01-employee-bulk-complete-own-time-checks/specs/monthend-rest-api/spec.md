## ADDED Requirements

### Requirement: Employees can bulk complete their own time-check tasks via a single endpoint
The system SHALL provide `POST /monthend/{month}/tasks/complete/employee`, where `month` is a `YearMonth` in `yyyy-MM` format carried in the path, accepting a request body with an optional `projectId`. The endpoint SHALL complete all `EMPLOYEE_TIME_CHECK` tasks in that month whose subject is the authenticated caller and that are currently open, and SHALL return `200` with a body `{ "completed": [ ... ] }` where each entry uses the same task shape returned by single-task completion. When `projectId` is present, the scope SHALL be limited to that project; when it is absent, the scope SHALL be every project for which the caller has an open time-check task in that month. The `completed` array SHALL contain only tasks newly transitioned to `DONE`; tasks already done or not completable by the caller SHALL be omitted. The endpoint SHALL require the employee role. The scope SHALL always be the caller's own tasks — the request SHALL NOT accept a subject employee identifier, so no caller can complete another employee's time check through this endpoint.

#### Scenario: Employee completes all their time checks for a month
- **WHEN** an authenticated employee submits an empty body to `POST /monthend/2026-06/tasks/complete/employee`
- **THEN** the API completes every open `EMPLOYEE_TIME_CHECK` task in that month whose subject is the caller, across all their projects
- **THEN** the API returns `200` with a `completed` array containing one entry per newly completed task in the single-task completion shape
- **THEN** each completed task records the authenticated caller as the completing actor
- **THEN** the request does not require a separate actor identifier

#### Scenario: Employee completes their time check for a single project
- **WHEN** an authenticated employee submits the request with a `projectId`
- **THEN** the API completes only the caller's open `EMPLOYEE_TIME_CHECK` tasks for that project in that month
- **THEN** the caller's open time-check tasks on other projects remain open

#### Scenario: Already-completed tasks are omitted from the response
- **WHEN** some of the caller's time-check tasks in the requested scope are already `DONE`
- **THEN** the response `completed` array contains only the tasks newly transitioned to `DONE`

#### Scenario: Re-issuing a completed request returns an empty completed list
- **WHEN** an employee re-submits a request whose in-scope tasks are all already `DONE`
- **THEN** the API returns `200` with an empty `completed` array

#### Scenario: Employee with no open time checks receives an empty result
- **WHEN** an authenticated employee submits the request for a month in which they have no open time-check tasks
- **THEN** the API returns `200` with an empty `completed` array

#### Scenario: Caller without the employee role is forbidden
- **WHEN** an authenticated caller without the employee role submits the request
- **THEN** the API rejects the request with `403`

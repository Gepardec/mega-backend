## ADDED Requirements

### Requirement: Scoped bulk task completion is available via a single endpoint
The system SHALL provide `POST /monthend/tasks/complete` accepting a request body with required fields `month` (a `YearMonth` in `yyyy-MM` format), `projectId`, and `type`. The endpoint SHALL complete all month-end tasks of the given `type` for the given project and month that the authenticated actor is eligible to complete and that are currently open, and SHALL return `200` with a body `{ "completed": [ ... ] }` where each entry uses the same task shape returned by single-task completion (`POST /monthend/tasks/{taskId}/complete`). The `completed` array SHALL contain only tasks newly transitioned to `DONE`; tasks already done or not completable by the actor SHALL be omitted. The endpoint SHALL accept only the `type` values `LEISTUNGSNACHWEIS` and `PROJECT_LEAD_REVIEW`; any other value — including `EMPLOYEE_TIME_CHECK` and `ABRECHNUNG` — SHALL be rejected with `400`. The endpoint SHALL require the project-lead role, SHALL reject a caller who is not an eligible project lead of the referenced project with `403`, and SHALL reject a `month`/`projectId` pair with no active month-end project context with `400`.

#### Scenario: Project lead completes a whole task column for a project
- **WHEN** an authenticated project lead submits `{ "month": "2026-06", "projectId": "<uuid>", "type": "PROJECT_LEAD_REVIEW" }` to `POST /monthend/tasks/complete`
- **THEN** the API completes every open `PROJECT_LEAD_REVIEW` task for that project and month the lead is eligible for
- **THEN** the API returns `200` with a `completed` array containing one entry per newly completed task in the single-task completion shape
- **THEN** each completed task records the authenticated caller as the completing actor
- **THEN** the request does not require a separate actor identifier

#### Scenario: Already-completed tasks are omitted from the response
- **WHEN** some tasks in the requested scope are already `DONE`
- **THEN** the response `completed` array contains only the tasks newly transitioned to `DONE`

#### Scenario: Re-issuing a completed request returns an empty completed list
- **WHEN** a project lead re-submits a request whose in-scope tasks are all already `DONE`
- **THEN** the API returns `200` with an empty `completed` array

#### Scenario: Employee-time-check type is rejected
- **WHEN** a caller submits the request with `type` set to `EMPLOYEE_TIME_CHECK`
- **THEN** the API rejects the request with `400`

#### Scenario: Abrechnung type is rejected
- **WHEN** a caller submits the request with `type` set to `ABRECHNUNG`
- **THEN** the API rejects the request with `400`

#### Scenario: Caller who is not a lead of the project is forbidden
- **WHEN** an authenticated caller who is not an eligible project lead of the referenced project submits the request
- **THEN** the API rejects the request with `403`

#### Scenario: Unknown or inactive project is rejected
- **WHEN** the request references a project that has no active month-end context for the given month
- **THEN** the API rejects the request with `400`

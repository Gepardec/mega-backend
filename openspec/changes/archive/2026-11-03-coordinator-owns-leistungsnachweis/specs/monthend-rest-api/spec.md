# Spec Delta

## MODIFIED Requirements

### Requirement: Project-lead bulk completion rejects invalid requests
The project-lead bulk completion endpoint SHALL respond with `400` for any `type` other than `LEISTUNGSNACHWEIS` or `PROJECT_LEAD_REVIEW`, and with `403` when the caller is not an eligible actor on at least one task of the requested type of the referenced project in that month, including when the project is unknown or has no tasks of that type. A caller who is eligible on tasks of the requested type but has none of them open SHALL receive `200` with an empty `completed` array.

#### Scenario: Unsupported task type is rejected
- **WHEN** a project lead submits the request with `type` set to `EMPLOYEE_TIME_CHECK` or `ABRECHNUNG`
- **THEN** the API rejects the request with `400`

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

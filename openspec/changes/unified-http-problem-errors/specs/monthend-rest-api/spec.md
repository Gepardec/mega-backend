# Spec Delta

## MODIFIED Requirements

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

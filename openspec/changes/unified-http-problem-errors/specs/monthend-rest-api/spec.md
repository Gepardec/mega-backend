# Spec Delta

## MODIFIED Requirements

### Requirement: Project-lead bulk completion rejects invalid requests
The project-lead bulk completion endpoint SHALL respond with `422` and the code `MONTHEND_TASK_TYPE_NOT_BULK_COMPLETABLE` for any `type` other than `LEISTUNGSNACHWEIS` or `PROJECT_LEAD_REVIEW`, and with `403` when the caller does not lead the referenced project in that month, including when the project is unknown. A caller who leads the project but has no open tasks of the requested type SHALL receive `200` with an empty `completed` array.

#### Scenario: Unsupported task type is rejected
- **WHEN** a project lead submits the request with `type` set to `EMPLOYEE_TIME_CHECK` or `ABRECHNUNG`
- **THEN** the API rejects the request with `422` and the code `MONTHEND_TASK_TYPE_NOT_BULK_COMPLETABLE`

#### Scenario: Caller who does not lead the project is forbidden
- **WHEN** an authenticated project lead submits the request for a project they do not lead in that month
- **THEN** the API rejects the request with `403`

#### Scenario: Unknown project is forbidden
- **WHEN** an authenticated project lead submits the request with a `projectId` that has no month-end tasks for that month
- **THEN** the API rejects the request with `403`

#### Scenario: Lead with no tasks of the requested type receives an empty result
- **WHEN** an authenticated project lead submits `{ "projectId": "<uuid>", "type": "LEISTUNGSNACHWEIS" }` for a project they lead that has no `LEISTUNGSNACHWEIS` tasks in that month
- **THEN** the API returns `200` with an empty `completed` array

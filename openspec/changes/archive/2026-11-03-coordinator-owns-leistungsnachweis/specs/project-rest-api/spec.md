# Spec Delta

## REMOVED Requirements

### Requirement: Project lead can list their project settings
**Reason**: Leistungsnachweis is owned by the project coordinator, not by the project leads. The settings list moves to the coordinator and covers all qualifying projects.
**Migration**: Use `GET /projects/settings` as a project coordinator; see "Project coordinator can list project settings". Project leads without the coordinator role now receive `403 Forbidden`.

### Requirement: Project lead can toggle Leistungsnachweis generation for a project they lead
**Reason**: Only the project coordinator decides whether Leistungsnachweis is generated for a project.
**Migration**: Use `PUT /projects/{projectId}/leistungsnachweis-enabled` as a project coordinator; see "Project coordinator can toggle Leistungsnachweis generation for a project". Project leads without the coordinator role now receive `403 Forbidden`.

## ADDED Requirements

### Requirement: Project coordinator can list project settings
The system SHALL expose `GET /projects/settings`, restricted to the `PROJECT_COORDINATOR` role, returning the Leistungsnachweis settings of all qualifying projects regardless of who leads them. Each entry SHALL contain a `project` object in the shared project reference shape (`id`, `name`, `zepUrl`) and the project's `leistungsnachweisEnabled` flag, and SHALL NOT contain the project's ZEP id or billable flag.

#### Scenario: Project coordinator requests the project settings
- **WHEN** an authenticated `PROJECT_COORDINATOR` user calls `GET /projects/settings`
- **THEN** the system returns `200 OK` with the settings of all qualifying projects
- **THEN** each entry includes a `project` object with `id`, `name`, and a non-null `zepUrl`, and a `leistungsnachweisEnabled` boolean
- **THEN** no entry includes a `zepId` or `billable` field

#### Scenario: Projects the coordinator does not lead are listed
- **WHEN** a project coordinator calls `GET /projects/settings` and a qualifying project exists that the coordinator does not lead
- **THEN** that project is included in the response

#### Scenario: No qualifying projects exist
- **WHEN** no billable, not-yet-ended project exists and a project coordinator calls `GET /projects/settings`
- **THEN** the system returns `200 OK` with an empty list

#### Scenario: Project lead without the coordinator role is rejected
- **WHEN** an authenticated user holding the `PROJECT_LEAD` role but not the `PROJECT_COORDINATOR` role calls `GET /projects/settings`
- **THEN** the system returns `403 Forbidden`

### Requirement: Project settings list only projects a future generation run can affect
`GET /projects/settings` SHALL list only projects that are billable and have not ended before the first day of the current month. Projects that start in a future month SHALL be included. A project's current `leistungsnachweisEnabled` value SHALL NOT affect whether it is listed.

#### Scenario: Non-billable project is not listed
- **WHEN** an active non-billable project exists and a project coordinator calls `GET /projects/settings`
- **THEN** that project is not included in the response

#### Scenario: Project that ended before the current month is not listed
- **WHEN** the current month is 2026-09 and a billable project's end date is 2026-08-31
- **THEN** that project is not included in the response of `GET /projects/settings`

#### Scenario: Project ending in the current month is listed
- **WHEN** the current month is 2026-09 and a billable project's end date is 2026-09-10
- **THEN** that project is included in the response of `GET /projects/settings`

#### Scenario: Project without end date is listed
- **WHEN** a billable project has no end date
- **THEN** that project is included in the response of `GET /projects/settings`

#### Scenario: Project starting in a future month is listed
- **WHEN** the current month is 2026-09 and a billable project starts on 2026-11-01
- **THEN** that project is included in the response of `GET /projects/settings`

#### Scenario: Project with Leistungsnachweis disabled is still listed
- **WHEN** a billable, not-yet-ended project has its `leistungsnachweisEnabled` flag set to `false`
- **THEN** that project is included in the response with `leistungsnachweisEnabled` set to `false`

### Requirement: Project coordinator can toggle Leistungsnachweis generation for a project
The system SHALL expose `PUT /projects/{projectId}/leistungsnachweis-enabled`, restricted to the `PROJECT_COORDINATOR` role, which sets the project's `leistungsnachweisEnabled` flag from a mandatory `enabled` boolean for any project, led by the caller or not. The change takes effect from the next generation run. On success the system SHALL return `200 OK` with the project's settings entry in the `GET /projects/settings` shape, carrying the persisted flag.

#### Scenario: Coordinator disables Leistungsnachweis for a project
- **WHEN** an authenticated `PROJECT_COORDINATOR` user calls `PUT /projects/{projectId}/leistungsnachweis-enabled` with `enabled=false`
- **THEN** the system persists `leistungsnachweisEnabled=false` for that project
- **THEN** the system returns `200 OK` with that project's settings entry, whose `leistungsnachweisEnabled` is `false`
- **THEN** the returned `project` object carries the target project's `id`, `name` and a non-null `zepUrl`

#### Scenario: Coordinator re-enables Leistungsnachweis for a project
- **WHEN** an authenticated `PROJECT_COORDINATOR` user calls the endpoint for a billable project with `enabled=true`
- **THEN** the system persists `leistungsnachweisEnabled=true` for that project
- **THEN** the system returns `200 OK` with that project's settings entry, whose `leistungsnachweisEnabled` is `true`

#### Scenario: Coordinator toggles a project they do not lead
- **WHEN** an authenticated `PROJECT_COORDINATOR` user who is not a lead of the target project calls the endpoint with a valid `enabled` value
- **THEN** the system persists the flag and returns `200 OK`

#### Scenario: Missing enabled value is rejected
- **WHEN** a project coordinator calls the endpoint with a request body that has no `enabled` field or has `enabled` set to `null`
- **THEN** the system returns `400 Bad Request`
- **THEN** the project's `leistungsnachweisEnabled` flag is unchanged

#### Scenario: Project lead without the coordinator role is rejected
- **WHEN** an authenticated user holding the `PROJECT_LEAD` role but not the `PROJECT_COORDINATOR` role calls the endpoint for a project they lead
- **THEN** the system returns `403 Forbidden`
- **THEN** the project's `leistungsnachweisEnabled` flag is unchanged

#### Scenario: Unknown project identifier
- **WHEN** a project coordinator calls the endpoint with a `projectId` that does not correspond to an existing project
- **THEN** the system returns a not-found error

### Requirement: Leistungsnachweis cannot be enabled on a non-billable project through the settings endpoint
Enabling Leistungsnachweis on a non-billable project through `PUT /projects/{projectId}/leistungsnachweis-enabled` SHALL be rejected with `400 Bad Request`, and the flag SHALL stay disabled. Disabling it on a non-billable project SHALL succeed and leave the flag disabled.

#### Scenario: Enabling Leistungsnachweis on a non-billable project is rejected
- **WHEN** a project coordinator calls the endpoint for a non-billable project with `enabled=true`
- **THEN** the system returns `400 Bad Request`
- **THEN** the project's `leistungsnachweisEnabled` flag remains `false`

#### Scenario: Disabling Leistungsnachweis on a non-billable project succeeds
- **WHEN** a project coordinator calls the endpoint for a non-billable project with `enabled=false`
- **THEN** the system returns `200 OK` with that project's settings entry, whose `leistungsnachweisEnabled` is `false`
- **THEN** the project's `leistungsnachweisEnabled` flag remains `false`

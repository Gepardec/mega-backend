# Project REST API

## Purpose

Defines the HTTP endpoints exposed by the Project bounded context. Covers the project lead reading the projects they lead and toggling per-project `LEISTUNGSNACHWEIS` month-end task generation, restricted to the `PROJECT_LEAD` role and additionally authorized per target project.

## Requirements

### Requirement: Project lead can list their project settings
The system SHALL expose a `GET /projects/settings` endpoint that returns the Leistungsnachweis settings of the projects for which the authenticated user is a lead. The endpoint SHALL be restricted to the `PROJECT_LEAD` role. Because the result set is scoped to the caller's own led projects, the role restriction alone is sufficient authorization for reading.

The endpoint SHALL return only projects whose setting can still affect a future month-end generation run. That means projects that are billable **and** have not ended before the current month. Projects that start in a future month SHALL be included. Non-billable projects and projects whose end date lies before the first day of the current month SHALL be omitted. A project's current `leistungsnachweisEnabled` value SHALL NOT affect whether it is listed.

Each entry SHALL contain:
- a `project` object in the shared project reference shape, with `id`, `name` and `zepUrl`
- the project's `leistungsnachweisEnabled` flag

The response SHALL NOT contain the project's ZEP id or its billable flag.

#### Scenario: Project lead requests their projects
- **WHEN** an authenticated `PROJECT_LEAD` user calls `GET /projects/settings`
- **THEN** the system returns `200 OK` with the settings of the qualifying projects the user leads
- **THEN** each entry includes a `project` object with `id`, `name`, and a non-null `zepUrl`, and a `leistungsnachweisEnabled` boolean
- **THEN** no entry includes a `zepId` or `billable` field
- **THEN** projects the user does not lead are not included

#### Scenario: Non-billable project is not listed
- **WHEN** a project lead leads an active non-billable project and calls `GET /projects/settings`
- **THEN** that project is not included in the response

#### Scenario: Project that ended before the current month is not listed
- **WHEN** the current month is 2026-09 and a project lead leads a billable project whose end date is 2026-08-31
- **THEN** that project is not included in the response of `GET /projects/settings`

#### Scenario: Project ending in the current month is listed
- **WHEN** the current month is 2026-09 and a project lead leads a billable project whose end date is 2026-09-10
- **THEN** that project is included in the response of `GET /projects/settings`

#### Scenario: Project without end date is listed
- **WHEN** a project lead leads a billable project that has no end date
- **THEN** that project is included in the response of `GET /projects/settings`

#### Scenario: Project starting in a future month is listed
- **WHEN** the current month is 2026-09 and a project lead leads a billable project that starts on 2026-11-01
- **THEN** that project is included in the response of `GET /projects/settings`

#### Scenario: Project with Leistungsnachweis disabled is still listed
- **WHEN** a project lead leads a billable, not-yet-ended project whose `leistungsnachweisEnabled` flag is `false`
- **THEN** that project is included in the response with `leistungsnachweisEnabled` set to `false`

#### Scenario: Project lead leads no projects
- **WHEN** an authenticated `PROJECT_LEAD` user who leads no billable, not-yet-ended project calls `GET /projects/settings`
- **THEN** the system returns `200 OK` with an empty list

#### Scenario: Non-project-lead user is rejected
- **WHEN** an authenticated user without the `PROJECT_LEAD` role calls `GET /projects/settings`
- **THEN** the system returns `403 Forbidden`

### Requirement: Project lead can toggle Leistungsnachweis generation for a project they lead
The system SHALL expose a `PUT /projects/{projectId}/leistungsnachweis-enabled` endpoint. It sets the project's `leistungsnachweisEnabled` flag from a request body carrying an `enabled` boolean. The `enabled` field is mandatory: a request body without it, or with `enabled` set to `null`, SHALL be rejected with `400 Bad Request`.

The endpoint SHALL be restricted to the `PROJECT_LEAD` role. In addition to the role restriction, the system SHALL verify that the authenticated user is a lead of the specific target project. A user who is not a lead of that project SHALL be rejected even if they hold the `PROJECT_LEAD` role.

Enabling Leistungsnachweis on a non-billable project SHALL be rejected with `400 Bad Request`, and the flag SHALL stay disabled. Disabling it on a non-billable project SHALL succeed and leave the flag disabled.

On success the flag is persisted and takes effect from the next month-end generation run.

#### Scenario: Lead disables Leistungsnachweis for their project
- **WHEN** an authenticated `PROJECT_LEAD` user who is a lead of the target project calls `PUT /projects/{projectId}/leistungsnachweis-enabled` with `enabled=false`
- **THEN** the system persists `leistungsnachweisEnabled=false` for that project
- **THEN** the system returns a success response

#### Scenario: Lead re-enables Leistungsnachweis for their project
- **WHEN** an authenticated `PROJECT_LEAD` user who is a lead of the target billable project calls the endpoint with `enabled=true`
- **THEN** the system persists `leistungsnachweisEnabled=true` for that project

#### Scenario: Enabling Leistungsnachweis on a non-billable project is rejected
- **WHEN** a lead of a non-billable project calls the endpoint with `enabled=true`
- **THEN** the system returns `400 Bad Request`
- **THEN** the project's `leistungsnachweisEnabled` flag remains `false`

#### Scenario: Disabling Leistungsnachweis on a non-billable project succeeds
- **WHEN** a lead of a non-billable project calls the endpoint with `enabled=false`
- **THEN** the system returns a success response
- **THEN** the project's `leistungsnachweisEnabled` flag remains `false`

#### Scenario: Missing enabled value is rejected
- **WHEN** a lead of the target project calls the endpoint with a request body that has no `enabled` field or has `enabled` set to `null`
- **THEN** the system returns `400 Bad Request`
- **THEN** the project's `leistungsnachweisEnabled` flag is unchanged

#### Scenario: Lead of a different project is rejected
- **WHEN** an authenticated `PROJECT_LEAD` user who is NOT a lead of the target project calls the endpoint
- **THEN** the system rejects the request with an authorization error
- **THEN** the project's `leistungsnachweisEnabled` flag is unchanged

#### Scenario: Non-project-lead user is rejected
- **WHEN** an authenticated user without the `PROJECT_LEAD` role calls the endpoint
- **THEN** the system returns `403 Forbidden`

#### Scenario: Unknown project identifier
- **WHEN** the `projectId` does not correspond to an existing project
- **THEN** the system returns a not-found error

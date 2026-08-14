## ADDED Requirements

### Requirement: Project lead can list their own projects
The system SHALL expose a `GET /projects` endpoint that returns the projects for which the authenticated user is a lead. The endpoint SHALL be restricted to the `PROJECT_LEAD` role. Because the result set is scoped to the caller's own led projects, the role restriction alone is sufficient authorization for reading. The response SHALL include, for each project, its identifier, ZEP id, name, `billable` flag, and `leistungsnachweisEnabled` flag.

#### Scenario: Project lead requests their projects
- **WHEN** an authenticated `PROJECT_LEAD` user calls `GET /projects`
- **THEN** the system returns `200 OK` with the list of projects the user leads
- **THEN** each entry includes `id`, `zepId`, `name`, `billable`, and `leistungsnachweisEnabled`

#### Scenario: Project lead leads no projects
- **WHEN** an authenticated `PROJECT_LEAD` user who leads no projects calls `GET /projects`
- **THEN** the system returns `200 OK` with an empty list

#### Scenario: Non-project-lead user is rejected
- **WHEN** an authenticated user without the `PROJECT_LEAD` role calls `GET /projects`
- **THEN** the system returns `403 Forbidden`

### Requirement: Project lead can toggle Leistungsnachweis generation for a project they lead
The system SHALL expose a `PUT /projects/{projectId}/leistungsnachweis-enabled` endpoint that sets the project's `leistungsnachweisEnabled` flag from a request body carrying an `enabled` boolean. The endpoint SHALL be restricted to the `PROJECT_LEAD` role. In addition to the role restriction, the system SHALL verify that the authenticated user is a lead of the specific target project; a user who is not a lead of that project SHALL be rejected even if they hold the `PROJECT_LEAD` role. On success the flag is persisted and takes effect from the next month-end generation run.

#### Scenario: Lead disables Leistungsnachweis for their project
- **WHEN** an authenticated `PROJECT_LEAD` user who is a lead of the target project calls `PUT /projects/{projectId}/leistungsnachweis-enabled` with `enabled=false`
- **THEN** the system persists `leistungsnachweisEnabled=false` for that project
- **THEN** the system returns a success response

#### Scenario: Lead re-enables Leistungsnachweis for their project
- **WHEN** an authenticated `PROJECT_LEAD` user who is a lead of the target project calls the endpoint with `enabled=true`
- **THEN** the system persists `leistungsnachweisEnabled=true` for that project

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

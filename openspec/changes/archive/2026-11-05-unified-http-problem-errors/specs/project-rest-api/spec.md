# Spec Delta

## MODIFIED Requirements

### Requirement: Leistungsnachweis cannot be enabled on a non-billable project through the settings endpoint
Enabling Leistungsnachweis on a non-billable project through `PUT /projects/{projectId}/leistungsnachweis-enabled` SHALL be rejected with `409 Conflict` and the code `PROJECT_LEISTUNGSNACHWEIS_NOT_APPLICABLE`, and the flag SHALL stay disabled. Disabling it on a non-billable project SHALL succeed and leave the flag disabled.

#### Scenario: Enabling Leistungsnachweis on a non-billable project is rejected
- **WHEN** a project coordinator calls the endpoint for a non-billable project with `enabled=true`
- **THEN** the system returns `409 Conflict` with the code `PROJECT_LEISTUNGSNACHWEIS_NOT_APPLICABLE`
- **THEN** the project's `leistungsnachweisEnabled` flag remains `false`

#### Scenario: Disabling Leistungsnachweis on a non-billable project succeeds
- **WHEN** a project coordinator calls the endpoint for a non-billable project with `enabled=false`
- **THEN** the system returns `200 OK` with that project's settings entry, whose `leistungsnachweisEnabled` is `false`
- **THEN** the project's `leistungsnachweisEnabled` flag remains `false`

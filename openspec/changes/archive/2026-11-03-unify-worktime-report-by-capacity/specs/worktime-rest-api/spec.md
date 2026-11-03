## ADDED Requirements

### Requirement: Work time report is available per capacity
The system SHALL provide `GET /worktime/{payrollMonth}` with a required `capacity` query parameter, returning the work time report of the requested capacity as defined in the `worktime-report` capability. Both capacities SHALL return the same response shape. The endpoint SHALL reject a malformed payroll month with `400 Bad Request`.

#### Scenario: Actor retrieves their own work time in the employee capacity
- **WHEN** an authenticated actor requests `GET /worktime/2026-06?capacity=EMPLOYEE`
- **THEN** the API returns the work time report of the employee capacity for that actor and month

#### Scenario: Project lead retrieves work time of their projects in the controlling capacity
- **WHEN** an authenticated project lead requests `GET /worktime/2026-06?capacity=CONTROLLING`
- **THEN** the API returns the work time report of the controlling capacity for that lead and month

#### Scenario: Work time report without a capacity is rejected
- **WHEN** an authenticated actor requests `GET /worktime/2026-06` without the `capacity` query parameter
- **THEN** the API rejects the request with `400 Bad Request`

#### Scenario: Work time report with a malformed payroll month is rejected
- **WHEN** an authenticated actor requests the work time report with a malformed payroll month
- **THEN** the API rejects the request with `400 Bad Request`

## MODIFIED Requirements

### Requirement: Work time endpoints derive the acting user from authentication
The system SHALL treat the authenticated caller as the acting subject for all work time REST endpoints. Work time requests MUST NOT require a caller-supplied actor identifier; the acting user SHALL be resolved from the authentication context.

#### Scenario: Employee work time is resolved for the authenticated employee
- **WHEN** an authenticated actor requests the work time report for a month with `capacity=EMPLOYEE`
- **THEN** the API resolves the acting employee from the authentication context
- **THEN** the request does not require an employee identifier

#### Scenario: Project lead work time is resolved for the authenticated project lead
- **WHEN** an authenticated project lead requests the work time report for a month with `capacity=CONTROLLING`
- **THEN** the API resolves the acting project lead from the authentication context
- **THEN** the request does not require a user identifier

### Requirement: Work time endpoint access follows employee and project-lead roles
The system SHALL secure work time REST endpoints by caller role. The work time report endpoint MUST require the role of the requested capacity: the EMPLOYEE role for `capacity=EMPLOYEE` and the PROJECT_LEAD role for `capacity=CONTROLLING`. The warnings endpoint MUST require the EMPLOYEE role.

#### Scenario: Caller without EMPLOYEE role cannot access the employee work time endpoint
- **WHEN** an authenticated caller without the EMPLOYEE role requests the work time report with `capacity=EMPLOYEE`
- **THEN** the API rejects the request as forbidden

#### Scenario: Caller without PROJECT_LEAD role cannot access the project lead work time endpoint
- **WHEN** an authenticated caller without the PROJECT_LEAD role requests the work time report with `capacity=CONTROLLING`
- **THEN** the API rejects the request as forbidden

#### Scenario: Project lead can read the work time report in both capacities
- **WHEN** an authenticated caller holding both the EMPLOYEE and the PROJECT_LEAD role requests the work time report once with `capacity=EMPLOYEE` and once with `capacity=CONTROLLING`
- **THEN** the API accepts both requests

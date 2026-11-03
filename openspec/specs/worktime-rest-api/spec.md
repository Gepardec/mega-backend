# Work Time REST API

## Purpose

Defines the work time REST API contract within the application's canonical OpenAPI document, including authentication-derived actor scoping and role-based access for employee and project-lead work time endpoints.

## Requirements

### Requirement: Work time REST API contract is defined spec-first
The system SHALL define the work time REST API as part of the single canonical application OpenAPI contract. The source contract MAY be split across referenced files and SHALL be bundled for code generation. Java API interfaces and HTTP models for work time endpoints SHALL be generated from that bundled contract and used by the REST adapter implementation.

#### Scenario: Generated API types follow the canonical contract
- **WHEN** the work time REST contract is updated
- **THEN** the OpenAPI document is updated as the canonical source
- **THEN** the generated Java API interfaces and models reflect that contract for the worktime REST adapter layer

#### Scenario: Work time REST adapters implement generated interfaces
- **WHEN** a work time REST adapter is implemented
- **THEN** it implements the generated Java API interface instead of defining a separate handwritten endpoint signature

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

### Requirement: Employee warnings endpoint is defined in the work time contract
The canonical work time REST contract SHALL define an endpoint that returns the authenticated employee's time-booking warnings for a payroll month at `GET /worktime/warnings/{payrollMonth}`. The endpoint MUST require the EMPLOYEE role and MUST resolve the acting employee from the authentication context, without accepting a caller-supplied employee identifier. The response SHALL be a flat array of warnings; each warning SHALL carry a `type` (an identifier from the warning-type vocabulary), a `date`, and an `hours` value. `date` and `hours` SHALL always be present on every warning and SHALL be `null` when the warning carries no date or no quantity. The endpoint SHALL reject an invalid payroll month as a bad request and a caller lacking the EMPLOYEE role as forbidden, consistent with the other work time endpoints. Grouping the returned warnings by type for display is a client concern.

#### Scenario: Authenticated employee retrieves their warnings for a month
- **WHEN** an authenticated employee requests `GET /worktime/warnings/{payrollMonth}` for a valid month
- **THEN** the API resolves the acting employee from the authentication context
- **THEN** the response is a flat array of warnings, each carrying `type`, `date`, and `hours`

#### Scenario: A booking-convention warning is returned with its type and date
- **WHEN** the employee's month contains a billable booking whose note is marked as internal
- **THEN** the response contains a warning with `type` `INTERNAL_NOTE_BILLABLE`, the booking's `date`, and `hours` `null`

#### Scenario: A warning without a quantity reports null hours
- **WHEN** the response contains a warning whose type carries no hours quantity
- **THEN** that warning includes an `hours` field with the value `null`

#### Scenario: A month-level warning reports a null date
- **WHEN** the response contains a warning that applies to the whole month rather than a single day
- **THEN** that warning includes a `date` field with the value `null`

#### Scenario: The endpoint requires no employee identifier
- **WHEN** an authenticated employee requests the warnings endpoint
- **THEN** the request does not require or accept an employee identifier

#### Scenario: Caller without EMPLOYEE role is forbidden
- **WHEN** an authenticated caller without the EMPLOYEE role requests the warnings endpoint
- **THEN** the API rejects the request as forbidden

#### Scenario: Invalid payroll month is rejected
- **WHEN** the warnings endpoint is requested with a malformed payroll month
- **THEN** the API rejects the request as a bad request

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

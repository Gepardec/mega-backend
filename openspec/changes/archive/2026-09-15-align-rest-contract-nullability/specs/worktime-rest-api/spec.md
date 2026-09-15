## MODIFIED Requirements

### Requirement: Employee warnings endpoint is defined in the work time contract
The canonical work time REST contract SHALL define an endpoint that returns the authenticated employee's time-booking warnings for a payroll month at `GET /worktime/warnings/{payrollMonth}`. The endpoint MUST require the EMPLOYEE role and MUST resolve the acting employee from the authentication context, without accepting a caller-supplied employee identifier. The response SHALL be a flat array of warnings; each warning SHALL carry a `type` (the preserved warning-type identifier), a `date`, and an `hours` value. `date` and `hours` SHALL always be present on every warning and SHALL be `null` when the warning carries no date or no quantity. The endpoint SHALL reject an invalid payroll month as a bad request and a caller lacking the EMPLOYEE role as forbidden, consistent with the other work time endpoints. Grouping the returned warnings by type for display is a client concern.

#### Scenario: Authenticated employee retrieves their warnings for a month
- **WHEN** an authenticated employee requests `GET /worktime/warnings/{payrollMonth}` for a valid month
- **THEN** the API resolves the acting employee from the authentication context
- **THEN** the response is a flat array of warnings, each carrying `type`, `date`, and `hours`

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

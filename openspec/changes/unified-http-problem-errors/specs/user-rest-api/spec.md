# Spec Delta

## MODIFIED Requirements

### Requirement: Office management can upload a CSV to bulk-update employee hourly rates
The system SHALL expose a `POST /users/internal-rates` endpoint that accepts a `multipart/form-data` CSV file upload and updates the hourly rate in ZEP for each employee listed in the file. The endpoint SHALL be restricted to the `OFFICE_MANAGEMENT` role. The CSV format SHALL be one row per employee with the fields `zepUsername`, `hourlyRate` and `effectiveFrom` (ISO date), separated by comma or semicolon. Lines beginning with `#` SHALL be treated as comments and ignored. Blank lines SHALL be ignored. On validation failure the response SHALL be `400 Bad Request` with an `application/problem+json` problem body whose `code` identifies the failure. When the failure concerns specific rows, the body SHALL also contain a `lines` array of the offending rows' 1-based line numbers. The body SHALL NOT contain an `errorCode` member.

#### Scenario: All updates succeed
- **WHEN** an `OFFICE_MANAGEMENT` user uploads a valid CSV with one or more employee rows
- **THEN** the system updates the hourly rate for each employee in ZEP
- **THEN** the system returns `200 OK` with no response body

#### Scenario: File is missing or empty
- **WHEN** an `OFFICE_MANAGEMENT` user uploads a CSV containing only comments or blank lines, or uploads no file at all
- **THEN** the system returns `400 Bad Request` with a problem body with `code: "USER_INTERNAL_RATES_EMPTY_FILE"`

#### Scenario: CSV contains format errors
- **WHEN** one or more data rows have an incorrect column count, a non-numeric hourly rate, or an unparseable date
- **THEN** the system returns `400 Bad Request` with a problem body with `code: "USER_INTERNAL_RATES_BAD_FORMAT"` and a `lines` array of the offending rows' 1-based line numbers
- **THEN** no ZEP updates are performed

#### Scenario: CSV references unknown employees
- **WHEN** one or more ZEP usernames in the CSV don't match a known user in the system
- **THEN** the system returns `400 Bad Request` with a problem body with `code: "USER_INTERNAL_RATES_UNKNOWN_USERS"` and a `lines` array of the unrecognised employees' 1-based line numbers
- **THEN** no ZEP updates are performed

#### Scenario: Non-office-management user is rejected
- **WHEN** an authenticated user without the `OFFICE_MANAGEMENT` role calls `POST /users/internal-rates`
- **THEN** the system returns `403 Forbidden`

## ADDED Requirements

### Requirement: Office management can upload a CSV to bulk-update employee hourly rates
The system SHALL expose a `POST /users/internal-rates` endpoint that accepts a `multipart/form-data` CSV file upload and updates the hourly rate for each employee listed in the file in ZEP. The endpoint SHALL be restricted to the `OFFICE_MANAGEMENT` role. The CSV format SHALL be: one row per employee with fields `zepUsername`, `hourlyRate`, `effectiveFrom` (ISO date), separated by comma or semicolon. Lines beginning with `#` SHALL be treated as comments and ignored. Blank lines SHALL be ignored. On validation failure the response SHALL be `400 Bad Request` with `application/json` body `{ "errorCode": "<code>", "lines": [<n>, ...] }`.

#### Scenario: All updates succeed
- **WHEN** an `OFFICE_MANAGEMENT` user uploads a valid CSV with one or more employee rows
- **THEN** the system updates the hourly rate for each employee in ZEP
- **THEN** the system returns `200 OK` with no response body

#### Scenario: File is missing or empty
- **WHEN** an `OFFICE_MANAGEMENT` user uploads a CSV containing only comments, blank lines, or no file at all
- **THEN** the system returns `400 Bad Request` with `errorCode: "EMPTY_FILE"`

#### Scenario: CSV contains format errors
- **WHEN** one or more data rows have an incorrect column count, a non-numeric hourly rate, or an unparseable date
- **THEN** the system returns `400 Bad Request` with `errorCode: "BAD_FORMAT"` and a `lines` array of the 1-based line numbers of the offending rows
- **THEN** no ZEP updates are performed

#### Scenario: CSV references unknown employees
- **WHEN** one or more ZEP usernames in the CSV do not correspond to a known user in the system
- **THEN** the system returns `400 Bad Request` with `errorCode: "UNKNOWN_USERS"` and a `lines` array of the 1-based line numbers of the unrecognised employees
- **THEN** no ZEP updates are performed

#### Scenario: Non-office-management user is rejected
- **WHEN** an authenticated user without the `OFFICE_MANAGEMENT` role calls `POST /users/internal-rates`
- **THEN** the system returns `403 Forbidden`

### Requirement: Hourly rate updates are performed concurrently
The system SHALL process all ZEP hourly-rate updates in a single `POST /users/internal-rates` request concurrently. The system SHALL wait for all updates to complete before responding.

#### Scenario: Multiple employees are updated in parallel
- **WHEN** an `OFFICE_MANAGEMENT` user uploads a valid CSV with multiple employee rows
- **THEN** all ZEP updates are issued concurrently
- **THEN** the system returns `200 OK` only after all updates have completed

### Requirement: Office management can download a CSV template pre-populated with active employees
The system SHALL expose a `GET /users/internal-rates/csv-template` endpoint that returns a `text/csv` file with one row per currently active employee. Each row SHALL contain the employee's ZEP username, an empty hourly-rate field, and today's date as the effective-from date. The endpoint SHALL be restricted to the `OFFICE_MANAGEMENT` role. The response SHALL include `Content-Disposition: attachment; filename="hourly_rates_template.csv"`.

#### Scenario: Template contains all active employees
- **WHEN** an `OFFICE_MANAGEMENT` user calls `GET /users/internal-rates/csv-template`
- **THEN** the system returns `200 OK` with a `text/csv` body
- **THEN** the first line is the comment header `#ZEPMitarbeiterId,neuerStundensatz,gueltigAb YYYY-MM-DD`
- **THEN** each subsequent row has the format `<zepUsername>,,<today's date as YYYY-MM-DD>`, sorted by ZEP username
- **THEN** the response includes `Content-Disposition: attachment; filename="hourly_rates_template.csv"`

#### Scenario: No active employees
- **WHEN** there are no active employees
- **THEN** the system returns `200 OK` with only the comment header line

#### Scenario: Non-office-management user is rejected
- **WHEN** an authenticated user without the `OFFICE_MANAGEMENT` role calls `GET /users/internal-rates/csv-template`
- **THEN** the system returns `403 Forbidden`

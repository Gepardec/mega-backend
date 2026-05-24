# User REST API

## Purpose

Defines the HTTP endpoints exposed by the User bounded context. Covers listing active employees, bulk-updating employee release dates, bulk-updating employee hourly rates via CSV upload, and downloading a CSV template pre-populated with active employees, all restricted to the `OFFICE_MANAGEMENT` role; also covers the authenticated employee self-profile endpoint.

## Requirements

### Requirement: Active employees can be listed by office management
The system SHALL expose a `GET /users/active` endpoint that returns all employees whose employment periods overlap the previous payroll month. The endpoint SHALL be restricted to the `OFFICE_MANAGEMENT` role. The response SHALL include each employee's identifier, full name, email address, and current release date (nullable).

#### Scenario: Office management requests active employee list
- **WHEN** an authenticated `OFFICE_MANAGEMENT` user calls `GET /users/active`
- **THEN** the system returns `200 OK` with a list of employees active during the previous calendar month
- **THEN** each entry includes the employee's identifier, full name, email, and release date

#### Scenario: Non-office-management user is rejected
- **WHEN** an authenticated user without the `OFFICE_MANAGEMENT` role calls `GET /users/active`
- **THEN** the system returns `403 Forbidden`

#### Scenario: No employees active in previous month
- **WHEN** no employees have an employment period overlapping the previous calendar month
- **THEN** the system returns `200 OK` with an empty list

### Requirement: Office management can bulk-update employee release dates
The system SHALL expose a `PUT /users/release-dates` endpoint that accepts a list of `{userId, releaseDate}` entries and updates each employee's release date. The endpoint SHALL be restricted to the `OFFICE_MANAGEMENT` role. The response SHALL be `200 OK` with a `failedUserIds` list identifying employees whose update could not be completed; an empty list indicates full success.

#### Scenario: All release date updates succeed
- **WHEN** an `OFFICE_MANAGEMENT` user submits a valid list of `{userId, releaseDate}` pairs and all updates succeed
- **THEN** the system returns `200 OK` with an empty `failedUserIds` list

#### Scenario: Some release date updates fail
- **WHEN** one or more updates fail for specific employees
- **THEN** the system returns `200 OK` with the identifiers of the failed employees in `failedUserIds`
- **THEN** successfully updated employees are not listed in `failedUserIds`

#### Scenario: Unknown employee identifier
- **WHEN** a submitted `userId` is not found in the system
- **THEN** that identifier is included in `failedUserIds`
- **THEN** processing of other entries continues unaffected

#### Scenario: Non-office-management user is rejected
- **WHEN** an authenticated user without the `OFFICE_MANAGEMENT` role calls `PUT /users/release-dates`
- **THEN** the system returns `403 Forbidden`

### Requirement: Release date updates are executed concurrently and failures are isolated
The system SHALL process all release date updates in a single `PUT /users/release-dates` request concurrently. A failure for one employee SHALL NOT prevent other employees from being updated. The system SHALL wait for all updates to complete before responding.

#### Scenario: Failure for one employee does not abort others
- **WHEN** a `PUT /users/release-dates` request contains multiple employees and one update fails
- **THEN** the remaining employees are still processed
- **THEN** the response reflects the partial outcome via `failedUserIds`

### Requirement: Authenticated employee can retrieve their own profile
The system SHALL expose a `GET /users/me` endpoint that returns the profile of the currently authenticated user. The endpoint SHALL be restricted to the `EMPLOYEE` role. The response SHALL include the user's identifier (UUID), email address, full name, ZEP username, release date (nullable), roles, and Personio ID (nullable).

#### Scenario: Employee retrieves their own profile
- **WHEN** an authenticated `EMPLOYEE` user calls `GET /users/me`
- **THEN** the system returns `200 OK` with the user's profile
- **THEN** the response includes `id` (UUID), `email`, `fullName`, `zepUsername`, `releaseDate` (nullable date), `roles` (array of role strings), and `personioId` (nullable integer)

#### Scenario: Unauthenticated request is rejected
- **WHEN** a request without a valid authentication token calls `GET /users/me`
- **THEN** the system returns `401 Unauthorized`

#### Scenario: Authenticated user without EMPLOYEE role is rejected
- **WHEN** an authenticated user without the `EMPLOYEE` role calls `GET /users/me`
- **THEN** the system returns `403 Forbidden`

#### Scenario: User has no release date set
- **WHEN** an authenticated `EMPLOYEE` user calls `GET /users/me` and their release date has not been set
- **THEN** the system returns `200 OK` with `releaseDate` as `null`

#### Scenario: User has no Personio ID
- **WHEN** an authenticated `EMPLOYEE` user calls `GET /users/me` and their Personio ID is not available
- **THEN** the system returns `200 OK` with `personioId` as `null`

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

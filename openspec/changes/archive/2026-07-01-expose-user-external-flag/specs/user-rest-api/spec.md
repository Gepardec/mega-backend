## MODIFIED Requirements

### Requirement: Authenticated employee can retrieve their own profile
The system SHALL expose a `GET /users/me` endpoint that returns the profile of the currently authenticated user. The endpoint SHALL be restricted to the `EMPLOYEE` role. The response SHALL include the user's identifier (UUID), email address, full name, ZEP username, release date (nullable), roles, Personio ID (nullable), and a boolean `isExternal` flag indicating whether the user is an external employee.

#### Scenario: Employee retrieves their own profile
- **WHEN** an authenticated `EMPLOYEE` user calls `GET /users/me`
- **THEN** the system returns `200 OK` with the user's profile
- **THEN** the response includes `id` (UUID), `email`, `fullName`, `zepUsername`, `releaseDate` (nullable date), `roles` (array of role strings), `personioId` (nullable integer), and `isExternal` (boolean)

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

#### Scenario: Internal user is identified correctly
- **WHEN** an authenticated `EMPLOYEE` user whose ZEP username does not start with "e" calls `GET /users/me`
- **THEN** the system returns `200 OK` with `isExternal: false`

#### Scenario: External user is identified correctly
- **WHEN** an authenticated `EMPLOYEE` user whose ZEP username starts with "e" calls `GET /users/me`
- **THEN** the system returns `200 OK` with `isExternal: true`

## MODIFIED Requirements

### Requirement: Warning rules are identified by a fixed vocabulary
Each warning rule SHALL have a stable identifier from this vocabulary: `CORE_WORKING_TIME`, `TIME_OVERLAP`, `HOLIDAY`, `WEEKEND`, `DOCTOR_APPOINTMENT`, `MAX_DAILY_WORKING_TIME`, `REST_TIME`, `BREAK_TIME`, `JOURNEY_PAIRING`, `JOURNEY_WORKING_LOCATION`, `LOCATION_PROJECT_RELEVANCE`, `INTERNAL_NOTE`, `NO_TIME_ENTRY`, `TRAVEL_ALLOWANCE`, `MEAL_ENTRY`. A rule SHALL cover every warning type it produces.

#### Scenario: A rule producing several warning types is configured as one
- **WHEN** the `INTERNAL_NOTE` rule is disabled
- **THEN** neither `INTERNAL_NOTE_BILLABLE` nor `INTERNAL_NOTE_MISSING` warnings are produced

#### Scenario: The removed billable-project rule is not part of the vocabulary
- **WHEN** an administrator requests `GET /worktime/warning-rules`
- **THEN** no entry has `rule` `BILLABLE_PROJECT`

### Requirement: Initial rule settings preserve the previous behaviour
On first deployment, the `INTERNAL_NOTE`, `MEAL_ENTRY` and `TRAVEL_ALLOWANCE` rules SHALL be enabled with `activeFrom` 2026-10-01. All other rules SHALL be enabled without `activeFrom`. On an installation that still holds a setting for the removed `BILLABLE_PROJECT` rule, the upgrade SHALL delete that setting and SHALL leave the settings of all other rules unchanged.

#### Scenario: A new rule starts from 2026-10-01
- **WHEN** the rule settings are listed after the first deployment
- **THEN** `MEAL_ENTRY` is enabled with `activeFrom` 2026-10-01

#### Scenario: An existing rule starts without a start date
- **WHEN** the rule settings are listed after the first deployment
- **THEN** `TIME_OVERLAP` is enabled and has no `activeFrom`

#### Scenario: The removed rule's setting is deleted on upgrade
- **WHEN** an installation holding a `BILLABLE_PROJECT` setting and an administrator-changed `INTERNAL_NOTE` setting is upgraded and the rule settings are listed
- **THEN** the list contains no `BILLABLE_PROJECT` entry, exactly one entry per remaining rule, and the `INTERNAL_NOTE` entry keeps its changed values

#### Scenario: Warnings keep working after the upgrade
- **WHEN** an employee requests their warnings after the upgrade
- **THEN** the response is successful

### Requirement: An enabled rule applies only from its start date
When a rule is enabled and has `activeFrom`, the warnings evaluation SHALL produce no warnings of that rule dated before `activeFrom`. Warnings on or after `activeFrom` SHALL be produced as the rule defines. An enabled rule without `activeFrom` SHALL apply to all dates.

#### Scenario: A warning before the start date is suppressed
- **WHEN** the `INTERNAL_NOTE` rule has `activeFrom` 2026-11-01 and a billable booking on 2026-10-15 carries the note "Intern - JFX"
- **THEN** no `INTERNAL_NOTE_BILLABLE` warning is produced for 2026-10-15

#### Scenario: A warning on the start date is produced
- **WHEN** the `INTERNAL_NOTE` rule has `activeFrom` 2026-11-01 and a billable booking on 2026-11-01 carries the note "Intern - JFX"
- **THEN** an `INTERNAL_NOTE_BILLABLE` warning is produced for 2026-11-01

#### Scenario: A rule without a start date applies to every date
- **WHEN** the `TIME_OVERLAP` rule is enabled without `activeFrom` and two bookings on 2025-03-03 overlap
- **THEN** a `TIME_OVERLAP` warning is produced for 2025-03-03

### Requirement: Administrators can update a rule's setting
The canonical REST contract SHALL define `PUT /worktime/warning-rules/{rule}` with a body of `enabled` (required) and `activeFrom` (an ISO date or `null`). It SHALL replace the setting and return the updated entry. It MUST require the `ADMIN` role. An unknown rule SHALL be rejected with 404, and a missing `enabled` or malformed `activeFrom` with 400.

#### Scenario: Administrator moves a rule's start date
- **WHEN** an administrator sends `PUT /worktime/warning-rules/MEAL_ENTRY` with `enabled` true and `activeFrom` "2026-11-01"
- **THEN** the response contains `rule` `MEAL_ENTRY`, `enabled` true and `activeFrom` "2026-11-01"
- **THEN** a subsequent `GET /worktime/warning-rules` shows the same values for `MEAL_ENTRY`

#### Scenario: Administrator removes a rule's start date
- **WHEN** an administrator sends `PUT /worktime/warning-rules/INTERNAL_NOTE` with `enabled` true and `activeFrom` null
- **THEN** the `INTERNAL_NOTE` rule applies to all dates

#### Scenario: Unknown rule is rejected
- **WHEN** an administrator sends `PUT /worktime/warning-rules/UNKNOWN_RULE`
- **THEN** the API rejects the request with 404 Not Found

#### Scenario: The removed billable-project rule is rejected as unknown
- **WHEN** an administrator sends `PUT /worktime/warning-rules/BILLABLE_PROJECT` with `enabled` true
- **THEN** the API rejects the request with 404 Not Found

#### Scenario: Missing enabled flag is rejected
- **WHEN** an administrator sends `PUT /worktime/warning-rules/MEAL_ENTRY` without `enabled`
- **THEN** the API rejects the request with 400 Bad Request

#### Scenario: Non-administrator cannot update a rule
- **WHEN** an authenticated caller without the `ADMIN` role sends `PUT /worktime/warning-rules/MEAL_ENTRY`
- **THEN** the API rejects the request with 403 Forbidden

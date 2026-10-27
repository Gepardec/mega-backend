# worktime-warning-rule-settings Specification

## Purpose
Lets administrators switch individual time-booking warning rules on or off and set the date from which each rule applies, at runtime and without a redeploy.

## Requirements

### Requirement: Warning rules are identified by a fixed vocabulary
Each warning rule SHALL have a stable identifier from this vocabulary: `CORE_WORKING_TIME`, `TIME_OVERLAP`, `HOLIDAY`, `WEEKEND`, `DOCTOR_APPOINTMENT`, `MAX_DAILY_WORKING_TIME`, `REST_TIME`, `BREAK_TIME`, `JOURNEY_PAIRING`, `JOURNEY_WORKING_LOCATION`, `LOCATION_PROJECT_RELEVANCE`, `INTERNAL_NOTE`, `NO_TIME_ENTRY`, `BILLABLE_PROJECT`, `TRAVEL_ALLOWANCE`, `MEAL_ENTRY`. A rule SHALL cover every warning type it produces.

#### Scenario: A rule producing several warning types is configured as one
- **WHEN** the `INTERNAL_NOTE` rule is disabled
- **THEN** neither `INTERNAL_NOTE_BILLABLE` nor `INTERNAL_NOTE_MISSING` warnings are produced

### Requirement: The empty-month warning is not governed by rule settings
The `EMPTY_ENTRY_LIST` warning SHALL NOT be a rule and SHALL NOT be affected by any rule setting.

#### Scenario: An empty month is reported regardless of rule settings
- **WHEN** every rule is disabled and the employee has no bookings in the requested month
- **THEN** the warnings response contains an `EMPTY_ENTRY_LIST` warning

### Requirement: Each warning rule has exactly one setting
The system SHALL persist exactly one setting per warning rule. A setting SHALL consist of `enabled` (boolean) and `activeFrom` (a date, or absent). Settings SHALL survive application restarts.

#### Scenario: Every rule has a setting
- **WHEN** the rule settings are listed
- **THEN** the list contains exactly one entry per rule in the vocabulary

#### Scenario: A changed setting survives a restart
- **WHEN** an administrator updates a rule's setting and the application restarts
- **THEN** the rule's setting reflects the update

### Requirement: Initial rule settings preserve the previous behaviour
On first deployment, the `BILLABLE_PROJECT`, `INTERNAL_NOTE`, `MEAL_ENTRY` and `TRAVEL_ALLOWANCE` rules SHALL be enabled with `activeFrom` 2026-10-01. All other rules SHALL be enabled without `activeFrom`.

#### Scenario: A new rule starts from 2026-10-01
- **WHEN** the rule settings are listed after the first deployment
- **THEN** `MEAL_ENTRY` is enabled with `activeFrom` 2026-10-01

#### Scenario: An existing rule starts without a start date
- **WHEN** the rule settings are listed after the first deployment
- **THEN** `TIME_OVERLAP` is enabled and has no `activeFrom`

### Requirement: A disabled rule produces no warnings
When a rule's setting has `enabled` false, the warnings evaluation SHALL produce no warnings of that rule, regardless of `activeFrom`.

#### Scenario: Disabling a rule suppresses its warnings
- **WHEN** the `WEEKEND` rule is disabled and the employee has a booking on a Saturday
- **THEN** the warnings response contains no `WEEKEND` warning

#### Scenario: A disabled rule keeps its start date
- **WHEN** the `MEAL_ENTRY` rule with `activeFrom` 2026-10-01 is disabled and later enabled again without changing `activeFrom`
- **THEN** the rule applies from 2026-10-01 again

### Requirement: An enabled rule applies only from its start date
When a rule is enabled and has `activeFrom`, the warnings evaluation SHALL produce no warnings of that rule dated before `activeFrom`. Warnings on or after `activeFrom` SHALL be produced as the rule defines. An enabled rule without `activeFrom` SHALL apply to all dates.

#### Scenario: A warning before the start date is suppressed
- **WHEN** the `BILLABLE_PROJECT` rule has `activeFrom` 2026-11-01 and a non-billable booking on 2026-10-15 refers to a billable project
- **THEN** no `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for 2026-10-15

#### Scenario: A warning on the start date is produced
- **WHEN** the `BILLABLE_PROJECT` rule has `activeFrom` 2026-11-01 and a non-billable booking on 2026-11-01 refers to a billable project
- **THEN** a `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for 2026-11-01

#### Scenario: A rule without a start date applies to every date
- **WHEN** the `TIME_OVERLAP` rule is enabled without `activeFrom` and two bookings on 2025-03-03 overlap
- **THEN** a `TIME_OVERLAP` warning is produced for 2025-03-03

### Requirement: Setting changes take effect on the next warnings request
A changed setting SHALL apply to the next warnings request without a redeploy or restart. Warnings are evaluated on request, so the change SHALL also apply when warnings for past months are requested.

#### Scenario: Disabling a rule takes effect immediately
- **WHEN** an employee's warnings contain a `MEAL_ENTRY_MISSING` warning and an administrator disables the `MEAL_ENTRY` rule
- **THEN** the employee's next warnings request for the same month contains no `MEAL_ENTRY_MISSING` warning

#### Scenario: Moving the start date applies to past months
- **WHEN** an administrator sets the `TRAVEL_ALLOWANCE` rule's `activeFrom` to 2026-09-01
- **THEN** warnings requested for September 2026 include travel-allowance warnings from 2026-09-01 onward

### Requirement: A rule without a setting is treated as disabled
If a rule has no persisted setting, the warnings evaluation SHALL treat it as disabled, SHALL still return the warnings of all other rules, and SHALL log the missing setting as a warning.

#### Scenario: A missing setting does not break the warnings response
- **WHEN** the `MEAL_ENTRY` rule has no persisted setting and an employee requests their warnings
- **THEN** the response contains the warnings of all other enabled rules and no `MEAL_ENTRY_MISSING` warning

### Requirement: Administrators can list all rule settings
The canonical REST contract SHALL define `GET /worktime/warning-rules`. It SHALL return an array with one entry per rule, each with `rule` (the rule identifier), `enabled`, and `activeFrom` (an ISO date, or `null` when absent). The endpoint MUST require the `ADMIN` role.

#### Scenario: Administrator lists rule settings
- **WHEN** an authenticated caller with the `ADMIN` role requests `GET /worktime/warning-rules`
- **THEN** the response is an array containing an entry for every rule, each with `rule`, `enabled`, and `activeFrom`

#### Scenario: Non-administrator cannot list rule settings
- **WHEN** an authenticated caller without the `ADMIN` role requests `GET /worktime/warning-rules`
- **THEN** the API rejects the request with 403 Forbidden

### Requirement: Administrators can update a rule's setting
The canonical REST contract SHALL define `PUT /worktime/warning-rules/{rule}` with a body of `enabled` (required) and `activeFrom` (an ISO date or `null`). It SHALL replace the setting and return the updated entry. It MUST require the `ADMIN` role. An unknown rule SHALL be rejected with 404, and a missing `enabled` or malformed `activeFrom` with 400.

#### Scenario: Administrator moves a rule's start date
- **WHEN** an administrator sends `PUT /worktime/warning-rules/MEAL_ENTRY` with `enabled` true and `activeFrom` "2026-11-01"
- **THEN** the response contains `rule` `MEAL_ENTRY`, `enabled` true and `activeFrom` "2026-11-01"
- **THEN** a subsequent `GET /worktime/warning-rules` shows the same values for `MEAL_ENTRY`

#### Scenario: Administrator removes a rule's start date
- **WHEN** an administrator sends `PUT /worktime/warning-rules/BILLABLE_PROJECT` with `enabled` true and `activeFrom` null
- **THEN** the `BILLABLE_PROJECT` rule applies to all dates

#### Scenario: Unknown rule is rejected
- **WHEN** an administrator sends `PUT /worktime/warning-rules/UNKNOWN_RULE`
- **THEN** the API rejects the request with 404 Not Found

#### Scenario: Missing enabled flag is rejected
- **WHEN** an administrator sends `PUT /worktime/warning-rules/MEAL_ENTRY` without `enabled`
- **THEN** the API rejects the request with 400 Bad Request

#### Scenario: Non-administrator cannot update a rule
- **WHEN** an authenticated caller without the `ADMIN` role sends `PUT /worktime/warning-rules/MEAL_ENTRY`
- **THEN** the API rejects the request with 403 Forbidden

# Spec Delta

## MODIFIED Requirements

### Requirement: Warnings are queryable per employee for a payroll month
The system SHALL provide a use case that returns all time-booking warnings for a given employee and payroll month. Warnings SHALL be derived by running the full set of warning calculators over the employee's detailed time bookings for that month, together with the employee's absences, expected working days, and the billability of the projects the bookings refer to. The acting employee SHALL be resolved before any data is fetched; if no user is found for the given employee and month, the use case SHALL raise the same not-found error used by the other worktime use cases.

#### Scenario: Employee with bookings receives warnings for every rule that triggers
- **WHEN** the warnings use case is invoked for an employee and month whose bookings violate one or more rules
- **THEN** the result contains one warning per triggered rule occurrence, each identified by its warning type and, where applicable, its date and hours

#### Scenario: Employee with no bookings in the month receives a single month-level warning
- **WHEN** the warnings use case is invoked for a month in which the employee has no time bookings
- **THEN** the result contains exactly one warning of type `EMPTY_ENTRY_LIST` and no per-day no-time-entry warnings

#### Scenario: Warnings take the booked projects' billability into account
- **WHEN** the warnings use case is invoked for a month in which the employee made a non-billable booking on a billable project
- **THEN** the result contains a `NON_BILLABLE_ON_BILLABLE_PROJECT` warning for that booking's date

#### Scenario: Unknown employee raises the worktime not-found error
- **WHEN** the warnings use case is invoked for an employee not active in the given month
- **THEN** the worktime user-not-found error is raised

### Requirement: The warning-type vocabulary is preserved from the legacy feature
The system SHALL define the warning-type vocabulary covering the time-related types `OUTSIDE_CORE_WORKING_TIME`, `TIME_OVERLAP`, `NO_TIME_ENTRY`, `EMPTY_ENTRY_LIST`, `HOLIDAY`, `WEEKEND`, `WRONG_DOCTOR_APPOINTMENT`, `EXCESS_WORKING_TIME_PRESENT`, `MISSING_REST_TIME`, `MISSING_BREAK_TIME`, the journey-related types `BACK_MISSING`, `TO_MISSING`, `INVALID_WORKING_LOCATION`, `LOCATION_RELEVANT_SET`, and the booking-convention types `INTERNAL_NOTE_BILLABLE`, `INTERNAL_NOTE_MISSING`, `NON_BILLABLE_ON_BILLABLE_PROJECT`. The type identifiers that exist in the legacy feature SHALL match the legacy identifiers so that existing frontend translation keys continue to resolve unchanged. The booking-convention types have no legacy counterpart and require new frontend translation keys.

#### Scenario: Each calculator emits its preserved warning-type identifier
- **WHEN** a calculator for a rule that exists in the legacy feature produces a warning
- **THEN** the warning's type is one of the preserved identifiers, spelled exactly as in the legacy feature

#### Scenario: Booking-convention rules emit their own identifiers
- **WHEN** the internal-note rules produce a warning
- **THEN** the warning's type is `INTERNAL_NOTE_BILLABLE` or `INTERNAL_NOTE_MISSING`

#### Scenario: The billable-project rule emits its own identifier
- **WHEN** the billable-project rule produces a warning
- **THEN** the warning's type is `NON_BILLABLE_ON_BILLABLE_PROJECT`

## MODIFIED Requirements

### Requirement: Warnings are queryable per employee for a payroll month
The system SHALL provide a use case that returns all time-booking warnings for a given employee and payroll month. Warnings SHALL be derived by running the full set of warning calculators over the employee's detailed time bookings for that month, together with the employee's absences, expected working days, the billability of the projects the bookings refer to, the travel-allowance tasks those projects declare in ZEP, and the employee's meal entries in that month. The acting employee SHALL be resolved before any data is fetched; if no user is found for the given employee and month, the use case SHALL raise the same not-found error used by the other worktime use cases.

#### Scenario: Employee with bookings receives warnings for every rule that triggers
- **WHEN** the warnings use case is invoked for an employee and month whose bookings violate one or more rules
- **THEN** the result contains one warning per triggered rule occurrence, each identified by its warning type and, where applicable, its date and hours

#### Scenario: Employee with no bookings in the month receives a single month-level warning
- **WHEN** the warnings use case is invoked for a month in which the employee has no time bookings
- **THEN** the result contains exactly one warning of type `EMPTY_ENTRY_LIST` and no per-day no-time-entry warnings

#### Scenario: Warnings take the booked projects' billability into account
- **WHEN** the warnings use case is invoked for a month on or after 2026-10-01 that contains billable work, and on one day the employee's only booking is a non-billable booking without an internal marker on a non-billable project
- **THEN** the result contains an `INTERNAL_NOTE_MISSING` warning for that day

#### Scenario: A non-billable booking on a billable project produces no warning of its own
- **WHEN** the warnings use case is invoked for a month in which, on a day without travel allowance, the employee's only booking is a non-billable booking without an internal marker on a billable project, e.g. on the task "Reisepauschale"
- **THEN** the result contains no booking-convention warning for that day

#### Scenario: Warnings take the booked projects' travel-allowance tasks into account
- **WHEN** the warnings use case is invoked for a month in which a day on or after 2026-10-01 carries travel allowance on "Intern" and its only passive journey is booked on a project for which ZEP lists a task "Reisepauschale"
- **THEN** the result contains a `TRAVEL_ALLOWANCE_WRONG_PROJECT` warning for that day

#### Scenario: Warnings take the employee's meal entries into account
- **WHEN** the warnings use case is invoked for a month in which a past trip day on or after 2026-10-01 has no meal entry in ZEP
- **THEN** the result contains a `MEAL_ENTRY_MISSING` warning for that day

#### Scenario: Unknown employee raises the worktime not-found error
- **WHEN** the warnings use case is invoked for an employee not active in the given month
- **THEN** the worktime user-not-found error is raised

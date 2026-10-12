## ADDED Requirements

### Requirement: Warnings are queryable per employee for a payroll month
The system SHALL provide a use case that returns all time-booking warnings for a given employee and payroll month. Warnings SHALL be derived by running the full set of warning calculators over the employee's detailed time bookings for that month, together with the employee's absences and expected working days. The acting employee SHALL be resolved before any data is fetched; if no user is found for the given employee and month, the use case SHALL raise the same not-found error used by the other worktime use cases.

#### Scenario: Employee with bookings receives warnings for every rule that triggers
- **WHEN** the warnings use case is invoked for an employee and month whose bookings violate one or more rules
- **THEN** the result contains one warning per triggered rule occurrence, each identified by its warning type and, where applicable, its date and hours

#### Scenario: Employee with no bookings in the month receives a single month-level warning
- **WHEN** the warnings use case is invoked for a month in which the employee has no time bookings
- **THEN** the result contains exactly one warning of type `EMPTY_ENTRY_LIST` and no per-day no-time-entry warnings

#### Scenario: Unknown employee raises the worktime not-found error
- **WHEN** the warnings use case is invoked for an employee not active in the given month
- **THEN** the worktime user-not-found error is raised

### Requirement: A warning is a typed, dated, quantified fact without localized text
Each warning SHALL be a domain fact carrying a warning type, an optional date, and an optional numeric hours quantity. The backend SHALL NOT compute, store, or return localized warning text; rendering warning messages is a presentation concern. The quantitative warning types — `MISSING_BREAK_TIME`, `MISSING_REST_TIME`, and `EXCESS_WORKING_TIME_PRESENT` — SHALL carry the relevant hours value; all other warning types SHALL carry no hours. The month-level `EMPTY_ENTRY_LIST` warning SHALL carry no date.

#### Scenario: A missing-break warning carries the missing break hours
- **WHEN** the missing-break rule triggers for a date
- **THEN** the produced warning carries type `MISSING_BREAK_TIME`, that date, and the missing break hours

#### Scenario: A non-quantitative warning carries a date and no hours
- **WHEN** the weekend rule triggers for a date
- **THEN** the produced warning carries type `WEEKEND`, that date, and no hours

#### Scenario: The empty-entry-list warning carries no date
- **WHEN** the `EMPTY_ENTRY_LIST` warning is produced
- **THEN** it carries the type only, with no date and no hours

### Requirement: The warning-type vocabulary is preserved from the legacy feature
The system SHALL define the warning-type vocabulary covering the time-related types `OUTSIDE_CORE_WORKING_TIME`, `TIME_OVERLAP`, `NO_TIME_ENTRY`, `EMPTY_ENTRY_LIST`, `HOLIDAY`, `WEEKEND`, `WRONG_DOCTOR_APPOINTMENT`, `EXCESS_WORKING_TIME_PRESENT`, `MISSING_REST_TIME`, `MISSING_BREAK_TIME` and the journey-related types `BACK_MISSING`, `TO_MISSING`, `INVALID_WORKING_LOCATION`, `LOCATION_RELEVANT_SET`. These type identifiers SHALL match the legacy identifiers so that existing frontend translation keys continue to resolve unchanged.

#### Scenario: Each calculator emits its preserved warning-type identifier
- **WHEN** a calculator produces a warning
- **THEN** the warning's type is one of the preserved identifiers, spelled exactly as in the legacy feature

### Requirement: Calculators preserve legacy behaviour and run as pure functions
The migrated calculators SHALL produce the same warnings as the legacy calculators for equivalent inputs; each calculator's decision logic SHALL remain unchanged, with only its inputs, outputs, and warning construction adapted. Calculators SHALL NOT read the wall clock, employee master data, or external systems directly — every such input SHALL be supplied by the application layer. The current date, where a rule needs it, SHALL be supplied from an injected clock.

#### Scenario: Equivalent inputs yield the legacy result
- **WHEN** a calculator is run over bookings (and, where relevant, expected working days, absences, and a current date) equivalent to a legacy scenario
- **THEN** the produced set of warnings equals the warnings the legacy calculator produced for that scenario

#### Scenario: The current date comes from the injected clock
- **WHEN** the no-time-entry rule excludes future days
- **THEN** it excludes the current date and later days using the current date supplied by the injected clock, not the process wall-clock time

### Requirement: No-time-entry warnings are a set difference over expected working days
The system SHALL determine no-time-entry warnings as the employee's expected working days for the month, minus days that carry any booking, minus days excused by an absence, minus the current date and later days. Expected working days SHALL be the office-calendar working days of the month that fall on or after the active employment-period start and on weekdays where the employee has non-zero regular working hours. Home-office absences SHALL NOT excuse a missing entry. This rule SHALL only run when the employee has at least one booking in the month; an empty month yields the single `EMPTY_ENTRY_LIST` warning instead.

#### Scenario: A past working day with no booking and no absence is flagged
- **WHEN** an expected working day before the current date has neither a booking nor an excusing absence
- **THEN** a `NO_TIME_ENTRY` warning is produced for that day

#### Scenario: A day excused by an absence is not flagged
- **WHEN** an expected working day is covered by a vacation, sick-leave, or other non-home-office absence
- **THEN** no no-time-entry warning is produced for that day

#### Scenario: A future working day is not flagged
- **WHEN** an expected working day falls after the current date
- **THEN** no no-time-entry warning is produced for that day

#### Scenario: Days outside the expected working set are not flagged
- **WHEN** a day is a holiday, a weekend, a zero-regular-hours weekday, or before the employment-period start
- **THEN** it is not part of the expected working days and yields no no-time-entry warning

### Requirement: Warnings from all calculators are assembled into one flat result
The system SHALL combine the outputs of all calculators into a single flat collection of warnings for the employee and month. Assembly SHALL NOT bake localized text and SHALL NOT collapse warnings of different types that fall on the same date. Grouping warnings by type for display is a client concern.

#### Scenario: Different warning types on the same date remain separate
- **WHEN** two calculators produce warnings of different types for the same date
- **THEN** the assembled result contains both warnings as separate entries

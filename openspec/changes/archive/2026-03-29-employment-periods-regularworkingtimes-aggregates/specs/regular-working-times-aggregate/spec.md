## ADDED Requirements

### Requirement: RegularWorkingTimes aggregate encapsulates a list of regular working times
The system SHALL provide a `RegularWorkingTimes` record in `com.gepardec.mega.hexagon.user.domain.model` that wraps a `List<RegularWorkingTime>` and exposes temporal query methods. It SHALL provide a static `empty()` factory and a convenience single-entry constructor. It SHALL NOT depend on `commons-collections4`; standard Java list operations SHALL be used instead.

#### Scenario: Empty aggregate is created
- **WHEN** `RegularWorkingTimes.empty()` is called
- **THEN** a `RegularWorkingTimes` with an empty list is returned

#### Scenario: Single-entry convenience constructor
- **WHEN** `new RegularWorkingTimes(singleEntry)` is called
- **THEN** a `RegularWorkingTimes` containing exactly that one entry is returned

### Requirement: RegularWorkingTimes.latest() returns the entry with the most recent start date
The aggregate SHALL return the working time entry with the latest `start` date. If no entries exist, an empty `Optional` SHALL be returned.

#### Scenario: Multiple entries - returns latest by start date
- **WHEN** `latest()` is called on an aggregate with entries of different start dates
- **THEN** the entry with the most recent start date is returned

#### Scenario: Empty aggregate - returns empty optional
- **WHEN** `latest()` is called on an empty `RegularWorkingTimes`
- **THEN** `Optional.empty()` is returned

### Requirement: RegularWorkingTimes.active(LocalDate) returns the entry effective on the given date
The aggregate SHALL return the working time entry effective on `referenceDate`. The entry whose `start` is the latest date on or before the reference date SHALL be returned. If the only entry has a null `start`, it is treated as effective from the beginning of time and SHALL be returned for any reference date. Entries with a null `start` are excluded from comparison when multiple entries exist. If no entry qualifies, `Optional.empty()` SHALL be returned.

#### Scenario: Single entry with null start - always active
- **WHEN** `active(referenceDate)` is called and only one entry exists with a null start
- **THEN** that entry is returned regardless of reference date

#### Scenario: Multiple entries, one with null start - null start excluded from selection
- **WHEN** `active(referenceDate)` is called and multiple entries exist including one with a null start
- **THEN** the entry with the latest non-null start on or before the reference date is returned

#### Scenario: Reference date is after a start date
- **WHEN** `active(referenceDate)` is called and multiple entries have starts before the reference date
- **THEN** the entry with the latest start on or before the reference date is returned

#### Scenario: Reference date equals start date
- **WHEN** `active(referenceDate)` is called with a date equal to an entry's start
- **THEN** that entry is returned

#### Scenario: Reference date before all non-null start dates
- **WHEN** `active(referenceDate)` is called and all entries have starts after the reference date
- **THEN** `Optional.empty()` is returned

#### Scenario: Multiple entries with null start
- **WHEN** `active(referenceDate)` is called and multiple entries have null start dates (and no non-null starts qualify)
- **THEN** `Optional.empty()` is returned

#### Scenario: Entry starting mid-month not found at month start
- **WHEN** `active(referenceDate)` is called on the first day of a month and an entry starts on the 18th of that month
- **THEN** `Optional.empty()` is returned for that reference date

### Requirement: RegularWorkingTimes.active(YearMonth) returns the entry effective during the given month
The aggregate SHALL return the working time entry effective for a payroll month by checking the first day of the month, falling back to the last day of the month if no entry is active on the first day.

#### Scenario: Entry starting mid-month is found via end-of-month fallback
- **WHEN** `active(yearMonth)` is called and an entry starts after the first day but on or before the last day of that month
- **THEN** that entry is returned via the end-of-month check

#### Scenario: No entry effective in month
- **WHEN** `active(yearMonth)` is called and no entry starts on or before any day in that month
- **THEN** `Optional.empty()` is returned

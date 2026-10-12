## ADDED Requirements

### Requirement: EmploymentPeriods aggregate encapsulates a list of employment periods
The system SHALL provide an `EmploymentPeriods` record in `com.gepardec.mega.hexagon.user.domain.model` that wraps a `List<EmploymentPeriod>` and exposes temporal query methods. It SHALL provide a static `empty()` factory and a convenience single-period constructor.

#### Scenario: Empty aggregate is created
- **WHEN** `EmploymentPeriods.empty()` is called
- **THEN** an `EmploymentPeriods` with an empty list is returned

#### Scenario: Single-period convenience constructor
- **WHEN** `new EmploymentPeriods(singlePeriod)` is called
- **THEN** an `EmploymentPeriods` containing exactly that one period is returned

### Requirement: EmploymentPeriods.latest() returns the period with the most recent start date
The aggregate SHALL return the employment period with the latest `start` date when `latest()` is called. If no periods exist, an empty `Optional` SHALL be returned.

#### Scenario: Multiple periods - returns latest by start date
- **WHEN** `latest()` is called on an aggregate with multiple periods of different start dates
- **THEN** the period with the most recent start date is returned

#### Scenario: Empty aggregate - returns empty optional
- **WHEN** `latest()` is called on an empty `EmploymentPeriods`
- **THEN** `Optional.empty()` is returned

### Requirement: EmploymentPeriods.active(LocalDate) returns the period active on the given date
The aggregate SHALL return the employment period active on `referenceDate`. A period is active if its `start` is on or before the reference date AND its `end` is null or on or after the reference date. If multiple active periods exist, the one with the latest `start` SHALL be returned. Periods with a null `start` are never active.

#### Scenario: Reference date within a bounded period
- **WHEN** `active(referenceDate)` is called and the reference date falls within a period with both start and end
- **THEN** that period is returned

#### Scenario: Reference date within an open-ended period
- **WHEN** `active(referenceDate)` is called and the reference date is after a period's start with no end date
- **THEN** that open-ended period is returned

#### Scenario: Reference date before all periods
- **WHEN** `active(referenceDate)` is called and the reference date is before all period start dates
- **THEN** `Optional.empty()` is returned

#### Scenario: Reference date after a closed period
- **WHEN** `active(referenceDate)` is called and the reference date is after the period's end date
- **THEN** `Optional.empty()` is returned

#### Scenario: Reference date equals start date
- **WHEN** `active(referenceDate)` is called with a date equal to a period's start
- **THEN** that period is returned

#### Scenario: Reference date equals end date
- **WHEN** `active(referenceDate)` is called with a date equal to a period's end
- **THEN** that period is returned

#### Scenario: Period with null start is never active
- **WHEN** `active(referenceDate)` is called and only period has a null start
- **THEN** `Optional.empty()` is returned

#### Scenario: Multiple active periods - returns latest by start
- **WHEN** `active(referenceDate)` is called and multiple periods are active
- **THEN** the period with the latest start date is returned

### Requirement: EmploymentPeriods.active(YearMonth) returns the period active during the given month
The aggregate SHALL return the active period for a payroll month by checking the first day of the month, falling back to the last day of the month if no period is active on the first day.

#### Scenario: Period active on first day of month
- **WHEN** `active(yearMonth)` is called and a period covers the first day of that month
- **THEN** that period is returned

#### Scenario: Period starting mid-month is found via end-of-month fallback
- **WHEN** `active(yearMonth)` is called and a period starts after the first day but before or on the last day of that month
- **THEN** that period is returned via the end-of-month check

#### Scenario: No period active in month
- **WHEN** `active(yearMonth)` is called and no period covers any day in that month
- **THEN** `Optional.empty()` is returned

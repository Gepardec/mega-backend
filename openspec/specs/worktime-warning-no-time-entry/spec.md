# worktime-warning-no-time-entry Specification

## Purpose
Defines when an expected working day without any booking is flagged with `NO_TIME_ENTRY`, based on the employee's expected working days, absences and the current date.

## Requirements

### Requirement: No-time-entry warnings are a set difference over expected working days
The system SHALL determine no-time-entry warnings as the employee's expected working days for the month, minus days that carry any booking, minus days excused by an absence, minus the current date and later days. Expected working days SHALL be the office-calendar working days of the month that fall on or after the active employment-period start and on weekdays where the employee has non-zero regular working hours. Home-office absences SHALL NOT excuse a missing entry. This rule SHALL only run when the employee has at least one booking in the month; an empty month yields the single `EMPTY_ENTRY_LIST` warning instead.

#### Scenario: A past working day with no booking and no absence is flagged
- **WHEN** an expected working day before the current date has neither a booking nor an excusing absence
- **THEN** a `NO_TIME_ENTRY` warning is produced for that day

#### Scenario: A day excused by an absence is not flagged
- **WHEN** an expected working day is covered by a vacation, sick-leave, or other non-home-office absence
- **THEN** no no-time-entry warning is produced for that day

#### Scenario: A home-office day without a booking is flagged
- **WHEN** an expected working day before the current date is covered only by a home-office absence and has no booking
- **THEN** a `NO_TIME_ENTRY` warning is produced for that day

#### Scenario: A future working day is not flagged
- **WHEN** an expected working day falls after the current date
- **THEN** no no-time-entry warning is produced for that day

#### Scenario: Days outside the expected working set are not flagged
- **WHEN** a day is a holiday, a weekend, a zero-regular-hours weekday, or before the employment-period start
- **THEN** it is not part of the expected working days and yields no no-time-entry warning

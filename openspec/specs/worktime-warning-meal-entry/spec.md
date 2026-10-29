# worktime-warning-meal-entry Specification

## Purpose
Defines which days of a payroll month are trip days based on the employee's journeys, and when a trip day without a meal entry is flagged with `MEAL_ENTRY_MISSING`, reminding employees to record which meals the employer covered.

## Requirements

### Requirement: A closed trip makes every day from outbound to return a trip day
The system SHALL evaluate the journeys of the payroll month in chronological order, regardless of vehicle and project. An outbound journey SHALL open a trip, an onward journey SHALL continue an open trip, and a return journey SHALL close it. Every date from the outbound journey through the return journey, both inclusive, SHALL be a trip day, including dates that carry no journey.

#### Scenario: A day trip has one trip day
- **WHEN** the month contains an outbound journey and a return journey on 2026-10-14
- **THEN** 2026-10-14 is the only trip day

#### Scenario: The days between outbound and return are trip days
- **WHEN** the month contains an outbound journey on 2026-10-12 and a return journey on 2026-10-15
- **THEN** 2026-10-12, 2026-10-13, 2026-10-14 and 2026-10-15 are trip days

#### Scenario: An onward journey continues the trip
- **WHEN** the month contains an outbound journey on 2026-10-12, an onward journey on 2026-10-13 and a return journey on 2026-10-15
- **THEN** every date from 2026-10-12 through 2026-10-15 is a trip day

#### Scenario: Days between two trips are not trip days
- **WHEN** one trip runs from 2026-10-05 to 2026-10-06 and another from 2026-10-08 to 2026-10-09
- **THEN** 2026-10-07 is not a trip day

### Requirement: Every day with a journey is a trip day
Every date that carries at least one journey SHALL be a trip day, whether or not the journey belongs to a properly closed trip.

#### Scenario: A return journey in the middle of the month without an outbound journey
- **WHEN** a trip from 2026-10-05 to 2026-10-06 is followed by a return journey on 2026-10-20 without a new outbound journey
- **THEN** 2026-10-20 is a trip day and the dates between 2026-10-06 and 2026-10-20 are not

### Requirement: A trip still open at the end of the month runs until the end of the month
When the last trip of the month is opened and not closed by a return journey, every date from its outbound journey through the last day of the month SHALL be a trip day.

#### Scenario: A trip crossing into the next month
- **WHEN** the month's last journey is an outbound journey on 2026-10-29
- **THEN** 2026-10-29, 2026-10-30 and 2026-10-31 are trip days

#### Scenario: A trip with an onward journey left open
- **WHEN** the month ends with an outbound journey on 2026-10-27 and an onward journey on 2026-10-28
- **THEN** every date from 2026-10-27 through 2026-10-31 is a trip day

### Requirement: A trip continued from the previous month starts on the first day of the month
When the first journey of the month is an onward or return journey, the system SHALL treat it as continuing a trip that started before the month. Every date from the first day of the month through that trip's return journey SHALL be a trip day, or through the last day of the month if the trip is not closed within the month.

#### Scenario: A return journey at the start of the month
- **WHEN** the month's first journey is a return journey on 2026-11-03
- **THEN** 2026-11-01, 2026-11-02 and 2026-11-03 are trip days

#### Scenario: An onward journey at the start of the month followed by a return
- **WHEN** the month's first journey is an onward journey on 2026-11-02, followed by a return journey on 2026-11-04
- **THEN** every date from 2026-11-01 through 2026-11-04 is a trip day

### Requirement: A trip interrupted by a new outbound journey only counts its journey days
When an open trip is followed by another outbound journey without a return journey in between, the interrupted trip SHALL contribute only the dates that carry one of its journeys. The new outbound journey SHALL open a new trip.

#### Scenario: A forgotten return journey
- **WHEN** the month contains an outbound journey on 2026-10-05, another outbound journey on 2026-10-12 and a return journey on 2026-10-13
- **THEN** 2026-10-05, 2026-10-12 and 2026-10-13 are trip days and the dates from 2026-10-06 through 2026-10-11 are not

### Requirement: A trip day without a meal entry is reported
The system SHALL produce a `MEAL_ENTRY_MISSING` warning for every checked trip day for which ZEP holds no meal entry of the employee. A meal entry SHALL satisfy its date regardless of which of breakfast, lunch and dinner it marks, including none of them. The warning SHALL carry the date and no hours. Meal entries of other dates SHALL NOT count.

#### Scenario: A trip day with a meal entry is not reported
- **WHEN** a trip day has a meal entry with breakfast, lunch and dinner set
- **THEN** no `MEAL_ENTRY_MISSING` warning is produced for that day

#### Scenario: A meal entry with no meal set still counts
- **WHEN** a trip day has a meal entry with breakfast, lunch and dinner all unset
- **THEN** no `MEAL_ENTRY_MISSING` warning is produced for that day

#### Scenario: A trip day without a meal entry is reported
- **WHEN** a trip from 2026-10-12 to 2026-10-14 has meal entries for 2026-10-12 and 2026-10-14 only
- **THEN** a `MEAL_ENTRY_MISSING` warning for 2026-10-13 without hours is produced and none for the other two days

#### Scenario: Meal entries on days that are not trip days are ignored
- **WHEN** the employee has a meal entry for a day without a trip and no journeys in the month
- **THEN** no `MEAL_ENTRY_MISSING` warning is produced

### Requirement: Only past trip days from the rule's configured start date are checked
The meal-entry rule SHALL be the `MEAL_ENTRY` warning rule and SHALL be governed by its rule setting. It SHALL only check trip days before the current date. When the setting has an `activeFrom` date, trip days before it SHALL NOT trigger `MEAL_ENTRY_MISSING`. The current date and later days SHALL NOT trigger it. The initial `activeFrom` is 2026-10-01.

#### Scenario: A trip day before the configured start date is not checked
- **WHEN** the `MEAL_ENTRY` rule has `activeFrom` 2026-10-01 and 2026-09-30 is a trip day without a meal entry
- **THEN** no `MEAL_ENTRY_MISSING` warning is produced for 2026-09-30

#### Scenario: A trip day on the configured start date is checked
- **WHEN** the `MEAL_ENTRY` rule has `activeFrom` 2026-10-01, 2026-10-01 is a trip day without a meal entry, and the current date is later
- **THEN** a `MEAL_ENTRY_MISSING` warning for 2026-10-01 is produced

#### Scenario: The current date is not checked
- **WHEN** the current date is a trip day without a meal entry
- **THEN** no `MEAL_ENTRY_MISSING` warning is produced for the current date

#### Scenario: Future trip days are not checked
- **WHEN** a trip open at the end of the month extends past the current date
- **THEN** no `MEAL_ENTRY_MISSING` warning is produced for the days after the current date

#### Scenario: A disabled rule checks no trip days
- **WHEN** the `MEAL_ENTRY` rule is disabled and a past trip day on or after 2026-10-01 has no meal entry
- **THEN** no `MEAL_ENTRY_MISSING` warning is produced

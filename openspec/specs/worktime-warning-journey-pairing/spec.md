# worktime-warning-journey-pairing Specification

## Purpose
Defines how journeys of a payroll month form trips and when a journey without a preceding outbound journey (`TO_MISSING`) or a trip without a return journey (`BACK_MISSING`) is flagged.

## Requirements

### Requirement: Journeys form trips of outbound, onward and return journeys
The system SHALL evaluate all journeys of the payroll month in chronological order, regardless of vehicle. An outbound journey SHALL open a trip. An onward journey SHALL continue an open trip. A return journey SHALL close an open trip. The system SHALL produce a `TO_MISSING` warning for the date of every onward or return journey that occurs while no trip is open. The system SHALL produce a `BACK_MISSING` warning when an open trip is followed by another outbound journey or by no further journey in the month; the warning SHALL carry the date of the trip's last journey. Both warnings SHALL carry no hours.

#### Scenario: A complete trip is not flagged
- **WHEN** the month contains an outbound journey followed by a return journey
- **THEN** no `TO_MISSING` or `BACK_MISSING` warning is produced

#### Scenario: A trip with onward journeys is not flagged
- **WHEN** the month contains an outbound journey, an onward journey and a return journey in that order
- **THEN** no `TO_MISSING` or `BACK_MISSING` warning is produced

#### Scenario: A return journey without an outbound journey is flagged
- **WHEN** a return journey occurs while no trip is open
- **THEN** a `TO_MISSING` warning is produced for the return journey's date

#### Scenario: An onward journey without an outbound journey is flagged
- **WHEN** an onward journey occurs while no trip is open
- **THEN** a `TO_MISSING` warning is produced for the onward journey's date

#### Scenario: A trip interrupted by a new outbound journey is flagged
- **WHEN** an outbound journey is followed by another outbound journey without a return journey in between
- **THEN** a `BACK_MISSING` warning is produced for the date of the first trip's last journey

#### Scenario: A trip left open at the end of the month is flagged
- **WHEN** the last journey of the month leaves a trip open
- **THEN** a `BACK_MISSING` warning is produced for that journey's date

### Requirement: Journey pairing is limited to the payroll month
Journey pairing SHALL only consider journeys of the requested payroll month. As a known limitation, a trip crossing a month boundary SHALL produce a `BACK_MISSING` warning in the month it starts and a `TO_MISSING` warning in the month it ends.

#### Scenario: A trip crossing a month boundary is flagged in both months
- **WHEN** an outbound journey on the last day of one month is followed by a return journey on the first day of the next month
- **THEN** the earlier month contains a `BACK_MISSING` warning and the later month contains a `TO_MISSING` warning

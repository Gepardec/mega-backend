# Spec Delta

## Purpose

Defines which working location non-journey bookings must carry during and outside trips, and when a day is flagged with `INVALID_WORKING_LOCATION`.

## ADDED Requirements

### Requirement: Bookings carry the working location of the current trip
The system SHALL evaluate all bookings of the payroll month in chronological order and track an expected working location: initially the main working location; an outbound or onward journey SHALL set it to that journey's working location; a return journey SHALL reset it to the main working location. The system SHALL produce an `INVALID_WORKING_LOCATION` warning for the date of every non-journey booking whose working location differs from the expected working location. Consequently, a working location other than the main working location is only permitted while a trip is active. Journeys themselves SHALL NOT be checked. The warning SHALL carry the date and no hours.

#### Scenario: Work at the trip's location is permitted
- **WHEN** an outbound journey to working location A is followed by a project booking at A and then a return journey
- **THEN** no `INVALID_WORKING_LOCATION` warning is produced

#### Scenario: Work at another location during a trip is flagged
- **WHEN** an outbound journey to working location A is followed by a project booking at the main working location
- **THEN** an `INVALID_WORKING_LOCATION` warning is produced for that booking's date

#### Scenario: Work away from the main location after returning is flagged
- **WHEN** a return journey is followed by a project booking at working location A
- **THEN** an `INVALID_WORKING_LOCATION` warning is produced for that booking's date

#### Scenario: Work away from the main location without an active trip is flagged
- **WHEN** the month contains no journey and a project booking at working location A
- **THEN** an `INVALID_WORKING_LOCATION` warning is produced for that booking's date

#### Scenario: Work at the main location without any journey is permitted
- **WHEN** the month contains no journey and all project bookings are at the main working location
- **THEN** no `INVALID_WORKING_LOCATION` warning is produced

#### Scenario: Several offending bookings on one day produce one warning
- **WHEN** three project bookings on the same day carry a working location other than the expected one
- **THEN** exactly one `INVALID_WORKING_LOCATION` warning is produced for that day

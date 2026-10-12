# worktime-warning-location-project-relevance Specification

## Purpose
Defines when bookings carrying the "location project-relevant" flag are flagged with `LOCATION_RELEVANT_SET`, prompting the employee to clarify billing of travel time with the project lead.

## Requirements

### Requirement: Bookings with the location project-relevant flag are flagged per day
The system SHALL produce a `LOCATION_RELEVANT_SET` warning for every date that carries at least one booking of any kind with the location project-relevant flag set, regardless of the booking's working location. The warning SHALL carry the date and no hours.

#### Scenario: A flagged booking at the main working location is flagged
- **WHEN** a booking at the main working location has the location project-relevant flag set
- **THEN** a `LOCATION_RELEVANT_SET` warning is produced for that date

#### Scenario: Several flagged bookings on one day produce one warning
- **WHEN** two bookings on the same day have the location project-relevant flag set
- **THEN** exactly one `LOCATION_RELEVANT_SET` warning is produced for that day

#### Scenario: Bookings without the flag are not flagged
- **WHEN** no booking on a day has the location project-relevant flag set
- **THEN** no `LOCATION_RELEVANT_SET` warning is produced for that day

# worktime-warning-max-daily-working-time Specification

## Purpose
Defines the maximum daily working time of 10 hours and when a day is flagged with `EXCESS_WORKING_TIME_PRESENT`.

## Requirements

### Requirement: More than 10 hours of working time per day is flagged
The system SHALL produce an `EXCESS_WORKING_TIME_PRESENT` warning for a date when the summed duration of that date's working-time bookings exceeds 10 hours. Working-time bookings are project bookings and journeys travelled as an active traveller; journeys as an inactive traveller do not count as working time. A booking counts in full towards the date it starts on, including a booking ending at 24:00. Exactly 10 hours SHALL NOT trigger the warning. The warning SHALL carry the date and the excess hours (total working time minus 10 hours).

#### Scenario: Exactly 10 hours is permitted
- **WHEN** a day's working-time bookings sum to exactly 10 hours
- **THEN** no `EXCESS_WORKING_TIME_PRESENT` warning is produced for that day

#### Scenario: More than 10 hours is flagged with the excess
- **WHEN** a day's working-time bookings sum to 11.25 hours
- **THEN** an `EXCESS_WORKING_TIME_PRESENT` warning with 1.25 hours is produced for that day

#### Scenario: A booking ending at 24:00 counts in full
- **WHEN** a day's bookings sum to 11.25 hours and the last one ends at 24:00
- **THEN** an `EXCESS_WORKING_TIME_PRESENT` warning with 1.25 hours is produced for that day

#### Scenario: Inactive travel does not count
- **WHEN** a day has 10 hours of project bookings and an additional journey travelled as an inactive traveller
- **THEN** no `EXCESS_WORKING_TIME_PRESENT` warning is produced for that day

# Spec Delta

## Purpose

Defines the minimum break of 30 minutes once a day's working time exceeds 6 hours and when a day is flagged with `MISSING_BREAK_TIME`.

## ADDED Requirements

### Requirement: Less than 30 minutes of break before exceeding 6 hours of work is flagged
The system SHALL evaluate each date's working-time bookings in chronological order, accumulating working time and counting every gap between consecutive working-time bookings as break time. Working-time bookings are project bookings and journeys travelled as an active traveller; journeys as an inactive traveller do not count as working time. At the first booking with which the accumulated working time exceeds 6 hours, the system SHALL produce a `MISSING_BREAK_TIME` warning for that date if the break time accumulated up to that booking is less than 30 minutes. The warning SHALL carry the date and the missing break hours (0.5 hours minus the accumulated break time), rounded to two decimal places. At most one break warning SHALL be produced per date. Exactly 6 hours of working time SHALL NOT require a break.

#### Scenario: Exactly 6 hours without a break is permitted
- **WHEN** a day consists of one working-time booking from 08:00 to 14:00
- **THEN** no `MISSING_BREAK_TIME` warning is produced for that day

#### Scenario: More than 6 hours without a break is flagged
- **WHEN** a day consists of one working-time booking from 08:00 to 15:00
- **THEN** a `MISSING_BREAK_TIME` warning with 0.5 hours is produced for that day

#### Scenario: A partial break is flagged with the remainder
- **WHEN** a day has working-time bookings from 08:00 to 12:00 and from 12:15 to 16:00
- **THEN** a `MISSING_BREAK_TIME` warning with 0.25 hours is produced for that day

#### Scenario: A sufficient break before exceeding 6 hours is permitted
- **WHEN** a day has working-time bookings from 08:00 to 12:00 and from 12:30 to 17:00
- **THEN** no `MISSING_BREAK_TIME` warning is produced for that day

#### Scenario: A break taken only after exceeding 6 hours does not count
- **WHEN** a day has working-time bookings from 08:00 to 14:30 and from 15:00 to 16:00
- **THEN** a `MISSING_BREAK_TIME` warning with 0.5 hours is produced for that day

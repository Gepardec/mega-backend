# Spec Delta

## Purpose

Defines when a day's working time lies outside the core working time of 06:00–22:00 and is flagged with `OUTSIDE_CORE_WORKING_TIME`.

## ADDED Requirements

### Requirement: Working time outside 06:00–22:00 is flagged per day
The system SHALL produce an `OUTSIDE_CORE_WORKING_TIME` warning for a date when, among that date's working-time bookings with a non-zero duration, the first booking starts before 06:00 or the last booking ends after 22:00 on that date. Bookings belong to the date on which they start. Working-time bookings are project bookings and journeys travelled as an active traveller; journeys as an inactive traveller do not count as working time. Zero-duration bookings SHALL be ignored when determining the day's first and last booking. A start at exactly 06:00 and an end at exactly 22:00 SHALL NOT trigger the warning. The warning SHALL carry the date and no hours.

#### Scenario: Work ending at 22:00 is within core working time
- **WHEN** a day's last working-time booking ends at exactly 22:00
- **THEN** no `OUTSIDE_CORE_WORKING_TIME` warning is produced for that day

#### Scenario: Work ending after 22:00 within the same hour is flagged
- **WHEN** a day's last working-time booking ends at 22:30
- **THEN** an `OUTSIDE_CORE_WORKING_TIME` warning is produced for that day

#### Scenario: Work ending at 24:00 is flagged on its start date
- **WHEN** a day's last working-time booking ends at 24:00
- **THEN** an `OUTSIDE_CORE_WORKING_TIME` warning is produced for the date the booking starts on

#### Scenario: Work starting before 06:00 is flagged
- **WHEN** a day's first working-time booking starts at 05:30
- **THEN** an `OUTSIDE_CORE_WORKING_TIME` warning is produced for that day

#### Scenario: Inactive travel outside core working time is not flagged
- **WHEN** the only bookings outside 06:00–22:00 on a day are journeys travelled as an inactive traveller
- **THEN** no `OUTSIDE_CORE_WORKING_TIME` warning is produced for that day

#### Scenario: A zero-duration booking does not hide a violation at the other end
- **WHEN** a day starts with a zero-duration journey at 05:00 and its last working-time booking ends at 23:00
- **THEN** an `OUTSIDE_CORE_WORKING_TIME` warning is produced for that day

#### Scenario: A zero-duration booking outside core working time is not flagged
- **WHEN** the only booking outside 06:00–22:00 on a day has a zero duration
- **THEN** no `OUTSIDE_CORE_WORKING_TIME` warning is produced for that day

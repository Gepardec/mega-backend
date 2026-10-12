# Spec Delta

## Purpose

Defines the minimum rest time of 11 hours between two consecutive working days and when a day is flagged with `MISSING_REST_TIME`.

## ADDED Requirements

### Requirement: Less than 11 hours of rest between consecutive days is flagged
The system SHALL produce a `MISSING_REST_TIME` warning for a date when the employee has working-time bookings on that date and on the preceding calendar day, and the rest time — from the end of the preceding day's last working-time booking to the start of that date's first working-time booking — is less than 11 hours. Working-time bookings are project bookings and journeys travelled as an active traveller; journeys as an inactive traveller do not count as working time. A booking ending at 24:00 ends at the start of the following day. The warning SHALL carry the later date and the missing rest hours (11 hours minus the rest time), rounded to two decimal places. Days that are not directly consecutive SHALL NOT be compared.

#### Scenario: 11 hours of rest is permitted
- **WHEN** work ends at 20:00 and work on the next day starts at 07:00
- **THEN** no `MISSING_REST_TIME` warning is produced

#### Scenario: Insufficient rest is flagged on the later day
- **WHEN** work ends at 22:00 and work on the next day starts at 08:00
- **THEN** a `MISSING_REST_TIME` warning with 1 hour is produced for the later day

#### Scenario: Rest after work ending at 24:00 is measured from midnight
- **WHEN** work ends at 24:00 and work on the next day starts at 07:00
- **THEN** a `MISSING_REST_TIME` warning with 4 hours is produced for the later day

#### Scenario: Non-consecutive days are not compared
- **WHEN** work ends at 23:00 on one day and the next working-time booking is two days later at 06:00
- **THEN** no `MISSING_REST_TIME` warning is produced

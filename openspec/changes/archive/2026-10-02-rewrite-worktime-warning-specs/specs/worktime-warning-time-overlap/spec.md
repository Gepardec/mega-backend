# Spec Delta

## Purpose

Defines when bookings on the same day overlap in time and the day is flagged with `TIME_OVERLAP`.

## ADDED Requirements

### Requirement: Overlapping bookings are flagged per day
The system SHALL produce a `TIME_OVERLAP` warning for a date when any two bookings starting on that date overlap in time. All bookings SHALL be considered, including journeys travelled as an inactive traveller. Bookings that only touch — one ends exactly when the next starts — SHALL NOT count as overlapping. The warning SHALL carry the date and no hours.

#### Scenario: Overlapping bookings are flagged
- **WHEN** one booking runs from 08:00 to 10:00 and another on the same day from 09:30 to 11:00
- **THEN** a `TIME_OVERLAP` warning is produced for that day

#### Scenario: A booking nested inside another is flagged
- **WHEN** one booking runs from 08:00 to 12:00 and another on the same day from 09:00 to 10:00
- **THEN** a `TIME_OVERLAP` warning is produced for that day

#### Scenario: Adjacent bookings are not flagged
- **WHEN** one booking ends at 10:00 and the next booking on the same day starts at 10:00
- **THEN** no `TIME_OVERLAP` warning is produced for that day

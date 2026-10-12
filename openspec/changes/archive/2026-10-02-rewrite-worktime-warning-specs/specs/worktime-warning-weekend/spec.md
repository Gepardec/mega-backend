# Spec Delta

## Purpose

Defines when bookings on Saturdays and Sundays are flagged with `WEEKEND`.

## ADDED Requirements

### Requirement: Bookings on weekends are flagged
The system SHALL produce a `WEEKEND` warning for every Saturday or Sunday that carries at least one booking of any kind. The warning SHALL carry the date and no hours. A weekend day that is also a public holiday SHALL produce both a `WEEKEND` and a `HOLIDAY` warning.

#### Scenario: A booking on a Saturday is flagged
- **WHEN** an employee has a booking on a Saturday
- **THEN** a `WEEKEND` warning is produced for that date

#### Scenario: A booking on a weekday is not flagged
- **WHEN** an employee has bookings only on Monday to Friday
- **THEN** no `WEEKEND` warning is produced

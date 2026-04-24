## MODIFIED Requirements

### Requirement: Scheduled generation is the primary driver of month-end task creation
The system SHALL trigger month-end task generation through a scheduled method on the last **working** day of the month for the current month. The scheduler SHALL fire daily during the last week of the month (days 25–31) and skip execution on any day that is not the last working day, accounting for weekends and Austrian public holidays.

#### Scenario: Scheduled trigger runs on last working day
- **WHEN** the scheduler fires and today is the last working day of the current month
- **THEN** the scheduler invokes month-end task generation for that current `YearMonth`

#### Scenario: Scheduled trigger skips on a non-last-working day
- **WHEN** the scheduler fires and today is not the last working day of the current month
- **THEN** the scheduler exits without invoking month-end task generation

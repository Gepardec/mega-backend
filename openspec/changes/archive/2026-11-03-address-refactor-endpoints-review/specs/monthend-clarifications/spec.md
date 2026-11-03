# Spec Delta

## MODIFIED Requirements

### Requirement: The system actor can create clarifications on behalf of absent employees
The system SHALL provide a `MonthEndClarification.createBySystem()` factory method that creates a clarification with `createdBy` set to `SystemActor.USER_ID`, bypassing the creator rule that normally requires the creator to be the subject employee or an eligible project lead.

#### Scenario: System-created clarification is initialised as open with SystemActor as creator
- **WHEN** `MonthEndClarification.createBySystem()` is called with a valid project, subject employee, eligible leads, and text
- **THEN** a `MonthEndClarification` is created with status `OPEN`
- **THEN** `createdBy` equals `SystemActor.USER_ID`
- **THEN** `sourceSystem` is `MEGA`

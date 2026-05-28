## MODIFIED Requirements

### Requirement: Clarification creator may permanently delete an open clarification
The system SHALL allow the creator of a clarification to permanently remove it from the database while it is `OPEN`, provided the clarification was NOT created by the system actor (`SystemActor.USER_ID`). Deletion of a `DONE` clarification SHALL be rejected. Deletion of a system-created clarification SHALL be rejected regardless of status. No other actor SHALL be permitted to delete a clarification. Deletion SHALL be a hard delete with no audit trail retained.

#### Scenario: Creator deletes their open clarification
- **WHEN** the creator requests deletion of their own open clarification
- **THEN** the clarification is permanently removed from the database

#### Scenario: Creator cannot delete their done clarification
- **WHEN** the creator requests deletion of their own done clarification
- **THEN** the system rejects the deletion attempt

#### Scenario: Non-creator cannot delete a clarification
- **WHEN** an involved party who is not the creator requests deletion of a clarification
- **THEN** the system rejects the deletion attempt

#### Scenario: System-created clarification cannot be deleted by the subject employee
- **WHEN** the subject employee attempts to delete a clarification whose `createdBy` is `SystemActor.USER_ID`
- **THEN** the system rejects the deletion attempt

## ADDED Requirements

### Requirement: The system actor can create clarifications on behalf of absent employees
The system SHALL provide a `MonthEndClarification.createBySystem()` factory method that creates a clarification with `createdBy` set to `SystemActor.USER_ID`, bypassing the `validateCreator` constraint that normally requires the creator to be the subject employee or an eligible project lead.

#### Scenario: System-created clarification is initialised as open with SystemActor as creator
- **WHEN** `MonthEndClarification.createBySystem()` is called with a valid project, subject employee, eligible leads, and text
- **THEN** a `MonthEndClarification` is created with status `OPEN`
- **THEN** `createdBy` equals `SystemActor.USER_ID`
- **THEN** `sourceSystem` is `MEGA`

### Requirement: System-created clarifications are resolvable only by eligible project leads
When a `MonthEndClarification` was created by `SystemActor.USER_ID`, the `canResolve(UserId actorId)` check SHALL return `true` only for actors whose id appears in `eligibleActorIds`. The subject employee SHALL NOT be permitted to resolve it. `SystemActor.USER_ID` SHALL NOT be permitted to resolve it.

#### Scenario: System-created clarification is resolvable by eligible leads only
- **WHEN** a system-created clarification exists for a subject employee
- **THEN** an eligible project lead can resolve it
- **THEN** the subject employee cannot resolve it
- **THEN** `SystemActor.USER_ID` cannot resolve it (it is the creator)

## MODIFIED Requirements

### Requirement: Clarification state changes fire domain events
The system SHALL fire a CDI domain event whenever a `MonthEndClarification` changes state. `ClarificationCreatedEvent`, `ClarificationUpdatedEvent`, `ClarificationCompletedEvent`, and `ClarificationDeletedEvent` SHALL be defined in `hexagon/monthend/domain/event/`. Each event SHALL carry the `MonthEndClarificationId`, `sourceSystem`, creator `UserId`, subject employee `UserId` (optional), `YearMonth`, and `ProjectId`. `ClarificationCompletedEvent` SHALL additionally carry the resolver `UserId`. `ClarificationUpdatedEvent` SHALL carry the `actorId` (editor) and the updated `text`. Events SHALL be Java records.

Events are fired by the application service layer (not the domain model itself) after the repository operation succeeds.

#### Scenario: Clarification creation fires ClarificationCreatedEvent
- **WHEN** `CreateMonthEndClarificationUseCase` successfully persists a new clarification
- **THEN** a `ClarificationCreatedEvent` is fired via CDI carrying the new clarification's id, sourceSystem, creator, subject employee, month, and project

#### Scenario: Clarification text update fires ClarificationUpdatedEvent
- **WHEN** `UpdateMonthEndClarificationUseCase` successfully saves updated clarification text
- **THEN** a `ClarificationUpdatedEvent` is fired via CDI carrying the clarification id, actorId, subject employee, and updated text

#### Scenario: Clarification completion fires ClarificationCompletedEvent
- **WHEN** `CompleteMonthEndClarificationUseCase` successfully marks a clarification as done
- **THEN** a `ClarificationCompletedEvent` is fired via CDI carrying the clarification id and resolver

#### Scenario: Clarification deletion fires ClarificationDeletedEvent
- **WHEN** `DeleteMonthEndClarificationUseCase` successfully deletes a clarification
- **THEN** a `ClarificationDeletedEvent` is fired via CDI carrying the clarification id and original creator

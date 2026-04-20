## ADDED Requirements

### Requirement: ZEP mail processing failure fires ZepMailProcessingFailedEvent
The system SHALL define `ZepMailProcessingFailedEvent` in `hexagon/monthend/domain/event/`. The event SHALL carry the creator `UserId` (optional, populated when the creator could be resolved), creator email (optional), error message, and raw mail content (subject + HTML body). The event SHALL be a Java record.

`CreateClarificationFromZepMailService` SHALL fire this event via CDI whenever a ZEP mail cannot be parsed or its processing raises an exception. The notification BC observes this event to send the `ZEP_CLARIFICATION_PROCESSING_ERROR` mail, consistent with the existing `ClarificationCreated/Updated/Completed/Deleted` event pattern.

#### Scenario: Parse failure fires ZepMailProcessingFailedEvent
- **WHEN** `CreateClarificationFromZepMailService` processes a message that `ZepMailMessageParser` cannot parse
- **THEN** a `ZepMailProcessingFailedEvent` is fired via CDI carrying the error message and raw mail content

#### Scenario: Processing exception fires ZepMailProcessingFailedEvent with creator context
- **WHEN** an exception occurs after the creator user has been resolved
- **THEN** a `ZepMailProcessingFailedEvent` is fired carrying the resolved creator UserId and email alongside the error details

#### Scenario: ZepMailProcessingFailedEvent is observed by the notification BC
- **WHEN** a `ZepMailProcessingFailedEvent` is fired
- **THEN** the notification BC observes the event and sends a `ZEP_CLARIFICATION_PROCESSING_ERROR` mail to the creator if a creator email is present

## MODIFIED Requirements

### Requirement: Clarification state changes fire domain events
The system SHALL fire a CDI domain event whenever a `MonthEndClarification` changes state. `ClarificationCreatedEvent`, `ClarificationUpdatedEvent`, `ClarificationCompletedEvent`, and `ClarificationDeletedEvent` SHALL be defined in `hexagon/monthend/domain/event/`. Each event SHALL carry the `MonthEndClarificationId`, `sourceSystem`, creator `UserId`, subject employee `UserId` (optional), `YearMonth`, and `ProjectId`. `ClarificationCompletedEvent` SHALL additionally carry the resolver `UserId`. `ClarificationUpdatedEvent` SHALL carry the `actorId` (editor) and the updated `text`. Events SHALL be Java records.

`ZepMailProcessingFailedEvent` SHALL also be defined in `hexagon/monthend/domain/event/` (see ADDED requirements above).

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

#### Scenario: ZEP mail processing failure fires ZepMailProcessingFailedEvent
- **WHEN** `CreateClarificationFromZepMailService` fails to process a ZEP mail
- **THEN** a `ZepMailProcessingFailedEvent` is fired via CDI (see ADDED requirements for full contract)

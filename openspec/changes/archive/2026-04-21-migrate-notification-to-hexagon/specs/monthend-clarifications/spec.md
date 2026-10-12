## ADDED Requirements

### Requirement: Clarification state changes fire domain events
The system SHALL fire a CDI domain event whenever a `MonthEndClarification` changes state. `ClarificationCreatedEvent`, `ClarificationCompletedEvent`, and `ClarificationDeletedEvent` SHALL be defined in `hexagon/monthend/domain/event/`. Each event SHALL carry the `MonthEndClarificationId`, `sourceSystem`, creator `UserId`, subject employee `UserId` (optional), `YearMonth`, and `ProjectId`. `ClarificationCompletedEvent` SHALL additionally carry the resolver `UserId`. Events SHALL be Java records.

Events are fired by the application service layer (not the domain model itself) after the repository operation succeeds.

#### Scenario: Clarification creation fires ClarificationCreatedEvent
- **WHEN** `CreateMonthEndClarificationUseCase` successfully persists a new clarification
- **THEN** a `ClarificationCreatedEvent` is fired via CDI carrying the new clarification's id, sourceSystem, creator, subject employee, month, and project

#### Scenario: Clarification completion fires ClarificationCompletedEvent
- **WHEN** `CompleteMonthEndClarificationUseCase` successfully marks a clarification as done
- **THEN** a `ClarificationCompletedEvent` is fired via CDI carrying the clarification id and resolver

#### Scenario: Clarification deletion fires ClarificationDeletedEvent
- **WHEN** `DeleteMonthEndClarificationUseCase` successfully deletes a clarification
- **THEN** a `ClarificationDeletedEvent` is fired via CDI carrying the clarification id and original creator

### Requirement: ZepClarificationMailReceived events are processed into clarifications by a monthend inbound adapter
The system SHALL provide a `ZepClarificationMailAdapter` inbound adapter in `hexagon/monthend/adapter/inbound/` that observes `ZepClarificationMailReceived` via CDI `@Observes`. The adapter SHALL resolve the subject employee via `UserRepository.findByFullName(FullName)` and invoke `CreateMonthEndClarificationUseCase` with `sourceSystem = ZEP`.

A ZEP-sourced clarification SHALL be treated as a project-lead-created clarification regardless of project billability: the creator is the resolved ZEP mail sender acting in a lead capacity, and the subject employee is the employee named in the mail. The clarification text SHALL be the raw HTML table carried in `ZepClarificationMailReceived.message` — no additional formatting is applied, as the table already contains all relevant booking details.

If the subject employee cannot be resolved or the project cannot be found the adapter SHALL throw an exception so that the notification BC's error handling in `ProcessZepMailService` can report the failure to the ZEP mail creator.

#### Scenario: ZepClarificationMailReceived creates a lead-style clarification for the subject employee
- **WHEN** `ZepClarificationMailAdapter` observes a `ZepClarificationMailReceived` event
- **THEN** it creates a `MonthEndClarification` with `sourceSystem = ZEP`, treating the ZEP mail sender as creator and the named employee as subject, equivalent to a project-lead-created clarification

#### Scenario: Unknown subject employee causes exception
- **WHEN** `ZepClarificationMailAdapter` cannot resolve the subject employee by full name
- **THEN** it throws an exception that propagates to the notification BC for error reporting

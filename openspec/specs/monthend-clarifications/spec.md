# Month-End Clarifications

## Purpose

Defines the `MonthEndClarification` aggregate for user-created month-end follow-up subtasks, including visibility, editability, resolution, and their independence from generated `MonthEndTask` obligations.

## Requirements

### Requirement: Month-end clarifications model user-created follow-up subtasks
The system SHALL represent each month-end clarification as a `MonthEndClarification` scoped to one month and one project. A clarification SHALL store its creator (`createdBy`), an optional subject employee (`subjectEmployeeId`), open or done status, clarification text, optional resolution note, eligible project lead snapshot, `createdAt`, optional `resolvedAt`, and `lastModifiedAt`. The `creatorSide` field is removed. When `subjectEmployeeId` is absent the clarification is project-level and the creator MUST be an eligible project lead.

Clarification display enrichment — resolving user identifiers to display references and evaluating actor-specific capability flags — is NOT a domain or application concern. The `MonthEndStatusOverview` aggregate SHALL hold `List<MonthEndClarification>` and SHALL NOT hold a pre-enriched presentation model. No domain or application class SHALL build or hold a `MonthEndOverviewClarificationItem` or equivalent presentation-layer view of a clarification.

#### Scenario: Employee-created clarification is initialized as open
- **WHEN** the subject employee creates a clarification for a project in a month-end context
- **THEN** the system creates one `MonthEndClarification` for that month, project, and subject employee
- **THEN** the clarification status is `OPEN`
- **THEN** `createdBy`, `createdAt`, and `lastModifiedAt` are populated

#### Scenario: Lead-created clarification for subject employee is initialized as open
- **WHEN** an eligible project lead creates a clarification for a project employee in a month-end context
- **THEN** the system creates one `MonthEndClarification` for that month, project, and subject employee
- **THEN** the clarification status is `OPEN`
- **THEN** the clarification stores the creating lead as `createdBy`

#### Scenario: Lead-created project-level clarification is initialized as open
- **WHEN** an eligible project lead creates a clarification with no subject employee
- **THEN** the system creates one `MonthEndClarification` for that month and project with `subjectEmployeeId` absent
- **THEN** the clarification status is `OPEN`
- **THEN** `createdBy`, `createdAt`, and `lastModifiedAt` are populated

#### Scenario: Project-level clarification cannot be created by a non-lead
- **WHEN** a user who is not an eligible project lead attempts to create a project-level clarification (no subject employee)
- **THEN** the system rejects the creation attempt

### Requirement: Clarifications preserve month-end lead eligibility at creation time
The system SHALL snapshot the eligible project leads of a clarification when it is created. Employee-created clarifications SHALL be resolvable by any lead in that snapshot. Lead-created clarifications SHALL remain visible and editable to the leads in that snapshot while open.

#### Scenario: Employee-created clarification captures all eligible leads
- **WHEN** the subject employee creates a clarification and the project has multiple eligible leads in that month-end context
- **THEN** the clarification stores that lead set as its eligible project lead snapshot

#### Scenario: Lead assignment changes do not alter an open clarification
- **WHEN** project lead assignments change after a clarification has already been created
- **THEN** the open clarification keeps its original eligible project lead snapshot

### Requirement: Clarification creation and visibility follow involved-party rules
The system SHALL allow a clarification to be created only by an involved party. The involved parties of a clarification are: the subject employee (if present) and all eligible project leads. A clarification SHALL be visible to all involved parties.

#### Scenario: Employee-created clarification is visible to subject employee and all leads
- **WHEN** the subject employee creates a clarification for a project in their month-end project context
- **THEN** the clarification is visible to that employee
- **THEN** the clarification is visible to every eligible project lead in the clarification's lead snapshot

#### Scenario: Lead-created clarification for employee is visible to subject employee and all leads
- **WHEN** an eligible project lead creates a clarification for a project employee in that month-end context
- **THEN** the clarification is visible to the subject employee
- **THEN** the clarification is visible to every eligible project lead in the clarification's lead snapshot

#### Scenario: Project-level clarification is visible to eligible leads only
- **WHEN** a lead creates a project-level clarification with no subject employee
- **THEN** the clarification is visible to every eligible project lead in the clarification's lead snapshot
- **THEN** no subject employee has visibility because none exists

#### Scenario: Ineligible actor cannot create a clarification
- **WHEN** a user who is not an involved party attempts to create a clarification
- **THEN** the system rejects the creation attempt

### Requirement: Open clarification text is editable only by the creator
The system SHALL allow edits to the main clarification text only while the clarification is `OPEN`, and only by the actor who created the clarification (`createdBy`). All other involved parties SHALL be rejected when attempting to edit. Clarifications with `sourceSystem = ZEP` SHALL never be editable, regardless of actor or status.

#### Scenario: Creator edits their open clarification
- **WHEN** the creator edits the text of their own open clarification
- **THEN** the clarification text is updated
- **THEN** `lastModifiedAt` is updated

#### Scenario: Non-creator involved party cannot edit clarification text
- **WHEN** an involved party who is not the creator attempts to edit the text of an open clarification
- **THEN** the system rejects the edit attempt

#### Scenario: Done clarification text cannot be edited
- **WHEN** any actor attempts to edit the text of a clarification with status `DONE`
- **THEN** the system rejects the edit attempt

#### Scenario: ZEP-sourced clarification text cannot be edited
- **WHEN** any actor attempts to edit the text of a clarification with `sourceSystem = ZEP`
- **THEN** the system rejects the edit attempt

### Requirement: Clarifications are resolved by any involved party except the creator
The system SHALL complete clarifications through a dedicated clarification completion flow. Any involved party who is NOT the creator SHALL be allowed to resolve an open clarification. Resolution SHALL set the clarification status to `DONE`, populate `resolvedBy` and `resolvedAt`, and MAY store a resolution note.

#### Scenario: Eligible lead resolves an employee-created clarification
- **WHEN** an eligible project lead (who is not the creator) resolves an open employee-created clarification
- **THEN** the clarification status becomes `DONE`
- **THEN** `resolvedBy` and `resolvedAt` are populated

#### Scenario: Employee resolves a lead-created clarification with a note
- **WHEN** the subject employee resolves an open lead-created clarification and provides a resolution note
- **THEN** the clarification status becomes `DONE`
- **THEN** the resolution note is stored with the clarification

#### Scenario: Another lead resolves a lead-created clarification for employee
- **WHEN** an eligible project lead who is not the creating lead resolves an open lead-for-employee clarification
- **THEN** the clarification status becomes `DONE`
- **THEN** `resolvedBy` and `resolvedAt` are populated

#### Scenario: Another lead resolves a project-level clarification
- **WHEN** an eligible project lead who is not the creating lead resolves an open project-level clarification
- **THEN** the clarification status becomes `DONE`
- **THEN** `resolvedBy` and `resolvedAt` are populated

#### Scenario: Creator cannot resolve their own clarification
- **WHEN** the creator of a clarification attempts to resolve it
- **THEN** the system rejects the resolution attempt

#### Scenario: Ineligible actor cannot resolve a clarification
- **WHEN** a user that is not an involved party attempts to resolve the clarification
- **THEN** the system rejects the completion attempt

### Requirement: Clarification creator may permanently delete an open clarification
The system SHALL allow the creator of a clarification to permanently remove it from the database while it is `OPEN`. Deletion of a `DONE` clarification SHALL be rejected. No other actor SHALL be permitted to delete a clarification. Deletion SHALL be a hard delete with no audit trail retained.

#### Scenario: Creator deletes their open clarification
- **WHEN** the creator requests deletion of their own open clarification
- **THEN** the clarification is permanently removed from the database

#### Scenario: Creator cannot delete their done clarification
- **WHEN** the creator requests deletion of their own done clarification
- **THEN** the system rejects the deletion attempt

#### Scenario: Non-creator cannot delete a clarification
- **WHEN** an involved party who is not the creator requests deletion of a clarification
- **THEN** the system rejects the deletion attempt

### Requirement: Domain service resolves project-level context for lead-only clarification creation
The system SHALL provide a domain service `MonthEndProjectContextService` in `monthend.domain.services` that validates and assembles the context required for creating a project-level clarification (no subject employee). The service SHALL verify that the project is active for the given month and return a `MonthEndProjectContext` containing the project snapshot and the set of lead IDs that are active for that month. It SHALL NOT validate any subject employee or project assignment.

#### Scenario: Valid project context assembled successfully
- **WHEN** `MonthEndProjectContextService.resolve(month, projectId)` is called with a project active in that month
- **THEN** the service returns a `MonthEndProjectContext` containing the matching project snapshot and the filtered set of active lead IDs

#### Scenario: Unknown or inactive project raises error
- **WHEN** no project matching `projectId` is active in the given month
- **THEN** the service throws `MonthEndProjectContextNotFoundException`

#### Scenario: Inactive lead is excluded from project context
- **WHEN** a project's leads set contains a UserId that has no matching active user for the given month
- **THEN** that UserId is absent from `MonthEndProjectContext.eligibleProjectLeadIds()`

### Requirement: Clarifications do not block generated month-end task completion
The system SHALL treat clarification subtasks as independent from the generated `MonthEndTask` obligations in the same month-end context.

#### Scenario: Generated task can complete while clarification remains open
- **WHEN** a generated month-end task is completed for a project employee that still has an open clarification
- **THEN** the generated task completes successfully
- **THEN** the clarification remains open until it is resolved through the clarification flow

### Requirement: Clarification repository supports full-status queries for actor-scoped month-end overviews
The system SHALL provide two additional repository query methods that return all clarifications for an actor's scope for a given month, regardless of clarification status:

- `findAllEmployeeClarifications(UserId employeeId, YearMonth month)`: returns all `MonthEndClarification` records where the employee is the `subjectEmployeeId` for that month, including both `OPEN` and `DONE`.
- `findAllProjectLeadClarifications(UserId leadId, YearMonth month)`: returns all `MonthEndClarification` records for projects the lead leads for that month, including both `OPEN` and `DONE`.

#### Scenario: findAllEmployeeClarifications returns open and done clarifications
- **WHEN** `findAllEmployeeClarifications` is called for an employee who has both open and done clarifications as subject employee in that month
- **THEN** both open and done clarifications are returned

#### Scenario: findAllEmployeeClarifications excludes clarifications from other months
- **WHEN** `findAllEmployeeClarifications` is called for a given month
- **THEN** clarifications from other months are not returned

#### Scenario: findAllProjectLeadClarifications returns clarifications for all led projects
- **WHEN** `findAllProjectLeadClarifications` is called for a lead who leads multiple projects
- **THEN** clarifications for all those projects are returned, regardless of status

#### Scenario: findAllProjectLeadClarifications excludes clarifications from projects the lead does not lead
- **WHEN** `findAllProjectLeadClarifications` is called
- **THEN** clarifications from projects where the lead has no eligible-actor role are not returned

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

### Requirement: Clarification state changes fire domain events
The system SHALL fire a CDI domain event whenever a `MonthEndClarification` changes state. `ClarificationCreatedEvent`, `ClarificationUpdatedEvent`, `ClarificationCompletedEvent`, and `ClarificationDeletedEvent` SHALL be defined in `hexagon/monthend/domain/event/`. Each event SHALL carry the `MonthEndClarificationId`, `sourceSystem`, creator `UserId`, subject employee `UserId` (optional), `YearMonth`, and `ProjectId`. `ClarificationCompletedEvent` SHALL additionally carry the resolver `UserId`. `ClarificationUpdatedEvent` SHALL carry the `actorId` (editor) and the updated `text`. Events SHALL be Java records.

`ZepMailProcessingFailedEvent` SHALL also be defined in `hexagon/monthend/domain/event/` (see `ZEP mail processing failure fires ZepMailProcessingFailedEvent` requirement above).

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
- **THEN** a `ZepMailProcessingFailedEvent` is fired via CDI (see `ZEP mail processing failure fires ZepMailProcessingFailedEvent` requirement for full contract)

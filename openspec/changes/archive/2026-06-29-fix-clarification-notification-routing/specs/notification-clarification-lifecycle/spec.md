## MODIFIED Requirements

### Requirement: Clarification lifecycle events trigger notification emails via CDI observation
The system SHALL provide a `ClarificationLifecycleNotificationAdapter` inbound adapter in the notification BC that observes `ClarificationCreatedEvent`, `ClarificationUpdatedEvent`, `ClarificationCompletedEvent`, and `ClarificationDeletedEvent` via CDI `@Observes`. For each event the adapter SHALL send the corresponding notification mail via `NotificationMailPort`.

The adapter SHALL suppress the `ClarificationCreatedEvent` notification when the clarification's `sourceSystem` is `ZEP`, matching legacy `CommentServiceImpl` behaviour where ZEP-sourced comments did not trigger a creation mail.

The adapter SHALL skip project-level clarifications (where `subjectEmployeeId` is null) for all event types — project-level clarifications have no individual subject employee to notify.

For `CREATED`, `UPDATED`, and `DELETED` events, the adapter SHALL determine recipients based on creator identity:
- If the creator is one of the eligible project leads, the adapter SHALL send a single notification to the subject employee.
- If the creator is the subject employee, the adapter SHALL fan out and send one notification to each eligible project lead.

For `COMPLETED` events, if the clarification creator is the system actor (`SystemActor.USER_ID`), the adapter SHALL skip the notification and log that no notification was sent.

#### Scenario: MEGA-sourced clarification created by a lead notifies the subject employee
- **WHEN** a `ClarificationCreatedEvent` is fired with `sourceSystem = MEGA` and the creator is one of the eligible project leads
- **THEN** the notification adapter sends a single `CLARIFICATION_CREATED` mail to the subject employee

#### Scenario: MEGA-sourced clarification created by the subject employee notifies all eligible leads
- **WHEN** a `ClarificationCreatedEvent` is fired with `sourceSystem = MEGA` and the creator is the subject employee
- **THEN** the notification adapter sends a `CLARIFICATION_CREATED` mail to each eligible project lead (one email per lead)

#### Scenario: ZEP-sourced clarification creation does not trigger notification
- **WHEN** a `ClarificationCreatedEvent` is fired with `sourceSystem = ZEP`
- **THEN** the notification adapter suppresses the mail and logs that no notification was sent

#### Scenario: Clarification text updated by a lead notifies the subject employee
- **WHEN** a `ClarificationUpdatedEvent` is fired and the actor is one of the eligible project leads
- **THEN** the notification adapter sends a single `CLARIFICATION_UPDATED` mail to the subject employee

#### Scenario: Clarification text updated by the subject employee notifies all eligible leads
- **WHEN** a `ClarificationUpdatedEvent` is fired and the actor is the subject employee
- **THEN** the notification adapter sends a `CLARIFICATION_UPDATED` mail to each eligible project lead (one email per lead)

#### Scenario: Clarification completion triggers notification to creator
- **WHEN** a `ClarificationCompletedEvent` is fired and the creator is not the system actor
- **THEN** the notification adapter sends a `CLARIFICATION_COMPLETED` mail to the clarification creator

#### Scenario: Clarification completion skipped for system-actor creator
- **WHEN** a `ClarificationCompletedEvent` is fired and the creator is the system actor (`SystemActor.USER_ID`)
- **THEN** the notification adapter skips the notification and logs that no notification was sent

#### Scenario: Clarification deleted by a lead notifies the subject employee
- **WHEN** a `ClarificationDeletedEvent` is fired and the creator is one of the eligible project leads
- **THEN** the notification adapter sends a single `CLARIFICATION_DELETED` mail to the subject employee

#### Scenario: Clarification deleted by the subject employee notifies all eligible leads
- **WHEN** a `ClarificationDeletedEvent` is fired and the creator is the subject employee
- **THEN** the notification adapter sends a `CLARIFICATION_DELETED` mail to each eligible project lead (one email per lead)

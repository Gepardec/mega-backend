# Notification Clarification Lifecycle

## Purpose

Defines how the notification BC reacts to clarification lifecycle events (created, completed, deleted) to send notification emails, and specifies the `NotificationMailPort` outbound port that abstracts HTML template email sending within the notification BC.

## Requirements

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

### Requirement: NotificationMailPort abstracts HTML template email sending for the notification BC
The system SHALL define a `NotificationMailPort` outbound port in `notification/domain/port/outbound/`. The port SHALL accept a `MailNotificationId`, recipient email address, recipient first name, locale, optional template parameters, and optional subject parameters.

`MailNotificationId` SHALL be a sealed interface in `notification/domain/model/` that permits exactly `ReminderType` and `ClarificationNotificationType` as implementations. It SHALL declare `String name()` to support convention-based template resolution.

`ClarificationNotificationType` SHALL be an enum in `notification/domain/model/` implementing `MailNotificationId` with values: `CLARIFICATION_CREATED`, `CLARIFICATION_UPDATED`, `CLARIFICATION_COMPLETED`, `CLARIFICATION_DELETED`, `ZEP_CLARIFICATION_PROCESSING_ERROR`.

`QuarkusMailNotificationAdapter` SHALL implement `NotificationMailPort` without importing any class from the legacy `notification.mail` package. It SHALL resolve the email template at `emails/{id.name()}.html` and the subject at `mail.{id.name()}.subject`. For `ReminderType` values the adapter SHALL render the template snippet inside `emails/reminder-template.html` (two-pass render). For `ClarificationNotificationType` values the adapter SHALL render the template directly (single-pass render).

The port SHALL NOT be placed in `shared/`; it is internal to the notification BC.

#### Scenario: Clarification notification mail is sent via the port
- **WHEN** `NotificationMailPort.send(ClarificationNotificationType.CLARIFICATION_CREATED, ...)` is called with a valid recipient
- **THEN** the `QuarkusMailNotificationAdapter` loads `emails/CLARIFICATION_CREATED.html`, performs a single-pass render, and dispatches the mail

#### Scenario: Reminder notification mail uses two-pass render
- **WHEN** `NotificationMailPort.send(ReminderType.EMPLOYEE_CHECK_PROJECTTIME, ...)` is called
- **THEN** the adapter loads `emails/EMPLOYEE_CHECK_PROJECTTIME.html` as the body snippet, injects it as `$mailText$` into `emails/reminder-template.html`, and dispatches the result

#### Scenario: Notification BC use cases and adapters use the port exclusively
- **WHEN** any notification BC class needs to send an email
- **THEN** it calls `NotificationMailPort` with a `MailNotificationId` value and does NOT reference legacy `MailSender` directly

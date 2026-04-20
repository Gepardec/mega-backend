# Notification Clarification Lifecycle

## Purpose

Defines how the notification BC reacts to clarification lifecycle events (created, completed, deleted) to send notification emails, and specifies the `NotificationMailPort` outbound port that abstracts HTML template email sending within the notification BC.

## Requirements

### Requirement: Clarification lifecycle events trigger notification emails via CDI observation
The system SHALL provide a `ClarificationLifecycleNotificationAdapter` inbound adapter in the notification BC that observes `ClarificationCreatedEvent`, `ClarificationUpdatedEvent`, `ClarificationCompletedEvent`, and `ClarificationDeletedEvent` via CDI `@Observes`. For each event the adapter SHALL send the corresponding notification mail via `NotificationMailPort`.

The adapter SHALL suppress the `ClarificationCreatedEvent` notification when the clarification's `sourceSystem` is `ZEP`, matching legacy `CommentServiceImpl` behaviour where ZEP-sourced comments did not trigger a creation mail.

The adapter SHALL skip project-level clarifications (where `subjectEmployeeId` is null) for all event types — project-level clarifications have no individual subject employee to notify.

#### Scenario: MEGA-sourced clarification creation triggers notification
- **WHEN** a `ClarificationCreatedEvent` is fired with `sourceSystem = MEGA`
- **THEN** the notification adapter sends a `CLARIFICATION_CREATED` mail to the clarification subject employee

#### Scenario: ZEP-sourced clarification creation does not trigger notification
- **WHEN** a `ClarificationCreatedEvent` is fired with `sourceSystem = ZEP`
- **THEN** the notification adapter suppresses the mail and logs that no notification was sent

#### Scenario: Clarification text update triggers notification
- **WHEN** a `ClarificationUpdatedEvent` is fired
- **THEN** the notification adapter sends a `CLARIFICATION_UPDATED` mail to the clarification subject employee

#### Scenario: Clarification completion triggers notification
- **WHEN** a `ClarificationCompletedEvent` is fired
- **THEN** the notification adapter sends a `CLARIFICATION_COMPLETED` mail to the clarification assignee

#### Scenario: Clarification deletion triggers notification
- **WHEN** a `ClarificationDeletedEvent` is fired
- **THEN** the notification adapter sends a `CLARIFICATION_DELETED` mail to the clarification subject employee

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

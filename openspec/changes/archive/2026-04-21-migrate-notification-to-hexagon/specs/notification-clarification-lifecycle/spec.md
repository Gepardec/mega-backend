## ADDED Requirements

### Requirement: Clarification lifecycle events trigger notification emails via CDI observation
The system SHALL provide a `ClarificationLifecycleNotificationAdapter` inbound adapter in the notification BC that observes `ClarificationCreatedEvent`, `ClarificationCompletedEvent`, and `ClarificationDeletedEvent` via CDI `@Observes`. For each event the adapter SHALL send the corresponding notification mail via `NotificationMailPort`.

The adapter SHALL suppress the `ClarificationCreatedEvent` notification when the clarification's `sourceSystem` is `ZEP`, matching legacy `CommentServiceImpl` behaviour where ZEP-sourced comments did not trigger a creation mail.

#### Scenario: MEGA-sourced clarification creation triggers notification
- **WHEN** a `ClarificationCreatedEvent` is fired with `sourceSystem = MEGA`
- **THEN** the notification adapter sends a `COMMENT_CREATED` mail to the clarification owner

#### Scenario: ZEP-sourced clarification creation does not trigger notification
- **WHEN** a `ClarificationCreatedEvent` is fired with `sourceSystem = ZEP`
- **THEN** the notification adapter suppresses the mail and logs that no notification was sent

#### Scenario: Clarification completion triggers notification
- **WHEN** a `ClarificationCompletedEvent` is fired
- **THEN** the notification adapter sends a `COMMENT_CLOSED` mail to the clarification assignee

#### Scenario: Clarification deletion triggers notification
- **WHEN** a `ClarificationDeletedEvent` is fired
- **THEN** the notification adapter sends a `COMMENT_DELETED` mail to the clarification owner

### Requirement: NotificationMailPort abstracts HTML template email sending for the notification BC
The system SHALL define a `NotificationMailPort` outbound port in `notification/domain/port/outbound/`. The port SHALL accept a mail type identifier, recipient address, recipient first name, locale, optional template parameters, and optional subject parameters. The `QuarkusMailNotificationAdapter` SHALL implement this port by delegating to `MailSender` and `NotificationHelper`.

The port SHALL NOT be placed in `shared/`; it is internal to the notification BC.

#### Scenario: Mail is sent via the port
- **WHEN** `NotificationMailPort.send(...)` is called with a valid recipient and mail type
- **THEN** the `QuarkusMailNotificationAdapter` delegates to `MailSender` and the mail is dispatched

#### Scenario: Notification BC use cases and adapters use the port exclusively
- **WHEN** any notification BC class needs to send an email
- **THEN** it calls `NotificationMailPort` and does NOT reference `MailSender` directly

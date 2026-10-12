## MODIFIED Requirements

### Requirement: ZEP mail processing use case fetches, parses, and publishes a domain event per valid message
The system SHALL provide a `ProcessZepMailUseCase` that, when invoked, fetches unread messages from the ZEP mailbox via `ZepMailboxPort`, attempts to parse each message via `ZepMailMessageParser`, and for each successfully parsed message fires a `ZepClarificationMailReceived` integration event via CDI.

The use case SHALL resolve the ZEP creator's user record via `UserRepository.findByZepUsername()` before firing the event, so that the event carries the creator's `UserId` and email for error reporting purposes.

Error notifications SHALL be sent via `NotificationMailPort` using `ClarificationNotificationType.ZEP_CLARIFICATION_PROCESSING_ERROR` as the `MailNotificationId`.

#### Scenario: Valid ZEP mail is parsed and published as an event
- **WHEN** `ProcessZepMailUseCase` is invoked and the mailbox contains one valid unread ZEP message
- **THEN** the use case parses the message, resolves the creator, and fires one `ZepClarificationMailReceived` event

#### Scenario: Unparseable message is skipped with error notification
- **WHEN** a message cannot be parsed by `ZepMailMessageParser`
- **THEN** the use case logs an error and sends a `ZEP_CLARIFICATION_PROCESSING_ERROR` notification via `NotificationMailPort` to the resolved creator if the creator could be determined

#### Scenario: Processing exception sends error notification to creator
- **WHEN** an exception occurs after the creator is resolved (e.g., during event firing or downstream processing)
- **THEN** the use case catches the exception and sends `ClarificationNotificationType.ZEP_CLARIFICATION_PROCESSING_ERROR` to the creator via `NotificationMailPort`, including the original mail raw content

#### Scenario: Multiple unread messages are each processed independently
- **WHEN** the mailbox contains multiple unread messages
- **THEN** each message is processed independently; a failure on one message does not prevent processing of others

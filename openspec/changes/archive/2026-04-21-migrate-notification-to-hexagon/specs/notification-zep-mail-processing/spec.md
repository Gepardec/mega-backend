## ADDED Requirements

### Requirement: ZEP mail webhook triggers the ZEP mail processing use case
The system SHALL provide a `ZepMailWebhookResource` REST adapter in the notification BC that exposes the existing `/pubsub/message-received` endpoint contract. On receipt of a Pub/Sub notification the adapter SHALL invoke `ProcessZepMailUseCase`. The adapter SHALL return HTTP 200 on success and HTTP 500 on unhandled exception, preserving the existing contract of `PubSubResourceImpl`.

#### Scenario: Webhook invocation triggers mail processing
- **WHEN** a POST request is received at `/pubsub/message-received`
- **THEN** the adapter invokes `ProcessZepMailUseCase` and returns HTTP 200

#### Scenario: Unhandled exception returns HTTP 500
- **WHEN** `ProcessZepMailUseCase` throws an unhandled exception
- **THEN** the adapter returns HTTP 500 with the error message in the response body

### Requirement: ZEP mail processing use case fetches, parses, and publishes a domain event per valid message
The system SHALL provide a `ProcessZepMailUseCase` that, when invoked, fetches unread messages from the ZEP mailbox via `ZepMailboxPort`, attempts to parse each message via `ZepMailMessageParser`, and for each successfully parsed message fires a `ZepClarificationMailReceived` integration event via CDI.

The use case SHALL resolve the ZEP creator's user record via `UserRepository.findByZepUsername()` before firing the event, so that the event carries the creator's `UserId` and email for error reporting purposes.

#### Scenario: Valid ZEP mail is parsed and published as an event
- **WHEN** `ProcessZepMailUseCase` is invoked and the mailbox contains one valid unread ZEP message
- **THEN** the use case parses the message, resolves the creator, and fires one `ZepClarificationMailReceived` event

#### Scenario: Unparseable message is skipped with error notification
- **WHEN** a message cannot be parsed by `ZepMailMessageParser`
- **THEN** the use case logs an error and sends an error notification via `NotificationMailPort` to the resolved creator if the creator could be determined

#### Scenario: Processing exception sends error notification to creator
- **WHEN** an exception occurs after the creator is resolved (e.g., during event firing or downstream processing)
- **THEN** the use case catches the exception and sends a `ZEP_COMMENT_PROCESSING_ERROR` mail to the creator via `NotificationMailPort` including the original mail raw content

#### Scenario: Multiple unread messages are each processed independently
- **WHEN** the mailbox contains multiple unread messages
- **THEN** each message is processed independently; a failure on one message does not prevent processing of others

### Requirement: ZepClarificationMailReceived is an integration event in the shared domain
The system SHALL define `ZepClarificationMailReceived` in `hexagon/shared/domain/event/`. The event SHALL carry all fields needed for a monthend BC observer to create a clarification without additional mailbox or ZEP lookups: `creatorUserId`, `creatorEmail`, `employeeFirstName`, `employeeLastName`, `zepIdErsteller`, `date`, `timeFrom`, `timeTo`, `projectName`, `task`, `remark`, `clarification`, and `message`.

The event SHALL be a Java record.

#### Scenario: Integration event carries complete clarification context
- **WHEN** `ProcessZepMailService` fires `ZepClarificationMailReceived`
- **THEN** the event contains all fields required by the monthend observer to create a clarification without further ZEP or mailbox queries

### Requirement: ZepMailboxPort returns domain-typed raw mails; ZepMailMessageParser is a domain service
The system SHALL define `ZepRawMail(String subject, String htmlBody)` as a domain value object in `notification/domain/model/`. `ZepMailboxPort.fetchUnreadMessages()` SHALL return `List<ZepRawMail>`. `ImapZepMailboxAdapter` is responsible for converting `jakarta.mail.Message` to `ZepRawMail` (extracting subject and the `text/html` body part). No `jakarta.mail` types SHALL appear outside the adapter layer.

The system SHALL provide `ZepMailMessageParser` as a pure domain service in `notification/domain/service/`. It takes a `ZepRawMail` and returns a `ZepMailParseResult`. As a domain service it has no infrastructure dependencies and no port interface. `ProcessZepMailService` injects it directly.

The HTML body SHALL contain a `<table>` where each row maps a field label to its value via `<tr><td>LABEL</td><td><div><span>VALUE</span></div></td></tr>`. The required labels are `Ersteller-ID`, `Mitarbeiter`, `Projekt`, `Vorgang`, `Bemerkungen`, and `Anmerkung`. `Anmerkung` carries the free-form clarification text entered by the ZEP sender describing what must be changed; it must be non-empty — a message missing this label or containing a blank value is treated as invalid, because without it the subject employee cannot know what the project lead wants corrected. It is mapped to the `clarification` field in both `ZepProjektzeitEntry` and `ZepClarificationMailReceived`. The `message` field of `ZepProjektzeitEntry` SHALL store the raw HTML table as-is so that consumers can render it directly.

`ZepMailParseResult` SHALL carry the parsed `Optional<ZepProjektzeitEntry>` and the best-effort `Optional<ZepUsername> creatorUsername` (always populated when the `Ersteller-ID` label is readable, even on parse failure). `ProcessZepMailService` derives `rawContent` for error notifications directly from the `ZepRawMail` it holds; `rawContent` is not carried in `ZepProjektzeitEntry` or `ZepClarificationMailReceived`.

`ZepProjektzeitEntry` SHALL be a Java record in `notification/domain/model/`.

#### Scenario: Valid ZEP message is parsed into a domain object
- **WHEN** a `ZepRawMail` with a matching subject and an HTML body containing the required table fields is passed to `ZepMailMessageParser`
- **THEN** the parser returns a `ZepMailParseResult` with a populated `ZepProjektzeitEntry` and the raw HTML table in the `message` field

#### Scenario: Message with invalid subject returns empty
- **WHEN** a message with a subject that does not match the ZEP pattern is passed to `ZepMailMessageParser`
- **THEN** the parser returns `Optional.empty()`

#### Scenario: Message body without a table returns empty
- **WHEN** a message body does not contain an HTML table, or the table is missing required fields
- **THEN** the parser returns `Optional.empty()`

#### Scenario: Message with blank Anmerkung returns empty
- **WHEN** the table contains an `Anmerkung` row but its value is blank
- **THEN** the parser returns `Optional.empty()`

### Requirement: ZepMailboxPort abstracts IMAP inbox access
The system SHALL define a `ZepMailboxPort` outbound port in the notification domain. The port SHALL expose a single method returning `List<ZepRawMail>` — unread messages from the configured ZEP sender already converted to domain values. The `ImapZepMailboxAdapter` SHALL implement this port using the existing IMAP configuration from `MailReceiverConfig` and is the sole class that imports `jakarta.mail` types.

#### Scenario: Unread messages from ZEP sender are returned as domain values
- **WHEN** `ZepMailboxPort.fetchUnreadMessages()` is called
- **THEN** it returns only unread messages from the configured ZEP sender address as `ZepRawMail` records with subject and HTML body extracted

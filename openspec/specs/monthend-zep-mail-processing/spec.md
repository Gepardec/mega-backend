# Monthend ZEP Mail Processing

## Purpose

Defines how the monthend BC receives ZEP mail webhook notifications, parses them, and creates `MonthEndClarification` entries directly. Covers the `ZepMailWebhookResource` REST adapter, the `CreateClarificationFromZepMailUseCase`, the `ZepMailboxPort` outbound port, domain models (`ZepRawMail`, `ZepProjektzeitEntry`, `ZepMailParseResult`), the `ZepMailMessageParser` domain service, and the `ImapZepMailboxAdapter` infrastructure adapter.

## Requirements

### Requirement: ZEP mail webhook triggers clarification creation in the monthend BC
The system SHALL provide a `ZepMailWebhookResource` REST adapter in the monthend BC that exposes the `/pubsub/message-received` endpoint. On receipt of a Pub/Sub notification the adapter SHALL invoke `CreateClarificationFromZepMailUseCase`. The adapter SHALL return HTTP 200 on success and HTTP 500 on unhandled exception, preserving the existing contract.

#### Scenario: Webhook invocation triggers clarification creation
- **WHEN** a POST request is received at `/pubsub/message-received`
- **THEN** the adapter invokes `CreateClarificationFromZepMailUseCase` and returns HTTP 200

#### Scenario: Unhandled exception returns HTTP 500
- **WHEN** `CreateClarificationFromZepMailUseCase` throws an unhandled exception
- **THEN** the adapter returns HTTP 500 with the error message in the response body

### Requirement: CreateClarificationFromZepMailUseCase fetches, parses, and creates clarifications directly
The system SHALL provide a `CreateClarificationFromZepMailUseCase` in the monthend BC that, when invoked, fetches unread messages from the ZEP mailbox via `ZepMailboxPort`, parses each message via `ZepMailMessageParser`, and for each successfully parsed message calls the monthend domain/repositories directly to create a `MonthEndClarification` with `SourceSystem.ZEP`. No intermediate CDI integration event is used.

On parse failure or processing exception the use case SHALL fire a `ZepMailProcessingFailedEvent` domain event carrying the creator `UserId`, creator email, error message, and raw mail content. The notification BC observes this event and is responsible for sending the error mail.

The use case SHALL resolve the ZEP creator's user record via `UserRepository.findByZepUsername()` before attempting clarification creation.

#### Scenario: Valid ZEP mail is parsed and persisted as a clarification
- **WHEN** `CreateClarificationFromZepMailUseCase` is invoked and the mailbox contains one valid unread ZEP message
- **THEN** the use case parses the message, resolves the creator, and persists one `MonthEndClarification` with `SourceSystem.ZEP`

#### Scenario: Unparseable message fires ZepMailProcessingFailedEvent
- **WHEN** a message cannot be parsed by `ZepMailMessageParser`
- **THEN** the use case fires a `ZepMailProcessingFailedEvent` with the error details and raw mail content

#### Scenario: Processing exception fires ZepMailProcessingFailedEvent with creator context
- **WHEN** an exception occurs after the creator is resolved (e.g. during clarification persistence)
- **THEN** the use case fires a `ZepMailProcessingFailedEvent` carrying the resolved creator's UserId and email

#### Scenario: Multiple unread messages are each processed independently
- **WHEN** the mailbox contains multiple unread messages
- **THEN** each message is processed independently; a failure on one message does not prevent processing of others

### Requirement: ZepMailboxPort and ZEP mail models reside in the monthend domain
The system SHALL define `ZepMailboxPort`, `ZepRawMail`, `ZepMailParseResult`, `ZepProjektzeitEntry`, and `ZepMailMessageParser` in the monthend domain packages. No `jakarta.mail` types SHALL appear outside `ImapZepMailboxAdapter`.

`ZepMailboxPort` SHALL be an outbound port in `monthend/domain/port/outbound/` exposing `List<ZepRawMail> fetchUnreadMessages()`.

`ZepRawMail(String subject, String htmlBody)` and `ZepProjektzeitEntry` SHALL be Java records in `monthend/domain/model/`.

`ZepMailMessageParser` SHALL be a pure domain service in `monthend/domain/service/` with no infrastructure dependencies.

#### Scenario: Unread messages from ZEP sender are returned as domain values
- **WHEN** `ZepMailboxPort.fetchUnreadMessages()` is called
- **THEN** it returns only unread messages from the configured ZEP sender address as `ZepRawMail` records

#### Scenario: Valid ZEP mail body is parsed into a ZepProjektzeitEntry
- **WHEN** a `ZepRawMail` with a matching subject and required HTML table fields is passed to `ZepMailMessageParser`
- **THEN** the parser returns a `ZepMailParseResult` with a populated `ZepProjektzeitEntry`

#### Scenario: Message with blank Anmerkung returns empty parse result
- **WHEN** the table contains an `Anmerkung` row but its value is blank
- **THEN** the parser returns a `ZepMailParseResult` with an empty `Optional<ZepProjektzeitEntry>`

### Requirement: ImapZepMailboxAdapter resides in monthend outbound adapters
The system SHALL provide `ImapZepMailboxAdapter` implementing `ZepMailboxPort` in `monthend/adapter/outbound/`. It is the sole class that imports `jakarta.mail` types and is responsible for IMAP connection, inbox search, and `ZepRawMail` extraction.

#### Scenario: Adapter returns domain-typed raw mails from IMAP
- **WHEN** `ImapZepMailboxAdapter.fetchUnreadMessages()` is called
- **THEN** it connects to the configured IMAP host, searches for unread messages from the ZEP sender, and returns them as `ZepRawMail` records

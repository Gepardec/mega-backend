# Spec Delta

## MODIFIED Requirements

### Requirement: ZEP mail webhook triggers clarification creation in the monthend BC
The system SHALL provide a `ZepMailWebhookResource` REST adapter in the monthend BC that exposes the `/pubsub/message-received` endpoint. When a Pub/Sub notification arrives, the adapter SHALL invoke `CreateClarificationFromZepMailUseCase`. The adapter SHALL return HTTP 200 on success and HTTP 500 on an unhandled exception, so Pub/Sub retries remain triggered as before. The 500 response SHALL be a problem-details body that does not contain the exception message.

#### Scenario: Webhook invocation triggers clarification creation
- **WHEN** a POST request is received at `/pubsub/message-received`
- **THEN** the adapter invokes `CreateClarificationFromZepMailUseCase` and returns HTTP 200

#### Scenario: Unhandled exception returns HTTP 500
- **WHEN** `CreateClarificationFromZepMailUseCase` throws an unhandled exception
- **THEN** the adapter returns HTTP 500 with a problem body
- **THEN** the response body does not contain the exception message

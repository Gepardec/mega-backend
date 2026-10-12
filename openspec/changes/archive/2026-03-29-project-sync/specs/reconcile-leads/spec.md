## ADDED Requirements

### Requirement: Reconcile fetches lead usernames per project from ZEP
For each project in the repository, the system SHALL call `ZepProjectPort.fetchLeadUsernames(zepId)` to retrieve the current set of lead usernames from ZEP. This is a per-project call (N calls total).

#### Scenario: Lead usernames fetched for each known project
- **WHEN** `ReconcileLeadsUseCase.reconcile()` is called
- **THEN** `ZepProjectPort.fetchLeadUsernames(zepId)` is called once per project in the repository

### Requirement: Lead usernames are resolved to UserId references
The system SHALL resolve each ZEP username to a `UserId` by calling `UserLookupPort.findUserIdByZepUsername(username)`. If a username cannot be resolved, that username SHALL be silently skipped. The reconciliation SHALL proceed for the remaining leads.

#### Scenario: Known username is resolved to UserId
- **WHEN** `UserLookupPort.findUserIdByZepUsername(username)` returns a UserId
- **THEN** that UserId is included in the project's leads set

#### Scenario: Unknown username is skipped silently
- **WHEN** `UserLookupPort.findUserIdByZepUsername(username)` returns empty
- **THEN** that username is skipped
- **THEN** reconciliation continues for the remaining leads and projects
- **THEN** no exception is thrown

### Requirement: Resolved leads replace the project's existing leads set
The system SHALL replace the full leads set on each project with the freshly resolved set from ZEP. Leads from a previous reconcile cycle that are no longer returned by ZEP SHALL be removed.

#### Scenario: Stale lead is removed on next reconcile
- **WHEN** a username that was previously a lead is no longer returned by ZEP for that project
- **THEN** the corresponding UserId is removed from the project's leads set after reconciliation

#### Scenario: New lead is added on reconcile
- **WHEN** a username is returned by ZEP as a lead for a project
- **THEN** the resolved UserId is present in the project's leads set after reconciliation

### Requirement: PROJECT_LEAD role is derived from reconciled leads
After updating all project leads, the system SHALL assign the `PROJECT_LEAD` role to any User who is a lead on at least one project. The system SHALL revoke the `PROJECT_LEAD` role from any User who is no longer a lead on any project. Role changes SHALL be persisted via `UserRepository.saveAll()`.

#### Scenario: User gains PROJECT_LEAD role
- **WHEN** a user's UserId appears in the leads set of at least one project after reconciliation
- **THEN** `PROJECT_LEAD` is added to that user's roles

#### Scenario: User loses PROJECT_LEAD role
- **WHEN** a user's UserId no longer appears in the leads set of any project after reconciliation
- **THEN** `PROJECT_LEAD` is removed from that user's roles

### Requirement: UserLookupPort is defined in the project domain
The project domain SHALL define its own `UserLookupPort` outbound interface. The adapter implementing this port SHALL query `hexagon_users` directly. The project domain SHALL NOT import any class from `com.gepardec.mega.hexagon.user`.

#### Scenario: Project domain has no import of user domain internals
- **WHEN** any class under `com.gepardec.mega.hexagon.project` is compiled
- **THEN** no import from `com.gepardec.mega.hexagon.user` is present

### Requirement: ReconcileLeadsUseCase is decoupled from Quarkus infrastructure
The `ReconcileLeadsUseCase` interface and its implementation SHALL NOT import or depend on any Quarkus, CDI, or JPA annotations.

#### Scenario: ReconcileLeadsService has no Quarkus imports
- **WHEN** `ReconcileLeadsService` is compiled
- **THEN** it imports only from `com.gepardec.mega.hexagon`, `java.*`, and standard libraries

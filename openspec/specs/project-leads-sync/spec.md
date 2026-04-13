# Reconcile Leads

## Purpose

Defines the `SyncProjectLeadsUseCase`, `ProjectLeadSyncResult`, and `SyncProjectLeadsService` implementation within the hexagonal domain. The project lead sync step resolves ZEP project lead usernames to internal `UserId` references, updates each project's leads set, and derives `PROJECT_LEAD` role assignments for users.

## Requirements

### Requirement: Project lead sync fetches lead usernames per project from ZEP
For each project in the repository, the system SHALL call `ZepProjectPort.fetchLeadUsernames(zepId)` to retrieve the current set of lead usernames from ZEP. This is a per-project call (N calls total).

#### Scenario: Lead usernames fetched for each known project
- **WHEN** `SyncProjectLeadsUseCase.sync()` is called
- **THEN** `ZepProjectPort.fetchLeadUsernames(zepId)` is called once per project in the repository

### Requirement: Lead usernames are resolved to UserId references
The system SHALL resolve each ZEP username to a `UserId` by calling `UserLookupPort.findUserIdByZepUsername(username)`. If a username cannot be resolved, that username SHALL be silently skipped. Project lead sync SHALL proceed for the remaining leads.

#### Scenario: Known username is resolved to UserId
- **WHEN** `UserLookupPort.findUserIdByZepUsername(username)` returns a UserId
- **THEN** that UserId is included in the project's leads set

#### Scenario: Unknown username is skipped silently
- **WHEN** `UserLookupPort.findUserIdByZepUsername(username)` returns empty
- **THEN** that username is skipped
- **THEN** project lead sync continues for the remaining leads and projects
- **THEN** no exception is thrown

### Requirement: Resolved leads replace the project's existing leads set
The system SHALL replace the full leads set on each project with the freshly resolved `UserId` set from ZEP by deriving a new immutable `Project` aggregate for that project. Leads from a previous project lead sync cycle that are no longer returned by ZEP SHALL be removed, and unchanged lead sets MAY be skipped from persistence.

#### Scenario: Stale lead is removed on next reconcile
- **WHEN** a username that was previously a lead is no longer returned by ZEP for that project
- **THEN** the corresponding `UserId` is removed from the project's leads set after project lead sync

#### Scenario: New lead is added on reconcile
- **WHEN** a username is returned by ZEP as a lead for a project
- **THEN** the resolved `UserId` is present in the project's leads set after project lead sync

#### Scenario: Unchanged lead set does not alter project master data
- **WHEN** the resolved lead set for a project matches the currently persisted leads set
- **THEN** project lead sync keeps the project's master data unchanged

### Requirement: Project lead sync boundary uses sync-oriented names
The system SHALL expose project lead synchronization through `SyncProjectLeadsUseCase` and `SyncProjectLeadsService`. The boundary SHALL use a `sync()` entrypoint and SHALL return `ProjectLeadSyncResult`.

#### Scenario: Project lead sync returns a named sync result
- **WHEN** `SyncProjectLeadsUseCase.sync()` completes
- **THEN** it returns `ProjectLeadSyncResult`
- **THEN** the result exposes `resolved`, `skipped`, `rolesAdded`, and `rolesRevoked`

### Requirement: PROJECT_LEAD role is derived from synced leads
After syncing all project leads, the system SHALL assign the `PROJECT_LEAD` role to any User who is a lead on at least one project. The system SHALL revoke the `PROJECT_LEAD` role from any User who is no longer a lead on any project. Role changes SHALL be persisted via `UserRepository.saveAll()`.

#### Scenario: User gains PROJECT_LEAD role
- **WHEN** a user's UserId appears in the leads set of at least one project after project lead sync
- **THEN** `PROJECT_LEAD` is added to that user's roles

#### Scenario: User loses PROJECT_LEAD role
- **WHEN** a user's UserId no longer appears in the leads set of any project after project lead sync
- **THEN** `PROJECT_LEAD` is removed from that user's roles

### Requirement: Project lead sync returns a result with operation counts
`SyncProjectLeadsUseCase.sync()` SHALL return a `ProjectLeadSyncResult` record instead of `void`. `ProjectLeadSyncResult` SHALL contain integer fields: `resolved` (lead usernames successfully resolved to a UserId), `skipped` (lead usernames that could not be resolved), `rolesAdded` (users who gained the `PROJECT_LEAD` role), and `rolesRevoked` (users who lost the `PROJECT_LEAD` role). The `SyncScheduler` SHALL use these counts when composing its log output.

#### Scenario: Result reflects leads resolved during project lead sync
- **WHEN** `SyncProjectLeadsUseCase.sync()` successfully resolves N usernames to UserIds
- **THEN** the returned `ProjectLeadSyncResult.resolved()` equals N

#### Scenario: Result reflects leads skipped during project lead sync
- **WHEN** `SyncProjectLeadsUseCase.sync()` cannot resolve M usernames
- **THEN** the returned `ProjectLeadSyncResult.skipped()` equals M

#### Scenario: Result reflects PROJECT_LEAD roles added during project lead sync
- **WHEN** `SyncProjectLeadsUseCase.sync()` grants `PROJECT_LEAD` to K users
- **THEN** the returned `ProjectLeadSyncResult.rolesAdded()` equals K

#### Scenario: Result reflects PROJECT_LEAD roles revoked during project lead sync
- **WHEN** `SyncProjectLeadsUseCase.sync()` revokes `PROJECT_LEAD` from L users
- **THEN** the returned `ProjectLeadSyncResult.rolesRevoked()` equals L

### Requirement: SyncProjectLeadsService is a CDI-managed application-service boundary
The system SHALL implement `SyncProjectLeadsUseCase` with a CDI-managed application service that owns the transaction boundary for project lead sync and role derivation. `SyncScheduler` SHALL inject the use case via the inbound port instead of manually constructing the service implementation.

#### Scenario: Scheduler injects the project lead sync use case
- **WHEN** the unified `SyncScheduler` starts
- **THEN** it receives `SyncProjectLeadsUseCase` via CDI injection
- **THEN** it does not construct `SyncProjectLeadsService` manually

#### Scenario: Project lead sync runs in one application-service transaction
- **WHEN** `SyncProjectLeadsUseCase.sync()` is invoked
- **THEN** project lead replacement and user role reconciliation occur within the service-owned transaction boundary

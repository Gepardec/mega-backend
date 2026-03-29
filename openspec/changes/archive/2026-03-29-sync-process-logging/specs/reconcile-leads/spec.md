## ADDED Requirements

### Requirement: Reconcile returns a result with operation counts
`ReconcileLeadsUseCase.reconcile()` SHALL return a `ReconcileLeadsResult` record instead of `void`. `ReconcileLeadsResult` SHALL contain integer fields: `resolved` (lead usernames successfully resolved to a UserId), `skipped` (lead usernames that could not be resolved), `rolesAdded` (users who gained the `PROJECT_LEAD` role), and `rolesRevoked` (users who lost the `PROJECT_LEAD` role). The `SyncScheduler` SHALL use these counts when composing its log output.

#### Scenario: Result reflects leads resolved during reconciliation
- **WHEN** `ReconcileLeadsUseCase.reconcile()` successfully resolves N usernames to UserIds
- **THEN** the returned `ReconcileLeadsResult.resolved()` equals N

#### Scenario: Result reflects leads skipped during reconciliation
- **WHEN** `ReconcileLeadsUseCase.reconcile()` cannot resolve M usernames
- **THEN** the returned `ReconcileLeadsResult.skipped()` equals M

#### Scenario: Result reflects PROJECT_LEAD roles added during reconciliation
- **WHEN** `ReconcileLeadsUseCase.reconcile()` grants `PROJECT_LEAD` to K users
- **THEN** the returned `ReconcileLeadsResult.rolesAdded()` equals K

#### Scenario: Result reflects PROJECT_LEAD roles revoked during reconciliation
- **WHEN** `ReconcileLeadsUseCase.reconcile()` revokes `PROJECT_LEAD` from L users
- **THEN** the returned `ReconcileLeadsResult.rolesRevoked()` equals L

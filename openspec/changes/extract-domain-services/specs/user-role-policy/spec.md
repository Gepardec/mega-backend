## ADDED Requirements

### Requirement: OfficeManagementEmails value object encapsulates the configured OM email set
The system SHALL provide an `OfficeManagementEmails` domain value object in `user.domain.model` that wraps the configured set of office management email addresses. It SHALL expose a `contains(String email) → boolean` method that returns true when the given email matches any entry in the set after case-insensitive trimming. An `OfficeManagementEmailsProducer` in `user.application` SHALL read the OM email list from `@ConfigProperty` and produce an `@ApplicationScoped` CDI `OfficeManagementEmails` bean, normalising all entries on construction.

#### Scenario: contains matches regardless of case and surrounding whitespace
- **WHEN** `OfficeManagementEmails.contains(email)` is called with an email that matches a configured entry only after lowercasing and trimming
- **THEN** it returns `true`

#### Scenario: contains returns false for non-OM email
- **WHEN** `OfficeManagementEmails.contains(email)` is called with an email not in the configured set
- **THEN** it returns `false`

### Requirement: Domain policy determines roles for a user during sync
The system SHALL provide a `UserRolePolicyService` as a CDI `@ApplicationScoped` domain service in `user.domain.services` that encodes the role assignment rules applied when creating or updating a `User` during sync. It SHALL inject `OfficeManagementEmails` and contain no framework-specific annotations beyond `@ApplicationScoped` and `@Inject`. Given a user's email and their existing `User` (if any), it SHALL return the complete set of `Role` values the user should hold after sync.

Rules:
- Every user SHALL always receive `Role.EMPLOYEE`
- If the user's email matches the `OfficeManagementEmails` set, the user SHALL also receive `Role.OFFICE_MANAGEMENT`
- If an existing `User` is present and already holds `Role.PROJECT_LEAD`, that role SHALL be preserved in the returned set

#### Scenario: New user with no OM match receives only EMPLOYEE role
- **WHEN** `UserRolePolicyService.determineRoles(email, null)` is called with an email not in the OM set and no existing user
- **THEN** the returned set contains exactly `Role.EMPLOYEE`

#### Scenario: New user with OM email receives EMPLOYEE and OFFICE_MANAGEMENT
- **WHEN** `UserRolePolicyService.determineRoles(email, null)` is called with an email matching the OM set and no existing user
- **THEN** the returned set contains `Role.EMPLOYEE` and `Role.OFFICE_MANAGEMENT`

#### Scenario: Existing project lead retains PROJECT_LEAD role after sync
- **WHEN** `UserRolePolicyService.determineRoles(email, existingUser)` is called and `existingUser` holds `Role.PROJECT_LEAD`
- **THEN** the returned set contains `Role.PROJECT_LEAD` in addition to any email-derived roles

#### Scenario: Email comparison delegates to OfficeManagementEmails
- **WHEN** `UserRolePolicyService.determineRoles(email, null)` is called with an email that `OfficeManagementEmails.contains()` returns true for
- **THEN** the returned set contains `Role.OFFICE_MANAGEMENT`

### Requirement: User aggregate exposes role grant and revoke operations
The `User` aggregate SHALL provide `grantProjectLeadRole()` and `revokeProjectLeadRole()` methods that return a new immutable `User` instance with `Role.PROJECT_LEAD` added or removed respectively. These methods SHALL be used by domain services and application services instead of mutating the role set directly via `withRoles()`.

#### Scenario: Granting PROJECT_LEAD to a user without it
- **WHEN** `user.grantProjectLeadRole()` is called on a user who does not hold `Role.PROJECT_LEAD`
- **THEN** the returned user holds `Role.PROJECT_LEAD` alongside all previously held roles

#### Scenario: Granting PROJECT_LEAD to a user who already has it
- **WHEN** `user.grantProjectLeadRole()` is called on a user who already holds `Role.PROJECT_LEAD`
- **THEN** the returned user is equal to the original (idempotent)

#### Scenario: Revoking PROJECT_LEAD from a user who holds it
- **WHEN** `user.revokeProjectLeadRole()` is called on a user who holds `Role.PROJECT_LEAD`
- **THEN** the returned user does not hold `Role.PROJECT_LEAD`
- **THEN** all other roles are preserved

#### Scenario: Revoking PROJECT_LEAD from a user who does not have it
- **WHEN** `user.revokeProjectLeadRole()` is called on a user who does not hold `Role.PROJECT_LEAD`
- **THEN** the returned user is equal to the original (idempotent)

### Requirement: Domain service reconciles PROJECT_LEAD roles against project assignments
The system SHALL provide a `ProjectLeadRoleReconciliationService` in `user.domain.services` that, given all users and the full set of lead user IDs after a project lead sync, returns only those users whose `PROJECT_LEAD` role needed to change. Users who are now a lead but lack the role SHALL have it granted. Users who are no longer a lead but hold the role SHALL have it revoked. Users whose role already matches their lead status SHALL be excluded from the result.

#### Scenario: User who became a lead has PROJECT_LEAD granted
- **WHEN** `ProjectLeadRoleReconciliationService.reconcile(users, leadUserIds)` is called and a user's ID is in `leadUserIds` but the user does not hold `Role.PROJECT_LEAD`
- **THEN** the returned list includes that user with `Role.PROJECT_LEAD` granted

#### Scenario: User who is no longer a lead has PROJECT_LEAD revoked
- **WHEN** `ProjectLeadRoleReconciliationService.reconcile(users, leadUserIds)` is called and a user holds `Role.PROJECT_LEAD` but their ID is not in `leadUserIds`
- **THEN** the returned list includes that user with `Role.PROJECT_LEAD` revoked

#### Scenario: User whose lead status matches their role is excluded
- **WHEN** `ProjectLeadRoleReconciliationService.reconcile(users, leadUserIds)` is called and a user's `PROJECT_LEAD` role already matches their presence in `leadUserIds`
- **THEN** that user is not included in the returned list

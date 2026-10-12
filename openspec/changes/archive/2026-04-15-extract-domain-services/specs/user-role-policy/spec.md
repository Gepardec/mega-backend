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

### Requirement: UserRolePolicyService applies the project lead role policy for a single user
The system SHALL keep project-lead role policy in `UserRolePolicyService` in `user.domain.services`. Given a `User` and whether that user should currently be a lead, it SHALL return a new `User` with the `PROJECT_LEAD` role granted or revoked as needed.

#### Scenario: User who became a lead has PROJECT_LEAD granted
- **WHEN** `UserRolePolicyService.updateProjectLeadRole(user, true)` is called and the user does not hold `Role.PROJECT_LEAD`
- **THEN** the returned user holds `Role.PROJECT_LEAD`

#### Scenario: User who is no longer a lead has PROJECT_LEAD revoked
- **WHEN** `UserRolePolicyService.updateProjectLeadRole(user, false)` is called and the user holds `Role.PROJECT_LEAD`
- **THEN** the returned user no longer holds `Role.PROJECT_LEAD`

#### Scenario: User whose lead status already matches remains unchanged
- **WHEN** `UserRolePolicyService.updateProjectLeadRole(user, shouldBeLead)` is called and the user's `PROJECT_LEAD` role already matches `shouldBeLead`
- **THEN** the returned user is equal to the original

### Requirement: Project lead sync is coordinated as two application use cases
The system SHALL keep `SyncProjectLeadsService` focused on updating project lead assignments in the `project` subdomain. It SHALL return the resolved lead `UserId`s as shared-kernel data. A separate `SyncProjectLeadRolesService` in `user.application` SHALL receive those lead ids, apply `UserRolePolicyService` user by user, and persist changed users. The scheduler SHALL invoke the role sync step after project lead sync completes.

#### Scenario: Scheduler passes resolved lead ids into user role sync
- **WHEN** `SyncScheduler` runs a sync cycle
- **THEN** it invokes `SyncProjectLeadsUseCase.sync()`
- **THEN** it invokes `SyncProjectLeadRolesUseCase.sync(leadUserIds)` with the `leadUserIds` returned by the project lead sync step

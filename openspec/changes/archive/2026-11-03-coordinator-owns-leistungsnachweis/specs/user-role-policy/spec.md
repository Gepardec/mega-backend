# Spec Delta

## MODIFIED Requirements

### Requirement: Domain policy determines roles for a user during sync
The system SHALL provide a `UserRolePolicyService` as a CDI `@ApplicationScoped` domain service in `user.domain.services` that encodes the role assignment rules applied when creating or updating a `User` during sync. It SHALL inject `OfficeManagementEmails`, the configured admin emails, and the configured project coordinator emails and contain no framework-specific annotations beyond `@ApplicationScoped` and `@Inject`. Given a user's email and their existing `User` (if any), it SHALL return the complete set of `Role` values the user should hold after sync.

Rules:
- Every user SHALL always receive `Role.EMPLOYEE`
- If the user's email matches the `OfficeManagementEmails` set, the user SHALL also receive `Role.OFFICE_MANAGEMENT`
- If the user's email matches the configured admin email list, the user SHALL also receive `Role.ADMIN`
- If the user's email matches the configured project coordinator email list, the user SHALL also receive `Role.PROJECT_COORDINATOR`
- If an existing `User` is present and already holds `Role.PROJECT_LEAD`, that role SHALL be preserved in the returned set

The project coordinator role SHALL NOT depend on the user holding `Role.PROJECT_LEAD`; that a coordinator is also a project lead is an organizational assumption, not a rule enforced by the policy.

#### Scenario: New user with no OM match receives only EMPLOYEE role
- **WHEN** `UserRolePolicyService.determineRoles(email, null)` is called with an email not in the OM set and no existing user
- **THEN** the returned set contains exactly `Role.EMPLOYEE`

#### Scenario: New user with OM email receives EMPLOYEE and OFFICE_MANAGEMENT
- **WHEN** `UserRolePolicyService.determineRoles(email, null)` is called with an email matching the OM set and no existing user
- **THEN** the returned set contains `Role.EMPLOYEE` and `Role.OFFICE_MANAGEMENT`

#### Scenario: User with admin email receives ADMIN
- **WHEN** `UserRolePolicyService.determineRoles(email, null)` is called with an email matching the configured admin list
- **THEN** the returned set contains `Role.EMPLOYEE` and `Role.ADMIN`

#### Scenario: Existing admin whose email left the list loses ADMIN
- **WHEN** `UserRolePolicyService.determineRoles(email, existingUser)` is called, `existingUser` holds `Role.ADMIN`, and the email no longer matches the admin list
- **THEN** the returned set does not contain `Role.ADMIN`

#### Scenario: User with project coordinator email receives PROJECT_COORDINATOR
- **WHEN** `UserRolePolicyService.determineRoles(email, null)` is called with an email matching the configured project coordinator list
- **THEN** the returned set contains `Role.EMPLOYEE` and `Role.PROJECT_COORDINATOR`

#### Scenario: Existing coordinator whose email left the list loses PROJECT_COORDINATOR
- **WHEN** `UserRolePolicyService.determineRoles(email, existingUser)` is called, `existingUser` holds `Role.PROJECT_COORDINATOR`, and the email no longer matches the project coordinator list
- **THEN** the returned set does not contain `Role.PROJECT_COORDINATOR`

#### Scenario: Coordinator role is assigned independently of the project lead role
- **WHEN** `UserRolePolicyService.determineRoles(email, existingUser)` is called with an email matching the project coordinator list and `existingUser` does not hold `Role.PROJECT_LEAD`
- **THEN** the returned set contains `Role.PROJECT_COORDINATOR`
- **THEN** the returned set does not contain `Role.PROJECT_LEAD`

#### Scenario: Existing project lead retains PROJECT_LEAD role after sync
- **WHEN** `UserRolePolicyService.determineRoles(email, existingUser)` is called and `existingUser` holds `Role.PROJECT_LEAD`
- **THEN** the returned set contains `Role.PROJECT_LEAD` in addition to any email-derived roles

#### Scenario: Email comparison delegates to OfficeManagementEmails
- **WHEN** `UserRolePolicyService.determineRoles(email, null)` is called with an email that `OfficeManagementEmails.contains()` returns true for
- **THEN** the returned set contains `Role.OFFICE_MANAGEMENT`

## ADDED Requirements

### Requirement: Project coordinator email list is configured
The system SHALL read a configured list of project coordinator email addresses. The list MAY contain more than one address. Matching an email against the list SHALL ignore case and surrounding whitespace. An empty or absent list SHALL mean that no user receives the project coordinator role.

#### Scenario: Coordinator email matches regardless of case and whitespace
- **WHEN** the project coordinator list contains " Coordinator@Gepardec.com " and a user with email "coordinator@gepardec.com" is synced
- **THEN** the user receives the project coordinator role

#### Scenario: Several coordinators are configured
- **WHEN** the project coordinator list contains two email addresses and both users are synced
- **THEN** both users receive the project coordinator role

#### Scenario: No coordinator list configured
- **WHEN** no project coordinator email list is configured and any user is synced
- **THEN** no user receives the project coordinator role

## ADDED Requirements

### Requirement: User has a stable domain identity
The system SHALL assign each User a UUID generated internally (`UserId`) that is independent of any external system identifier. ZEP username and Personio ID are stored as external references within their respective profile value objects, not as the domain identity.

#### Scenario: New user created from ZEP data
- **WHEN** a ZEP employee is encountered that has no matching User in the repository
- **THEN** a new User is created with a freshly generated UUID as its `UserId`
- **THEN** the ZEP username is stored inside `ZepProfile`, not as the User's primary identity

#### Scenario: Existing user matched by ZEP username
- **WHEN** a ZEP employee's username matches an existing User's `ZepProfile.username`
- **THEN** the existing User is updated rather than a new one being created
- **THEN** the User's `UserId` remains unchanged

### Requirement: User aggregate contains ZepProfile value object
The system SHALL model all ZEP-sourced fields in a `ZepProfile` value object nested within the `User` aggregate. The ZepProfile SHALL contain: username, firstname, lastname, title, salutation, workDescription, language, releaseDate, employmentPeriods, and regularWorkingTimes.

#### Scenario: ZepProfile fields are updated via sync
- **WHEN** `user.syncFromZep(newZepProfile)` is called with updated data
- **THEN** the User's `zepProfile` is replaced with the new value object
- **THEN** no other fields on the User are affected

### Requirement: User aggregate contains nullable PersonioProfile value object
The system SHALL model all Personio-sourced fields in a `PersonioProfile` value object nested within the `User` aggregate. `PersonioProfile` MAY be null if Personio data has never been successfully fetched for this User. The PersonioProfile SHALL contain: personioId, vacationDayBalance, guildLead, internalProjectLead, and hasCreditCard.

#### Scenario: PersonioProfile populated after successful Personio fetch
- **WHEN** `user.syncFromPersonio(personioProfile)` is called with a valid profile
- **THEN** the User's `personioProfile` is set to the provided value object

#### Scenario: PersonioProfile preserved when Personio unavailable
- **WHEN** Personio is unavailable and no PersonioProfile is provided
- **THEN** the existing `personioProfile` on the User is NOT cleared
- **THEN** the User retains the last successfully synced Personio data

### Requirement: User has a set of roles derived during sync
The system SHALL assign roles to each User during sync. Every User SHALL have the `EMPLOYEE` role. The `OFFICE_MANAGEMENT` role SHALL be assigned if the User's ZEP username appears in the configured office management usernames list. The `PROJECT_LEAD` role is defined in the enum but SHALL NOT be assigned by this sync implementation.

#### Scenario: All users receive EMPLOYEE role
- **WHEN** a User is created or updated during sync
- **THEN** the User's roles set includes `EMPLOYEE`

#### Scenario: Office management role assigned from config
- **WHEN** a User's ZEP username is in the `UserSyncConfig.officeManagementUsernames` list
- **THEN** the User's roles set includes `OFFICE_MANAGEMENT`

#### Scenario: Regular employee does not receive office management role
- **WHEN** a User's ZEP username is NOT in the `UserSyncConfig.officeManagementUsernames` list
- **THEN** the User's roles set does NOT include `OFFICE_MANAGEMENT`

### Requirement: User has an active/inactive status
The system SHALL track each User's status as either `ACTIVE` or `INACTIVE`. A User becomes `INACTIVE` when they no longer appear in the ZEP employee list during sync.

#### Scenario: User deactivated when absent from ZEP
- **WHEN** a User exists in the repository but their ZEP username is not present in the current ZEP sync response
- **THEN** the User's status is set to `INACTIVE`

#### Scenario: Active user remains active
- **WHEN** a User's ZEP username is present in the current ZEP sync response
- **THEN** the User's status is set to `ACTIVE`

## MODIFIED Requirements

### Requirement: Sync creates new Users for unknown ZEP employees
The system SHALL attempt to match each incoming ZEP employee to an existing `User` in two phases. In phase 1, the system SHALL perform a batch lookup of existing Users by ZEP username. For any ZEP employee not matched in phase 1, the system SHALL perform a phase-2 lookup by email address. If a User is found by email in phase 2, the system SHALL treat that employee as a returning employee and update the existing User with the new ZEP data (including the new ZEP username and employment periods) rather than creating a new row. Only if no match is found in either phase SHALL the system create a new `User` aggregate with a generated `UserId`.

#### Scenario: New ZEP employee results in new User
- **WHEN** a ZEP employee's username has no matching User in the repository
- **AND** no User with that employee's email address exists in the repository
- **THEN** a new User is created with a generated UUID and saved to the repository

#### Scenario: Returning employee matched by email updates existing User
- **WHEN** a ZEP employee's username has no matching User in the repository by ZEP username
- **AND** a User with that employee's email address exists in the repository
- **THEN** the existing User is updated with the new ZEP username and employment periods from the incoming ZEP data
- **THEN** no new User row is created
- **THEN** the existing User's `UserId` and Personio reference remain unchanged

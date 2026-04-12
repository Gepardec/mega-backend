## ADDED Requirements

### Requirement: Use cases fetch provider-owned user details on demand
The system SHALL expose provider-detail lookup capabilities for user data that is not persisted in the local `User` aggregate. These lookups SHALL use the stable external references stored on the User, including ZEP username and Personio ID.

#### Scenario: Use case requests provider-owned user detail
- **WHEN** a hexagon use case needs user detail that is not part of the local `User` aggregate
- **THEN** it resolves that detail through a provider-detail lookup capability instead of reading it from local user persistence

### Requirement: Regular working times are resolved from ZEP on demand
The system SHALL resolve a user's regular working times from ZEP when a use case explicitly requests them. The lookup SHALL identify the user by stored ZEP username and SHALL return domain-typed regular working time data to the calling use case.

#### Scenario: Use case retrieves regular working times for a user
- **WHEN** a use case requests regular working times for a User with a stored ZEP username
- **THEN** the system calls the ZEP-backed provider-detail lookup using that username
- **THEN** the calling use case receives domain-typed regular working time data

#### Scenario: Missing ZEP username prevents regular working-time lookup
- **WHEN** a use case requests regular working times for a User without a stored ZEP username
- **THEN** the provider-detail lookup returns no result or a domain error instead of reading from local user persistence

### Requirement: Personio-owned user details are resolved by stored Personio ID
The system SHALL resolve Personio-owned user detail on demand using the stored Personio identifier for a User. If no Personio identifier is available locally, the lookup SHALL return no result instead of triggering routine sync behavior.

#### Scenario: Use case retrieves Personio detail for linked user
- **WHEN** a use case requests Personio-owned detail for a User with a stored Personio identifier
- **THEN** the system calls the Personio-backed provider-detail lookup using that identifier

#### Scenario: Missing Personio ID returns no detail
- **WHEN** a use case requests Personio-owned detail for a User without a stored Personio identifier
- **THEN** the provider-detail lookup returns no detail
- **THEN** routine user sync is not triggered as part of that lookup

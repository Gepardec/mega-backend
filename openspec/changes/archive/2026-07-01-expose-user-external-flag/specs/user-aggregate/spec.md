## ADDED Requirements

### Requirement: User external classification is derived from ZEP username prefix
The system SHALL classify a `User` as external if their `ZepUsername` starts with the letter "e". This classification SHALL be a pure derivation from the stored `ZepUsername` value — no additional field is persisted. The `ZepUsername` value object SHALL expose an `isExternal()` predicate. The `User` aggregate SHALL expose an `isExternal()` convenience predicate that delegates to its `ZepUsername`.

#### Scenario: User with ZEP username starting with "e" is external
- **WHEN** a `User` has a `ZepUsername` whose value begins with the letter "e"
- **THEN** `User.isExternal()` returns `true`

#### Scenario: User with ZEP username not starting with "e" is internal
- **WHEN** a `User` has a `ZepUsername` whose value does not begin with the letter "e"
- **THEN** `User.isExternal()` returns `false`

#### Scenario: External classification is case-sensitive
- **WHEN** a `User` has a `ZepUsername` whose value begins with the uppercase letter "E"
- **THEN** `User.isExternal()` returns `false`

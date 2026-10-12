## MODIFIED Requirements

### Requirement: User aggregate contains ZepProfile value object
The system SHALL model all ZEP-sourced fields in a `ZepProfile` value object nested within the `User` aggregate. The ZepProfile SHALL contain: username, email, firstname, lastname, title, salutation, workDescription, language, releaseDate, `employmentPeriods` (typed as `EmploymentPeriods` aggregate), and `regularWorkingTimes` (typed as `RegularWorkingTimes` aggregate).

#### Scenario: ZepProfile fields are updated via sync
- **WHEN** `user.syncFromZep(newZepProfile)` is called with updated data
- **THEN** the User's `zepProfile` is replaced with the new value object
- **THEN** no other fields on the User are affected

#### Scenario: ZepProfile holds EmploymentPeriods aggregate
- **WHEN** a `ZepProfile` is constructed
- **THEN** its `employmentPeriods` field is of type `EmploymentPeriods` (not a raw list)

#### Scenario: ZepProfile holds RegularWorkingTimes aggregate
- **WHEN** a `ZepProfile` is constructed
- **THEN** its `regularWorkingTimes` field is of type `RegularWorkingTimes` (not a raw list)

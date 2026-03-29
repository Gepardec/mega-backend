## MODIFIED Requirements

### Requirement: Sync fetches all active employees from ZEP
The system SHALL fetch the complete list of employees from ZEP via `ZepEmployeePort.fetchAll()` at the start of each sync. This list is the authoritative source for which Users should be active. The ZEP adapter SHALL wrap raw period and working-time lists into `EmploymentPeriods` and `RegularWorkingTimes` aggregates when constructing the `ZepProfile`.

#### Scenario: ZEP employees fetched at sync start
- **WHEN** `SyncUsersUseCase.sync()` is called
- **THEN** `ZepEmployeePort.fetchAll()` is called and returns a list of ZepProfile data

#### Scenario: ZepProfile returned by adapter contains aggregate types
- **WHEN** `ZepEmployeeAdapter` maps a ZEP response to a `ZepProfile`
- **THEN** the `employmentPeriods` field is an `EmploymentPeriods` instance
- **THEN** the `regularWorkingTimes` field is a `RegularWorkingTimes` instance

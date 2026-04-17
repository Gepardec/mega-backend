## ADDED Requirements

### Requirement: Clarification repository supports full-status queries for actor-scoped month-end overviews
The system SHALL provide two additional repository query methods that return all clarifications for an actor's scope for a given month, regardless of clarification status:

- `findAllEmployeeClarifications(UserId employeeId, YearMonth month)`: returns all `MonthEndClarification` records where the employee is the `subjectEmployeeId` for that month, including both `OPEN` and `DONE`.
- `findAllProjectLeadClarifications(UserId leadId, YearMonth month)`: returns all `MonthEndClarification` records for projects the lead leads for that month, including both `OPEN` and `DONE`.

The existing `findOpenEmployeeClarifications` and `findOpenProjectLeadClarifications` methods SHALL remain unchanged with their open-only semantics for use by the worklist.

#### Scenario: findAllEmployeeClarifications returns open and done clarifications
- **WHEN** `findAllEmployeeClarifications` is called for an employee who has both open and done clarifications as subject employee in that month
- **THEN** both open and done clarifications are returned

#### Scenario: findAllEmployeeClarifications excludes clarifications from other months
- **WHEN** `findAllEmployeeClarifications` is called for a given month
- **THEN** clarifications from other months are not returned

#### Scenario: findAllProjectLeadClarifications returns clarifications for all led projects
- **WHEN** `findAllProjectLeadClarifications` is called for a lead who leads multiple projects
- **THEN** clarifications for all those projects are returned, regardless of status

#### Scenario: findAllProjectLeadClarifications excludes clarifications from projects the lead does not lead
- **WHEN** `findAllProjectLeadClarifications` is called
- **THEN** clarifications from projects where the lead has no eligible-actor role are not returned

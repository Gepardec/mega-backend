## MODIFIED Requirements

### Requirement: Clarification creation and visibility follow involved-party rules
The system SHALL allow a clarification to be created only by an involved party. The involved parties of a clarification are: the subject employee (if present) and all eligible project leads. A clarification SHALL be visible to all involved parties.

#### Scenario: Employee-created clarification is visible to subject employee and all leads
- **WHEN** the subject employee creates a clarification for a project in their month-end project context
- **THEN** the clarification is visible to that employee
- **THEN** the clarification is visible to every eligible project lead in the clarification's lead snapshot

#### Scenario: Lead-created clarification for employee is visible to subject employee and all leads
- **WHEN** an eligible project lead creates a clarification for a project employee in that month-end context
- **THEN** the clarification is visible to the subject employee
- **THEN** the clarification is visible to every eligible project lead in the clarification's lead snapshot

#### Scenario: Project-level clarification is visible to eligible leads only
- **WHEN** a lead creates a project-level clarification with no subject employee
- **THEN** the clarification is visible to every eligible project lead in the clarification's lead snapshot
- **THEN** no subject employee has visibility because none exists

#### Scenario: Ineligible actor cannot create a clarification
- **WHEN** a user who is not an involved party attempts to create a clarification
- **THEN** the system rejects the creation attempt

### Requirement: Clarification repository supports full-status queries for actor-scoped month-end overviews
The system SHALL provide two additional repository query methods that return all clarifications for an actor's scope for a given month, regardless of clarification status:

- `findAllEmployeeClarifications(UserId employeeId, YearMonth month)`: returns all `MonthEndClarification` records where the employee is the `subjectEmployeeId` for that month, including both `OPEN` and `DONE`.
- `findAllProjectLeadClarifications(UserId leadId, YearMonth month)`: returns all `MonthEndClarification` records for projects the lead leads for that month, including both `OPEN` and `DONE`.

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

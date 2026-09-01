## ADDED Requirements

### Requirement: An employee can complete their own open time-check tasks in one operation
The system SHALL support completing, in a single operation, all `EMPLOYEE_TIME_CHECK` tasks for a given month whose subject employee is the acting actor, optionally narrowed to a single project. The operation SHALL complete only tasks that are currently open and whose eligible actor set includes the acting actor; matching tasks that are already done or for which the actor is not eligible SHALL be skipped without error, so partial success is a normal outcome. The operation SHALL NOT widen eligibility: an actor SHALL only ever complete time-check tasks for which they are the subject employee. Each completed task SHALL follow the same per-task completion rules and completer tracking used for single-task completion. The operation SHALL be atomic with respect to unexpected failures: if an unexpected error occurs while completing the set, no task in the set is completed.

#### Scenario: All open time-check tasks of the actor in the month are completed
- **WHEN** an employee bulk-completes their time-check tasks for a month without naming a project
- **THEN** every open `EMPLOYEE_TIME_CHECK` task in that month whose subject is that employee becomes `DONE`
- **THEN** each completed task records that employee as the completing actor
- **THEN** the operation returns the set of tasks it transitioned to `DONE`

#### Scenario: Scope is narrowed to one project when a project is named
- **WHEN** an employee bulk-completes their time-check tasks for a month and names a project
- **THEN** only their open time-check tasks for that project and month become `DONE`
- **THEN** their open time-check tasks on other projects are left unchanged

#### Scenario: Another employee's time-check tasks are never in scope
- **WHEN** an employee bulk-completes their time-check tasks for a month
- **THEN** time-check tasks whose subject is a different employee are left unchanged, including on projects the acting employee is assigned to

#### Scenario: Already-done tasks in scope are skipped
- **WHEN** the requested scope contains time-check tasks that are already `DONE`
- **THEN** those tasks remain `DONE` and keep their original completing actor
- **THEN** those tasks are not included in the set of newly completed tasks

#### Scenario: Empty scope completes nothing
- **WHEN** the acting employee has no open time-check tasks in the requested scope
- **THEN** no task is completed
- **THEN** the returned set of newly completed tasks is empty

#### Scenario: Bulk completion is atomic on unexpected failure
- **WHEN** an unexpected error occurs while completing the matching tasks
- **THEN** none of the tasks in the scope are left completed

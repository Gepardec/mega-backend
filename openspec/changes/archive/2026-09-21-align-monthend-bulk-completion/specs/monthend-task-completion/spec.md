## ADDED Requirements

### Requirement: Project leads can bulk complete one task type for a project
The system SHALL complete, in a single operation, the open month-end tasks of one task type for one project and month on which the acting project lead is an eligible actor, and SHALL return exactly the tasks it transitioned to `DONE`.

#### Scenario: Project lead completes all open tasks of one type for a project
- **WHEN** a project lead bulk-completes `PROJECT_LEAD_REVIEW` tasks for a project they lead in a month
- **THEN** every open `PROJECT_LEAD_REVIEW` task of that project and month whose eligible actor set includes that lead becomes `DONE`
- **THEN** each completed task records that lead as the completing actor
- **THEN** the operation returns the set of tasks it transitioned to `DONE`

#### Scenario: Lead with no tasks of the requested type receives an empty result
- **WHEN** a project lead bulk-completes `LEISTUNGSNACHWEIS` tasks for a project they lead that has no `LEISTUNGSNACHWEIS` tasks in that month
- **THEN** no task is completed
- **THEN** the returned set of newly completed tasks is empty

### Requirement: Project-lead bulk completion accepts only lead-completable task types
The project-lead bulk completion operation SHALL accept only the task types `LEISTUNGSNACHWEIS` and `PROJECT_LEAD_REVIEW`, and SHALL reject any other task type as invalid regardless of how the operation is invoked.

#### Scenario: Unsupported task types are rejected
- **WHEN** a project-lead bulk completion is requested for `EMPLOYEE_TIME_CHECK` or `ABRECHNUNG`
- **THEN** the operation is rejected as invalid
- **THEN** no task is completed

### Requirement: Project-lead bulk completion is authorized by generation-time task eligibility
The project-lead bulk completion operation SHALL reject the acting actor as not authorized unless they lead the project in that month. An actor leads a project in a month when they are an eligible actor on at least one lead-eligible task of that project in that month, as recorded at generation time. Changes to project leads after generation SHALL NOT affect this.

#### Scenario: Lead removed from the project after generation can still bulk complete
- **WHEN** a project lead was an eligible actor on the project's lead-eligible tasks at generation time and has since been removed as lead of the project
- **THEN** a project-lead bulk completion by that lead for that project and month completes the open tasks of the requested type on which they are eligible

#### Scenario: Lead added to the project after generation is not authorized
- **WHEN** an actor became lead of a project after that month's tasks were generated and is an eligible actor on none of the project's tasks for that month
- **THEN** a project-lead bulk completion by that actor for that project and month is rejected as not authorized
- **THEN** no task is completed

#### Scenario: Actor who does not lead the project is not authorized
- **WHEN** an actor who is not an eligible actor on any lead-eligible task of a project in a month requests a project-lead bulk completion for that project and month
- **THEN** the operation is rejected as not authorized

### Requirement: Employees can bulk complete their own time-check tasks
The system SHALL complete, in a single operation, the open `EMPLOYEE_TIME_CHECK` tasks of one month whose subject employee is the acting actor, across all their projects. The operation SHALL NOT complete any other task type, even when the actor is an eligible actor on it.

#### Scenario: Employee completes all their open time-check tasks for a month
- **WHEN** an employee bulk-completes their time-check tasks for a month
- **THEN** every open `EMPLOYEE_TIME_CHECK` task in that month whose subject is that employee becomes `DONE`, across all their projects
- **THEN** each completed task records that employee as the completing actor
- **THEN** the operation returns the set of tasks it transitioned to `DONE`

#### Scenario: Another employee's time-check tasks are never completed
- **WHEN** an employee bulk-completes their time-check tasks for a month
- **THEN** time-check tasks whose subject is a different employee are left unchanged, including on projects the acting employee is assigned to

#### Scenario: A lead's own project tasks are not completed as an employee
- **WHEN** a project lead who is also assigned as an employee on a project they lead bulk-completes their time-check tasks for a month
- **THEN** their `EMPLOYEE_TIME_CHECK` task for that project becomes `DONE`
- **THEN** the `PROJECT_LEAD_REVIEW` and `LEISTUNGSNACHWEIS` tasks of which they are the subject remain open

### Requirement: Bulk completion skips non-completable tasks and is atomic
Every bulk completion operation SHALL skip, without error, tasks in its scope that are already done or on which the acting actor is not eligible, so partial success is a normal outcome. Each completed task SHALL follow the same per-task completion rules and completer tracking as single-task completion. If an unexpected error occurs while completing the tasks, no task in the scope SHALL be left completed.

#### Scenario: Already-done tasks in scope are skipped
- **WHEN** the requested scope contains tasks that are already `DONE`
- **THEN** those tasks remain `DONE` and keep their original completing actor
- **THEN** those tasks are not included in the set of newly completed tasks

#### Scenario: Re-running an already-completed scope completes nothing
- **WHEN** the same scope is bulk-completed again after all of its tasks are already `DONE`
- **THEN** no task is completed
- **THEN** the returned set of newly completed tasks is empty

#### Scenario: Tasks the actor is not eligible for are skipped, not failed
- **WHEN** the requested scope contains a task whose eligible actor set does not include the acting actor
- **THEN** that task is left unchanged
- **THEN** the operation does not fail and continues completing the remaining eligible tasks

#### Scenario: Bulk completion is atomic on unexpected failure
- **WHEN** an unexpected error occurs while completing the tasks in scope
- **THEN** none of the tasks in the scope are left completed

## REMOVED Requirements

### Requirement: Eligible actors can complete a scoped set of same-type tasks in one operation
**Reason**: Replaced by focused requirements: "Project leads can bulk complete one task type for a project", "Project-lead bulk completion accepts only lead-completable task types", "Project-lead bulk completion is authorized by generation-time task eligibility", and the shared "Bulk completion skips non-completable tasks and is atomic". Lead authorization now follows eligibility recorded at generation time, and the type restriction is a rule of the operation itself.
**Migration**: See the requirements above. Callers that relied on the operation accepting any task type must use single-task completion for `EMPLOYEE_TIME_CHECK` and `ABRECHNUNG`.

### Requirement: An employee can complete their own open time-check tasks in one operation
**Reason**: Replaced by "Employees can bulk complete their own time-check tasks" and the shared "Bulk completion skips non-completable tasks and is atomic", so the shared bulk rules are stated once.
**Migration**: See the requirements above. Behavior is unchanged.

## ADDED Requirements

### Requirement: Eligible actors can complete a scoped set of same-type tasks in one operation
The system SHALL support completing, in a single operation, all month-end tasks that match a given month, project, and task type. The operation SHALL complete only tasks that are currently open and whose eligible actor set includes the acting actor; matching tasks that are already done or for which the actor is not eligible SHALL be skipped without error, so partial success is a normal outcome. Each completed task SHALL follow the same per-task completion rules and completer tracking used for single-task completion. The operation SHALL be atomic with respect to unexpected failures: if an unexpected error occurs while completing the set, no task in the set is completed.

#### Scenario: All open eligible tasks in scope are completed
- **WHEN** an eligible project lead bulk-completes all tasks of one lead-eligible type for a project in a month
- **THEN** every such open task whose eligible actor set includes that lead becomes `DONE`
- **THEN** each completed task records that lead as the completing actor
- **THEN** the operation returns the set of tasks it transitioned to `DONE`

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
- **WHEN** an unexpected error occurs while completing the matching tasks
- **THEN** none of the tasks in the scope are left completed

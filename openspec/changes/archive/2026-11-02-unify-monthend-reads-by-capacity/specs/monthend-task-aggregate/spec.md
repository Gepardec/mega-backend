## ADDED Requirements

### Requirement: Each month-end task type is the duty of one capacity
Every month-end task type SHALL be the duty of exactly one capacity. `EMPLOYEE_TIME_CHECK` SHALL be a duty of the `EMPLOYEE` capacity. `LEISTUNGSNACHWEIS`, `PROJECT_LEAD_REVIEW`, and `ABRECHNUNG` SHALL be duties of the `CONTROLLING` capacity. The duty SHALL only decide where a task is offered as completable. It SHALL NOT change who may complete the task.

#### Scenario: Time check is an employee duty
- **WHEN** the duty of an `EMPLOYEE_TIME_CHECK` task is determined
- **THEN** it is the `EMPLOYEE` capacity

#### Scenario: Lead-eligible task types are controlling duties
- **WHEN** the duty of a `LEISTUNGSNACHWEIS`, `PROJECT_LEAD_REVIEW`, or `ABRECHNUNG` task is determined
- **THEN** it is the `CONTROLLING` capacity

#### Scenario: Duty does not restrict completion eligibility
- **WHEN** an actor in a task's eligible actor set completes that task
- **THEN** the completion succeeds regardless of the task type's duty

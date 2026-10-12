# MonthEnd Employee Project Context

## Purpose

Defines the monthend employee-project context capability that validates whether a specific employee can act on a specific project for a payroll month and assembles the project, employee, and eligible lead references needed by monthend use cases.

## Requirements

### Requirement: Domain service validates employee–project context for monthend operations
The system SHALL provide a domain service `MonthEndEmployeeProjectContextService` in `monthend.domain.services` that validates and assembles the context required for any monthend operation involving a specific employee and project. The service SHALL verify that the project is active for the given month, that the subject employee is active for the given month, and that the employee is assigned to the project in ZEP. On success it SHALL return a `MonthEndEmployeeProjectContext` containing the project snapshot, the subject employee reference, and the set of lead IDs that are also active for that month.

#### Scenario: Valid context assembled successfully
- **WHEN** `MonthEndEmployeeProjectContextService.resolve(month, projectId, subjectEmployeeId)` is called with a project active in that month, a subject employee active in that month, and the subject employee assigned to the project in ZEP
- **THEN** the service returns a `MonthEndEmployeeProjectContext` containing the matching project snapshot, the subject employee `UserRef`, and the filtered set of active lead IDs

#### Scenario: Unknown or inactive project raises error
- **WHEN** no project matching `projectId` is active in the given month according to `MonthEndProjectSnapshotPort`
- **THEN** the service throws `MonthEndProjectContextNotFoundException`

#### Scenario: Unknown or inactive employee raises error
- **WHEN** no user matching `subjectEmployeeId` is active in the given month according to `MonthEndUserSnapshotPort`
- **THEN** the service throws `MonthEndEmployeeContextNotFoundException`

#### Scenario: Employee not assigned to project raises error
- **WHEN** the subject employee's ZEP username is not in the set of assigned usernames returned by `MonthEndProjectAssignmentPort` for that project and month
- **THEN** the service throws `MonthEndEmployeeNotAssignedToProjectException`

### Requirement: MonthEndEmployeeProjectContext carries only active leads
The `MonthEndEmployeeProjectContext` SHALL contain only the subset of a project's lead IDs that correspond to users active in the given month. Lead IDs that cannot be found in the active user snapshot for that month SHALL be excluded silently.

#### Scenario: Inactive lead is excluded from context
- **WHEN** a project's leads set contains a UserId that has no matching active user for the given month
- **THEN** that UserId is absent from `MonthEndEmployeeProjectContext.eligibleProjectLeadIds()`

#### Scenario: All leads active results in full lead set in context
- **WHEN** all lead IDs in a project's leads set have matching active users for the given month
- **THEN** `MonthEndEmployeeProjectContext.eligibleProjectLeadIds()` contains all of them

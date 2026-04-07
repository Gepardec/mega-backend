# Month-End Status Overview

## Purpose

Defines a status-oriented month-end overview for matrix or dashboard-style views that need visibility into both open and completed month-end obligations without changing the existing open-only worklist semantics.

## Requirements

### Requirement: Status overview is derived from relevant month-end tasks for an actor and month
The system SHALL provide a month-end status overview for a requesting actor and month by querying all `MonthEndTask` obligations that are visible to that actor, including both `OPEN` and `DONE` tasks. A task is visible to an actor if the actor is in `eligibleActorIds` OR is the `subjectEmployeeId` of that task.

#### Scenario: Employee overview includes open and completed employee-owned tasks
- **WHEN** an employee requests their month-end status overview for a month in which some of their employee-owned month-end tasks are open and others are done
- **THEN** the system returns both the open and completed employee-owned tasks in the overview

#### Scenario: Lead overview includes completed shared tasks
- **WHEN** a project lead requests their month-end status overview for a month after an eligible lead has completed a shared lead-eligible month-end task
- **THEN** the system still returns that completed shared task in the lead's overview

#### Scenario: Employee overview includes PROJECT_LEAD_REVIEW tasks about that employee
- **WHEN** an employee requests their month-end status overview for a month that contains `PROJECT_LEAD_REVIEW` tasks where that employee is the subject
- **THEN** the system returns those `PROJECT_LEAD_REVIEW` tasks in the employee's overview

#### Scenario: Employee sees at most one entry per task even when subject and eligible
- **WHEN** an employee requests their month-end status overview and has tasks where they are both the subject employee and in the eligible actor set
- **THEN** each such task appears exactly once in the overview

### Requirement: Status overview entries expose the context needed for matrix or dashboard rendering
The system SHALL include the month-end task identity, task type, task status, a project reference object containing the project identifier and project name, and a nullable subject employee reference object containing the employee identifier and full name when present in each overview entry.

#### Scenario: Review task entry identifies the employee and named project
- **WHEN** the overview contains a `PROJECT_LEAD_REVIEW` task
- **THEN** that overview entry includes both a subject employee reference object and a project reference object
- **THEN** the subject employee reference object includes the associated employee identifier and full name
- **THEN** the project reference object includes the associated project identifier and project name

#### Scenario: Abrechnung entry identifies the named project without a subject employee
- **WHEN** the overview contains an `ABRECHNUNG` task
- **THEN** that overview entry includes a project reference object with the associated project identifier and project name
- **THEN** that overview entry includes no subject employee reference object

### Requirement: Status overview entries expose whether the actor can complete the task
The system SHALL include a `canComplete` flag on each overview entry indicating whether the requesting actor is eligible to complete that task. `canComplete` SHALL be `true` if and only if the actor is in `eligibleActorIds` for that task.

#### Scenario: Eligible actor sees canComplete true for their own task
- **WHEN** an employee requests their status overview and an entry corresponds to a task where that employee is in `eligibleActorIds`
- **THEN** that overview entry has `canComplete` set to `true`

#### Scenario: Subject-only actor sees canComplete false for a PROJECT_LEAD_REVIEW task
- **WHEN** an employee requests their status overview and an entry corresponds to a `PROJECT_LEAD_REVIEW` task where they are the subject employee but not in `eligibleActorIds`
- **THEN** that overview entry has `canComplete` set to `false`

#### Scenario: Eligible always wins when actor is both subject and eligible
- **WHEN** an actor is both the subject employee and in `eligibleActorIds` for a task
- **THEN** that overview entry has `canComplete` set to `true`

### Requirement: Completed overview entries retain completion details
The system SHALL expose the completing actor for `DONE` month-end tasks in the status overview.

#### Scenario: Shared task shows who completed it
- **WHEN** a shared lead-eligible month-end task appears as `DONE` in the status overview
- **THEN** the overview entry includes the eligible actor who completed that task

#### Scenario: Open task has no completer information
- **WHEN** an `OPEN` month-end task appears in the status overview
- **THEN** the overview entry does not include a completing actor

### Requirement: Worklists remain focused while the status overview preserves status visibility
The system SHALL keep the existing month-end worklists open-only even when the status overview shows completed tasks.

#### Scenario: Completed employee task stays visible in overview but not in worklist
- **WHEN** an employee completes one of their month-end tasks
- **THEN** that task remains visible as `DONE` in the employee's status overview and no longer appears in the employee's open worklist

#### Scenario: Completed shared lead task stays visible in overview but disappears from open lead worklists
- **WHEN** one eligible lead completes a shared lead-eligible month-end task
- **THEN** that task remains visible as `DONE` in every eligible lead's status overview and no longer appears in any eligible lead's open worklist

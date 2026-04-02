# Month-End Clarifications

## Purpose

Defines the `MonthEndClarification` aggregate for user-created month-end follow-up subtasks, including visibility, editability, resolution, and their independence from generated `MonthEndTask` obligations.

## Requirements

### Requirement: Month-end clarifications model user-created follow-up subtasks
The system SHALL represent each month-end clarification as a `MonthEndClarification` scoped to one month, one project, and one subject employee. A clarification SHALL store its creator, creator side, open or done status, clarification text, optional resolution note, eligible project lead snapshot, `createdAt`, optional `resolvedAt`, and `lastModifiedAt`.

#### Scenario: Employee-created clarification is initialized as open
- **WHEN** the subject employee creates a clarification for a project in a month-end context
- **THEN** the system creates one `MonthEndClarification` for that month, project, and subject employee
- **THEN** the clarification status is `OPEN`
- **THEN** `createdBy`, `createdAt`, and `lastModifiedAt` are populated

#### Scenario: Lead-created clarification is initialized as open
- **WHEN** an eligible project lead creates a clarification for a project employee in a month-end context
- **THEN** the system creates one `MonthEndClarification` for that month, project, and subject employee
- **THEN** the clarification status is `OPEN`
- **THEN** the clarification stores the creating lead as `createdBy`

### Requirement: Clarifications preserve month-end lead eligibility at creation time
The system SHALL snapshot the eligible project leads of a clarification when it is created. Employee-created clarifications SHALL be resolvable by any lead in that snapshot. Lead-created clarifications SHALL remain visible and editable to the leads in that snapshot while open.

#### Scenario: Employee-created clarification captures all eligible leads
- **WHEN** the subject employee creates a clarification and the project has multiple eligible leads in that month-end context
- **THEN** the clarification stores that lead set as its eligible project lead snapshot

#### Scenario: Lead assignment changes do not alter an open clarification
- **WHEN** project lead assignments change after a clarification has already been created
- **THEN** the open clarification keeps its original eligible project lead snapshot

### Requirement: Clarification creation and visibility follow employee and lead roles
The system SHALL allow clarifications to be created only by the subject employee or by an eligible project lead of the same month-end project context. An employee-created clarification SHALL be visible to the subject employee and all eligible project leads. A lead-created clarification SHALL be visible to the subject employee and all eligible project leads.

#### Scenario: Employee creates a clarification for their project context
- **WHEN** the subject employee creates a clarification for a project in their month-end worklist context
- **THEN** the clarification is visible to that employee
- **THEN** the clarification is visible to every eligible project lead in the clarification's lead snapshot

#### Scenario: Eligible lead creates a clarification for a project employee
- **WHEN** an eligible project lead creates a clarification for a project employee in that month-end context
- **THEN** the clarification is visible to the subject employee
- **THEN** the clarification is visible to every eligible project lead in the clarification's lead snapshot

#### Scenario: Ineligible actor cannot create a clarification
- **WHEN** a user who is neither the subject employee nor an eligible project lead attempts to create a clarification
- **THEN** the system rejects the creation attempt

### Requirement: Open clarification text is editable by the creator side
The system SHALL allow edits to the main clarification text only while the clarification is `OPEN`, and only by actors on the creator side. For employee-created clarifications, the subject employee SHALL be the only creator-side editor. For lead-created clarifications, any eligible lead in the clarification's lead snapshot SHALL be allowed to edit the text.

#### Scenario: Employee edits their open clarification
- **WHEN** the subject employee edits the text of an open employee-created clarification
- **THEN** the clarification text is updated
- **THEN** `lastModifiedAt` is updated

#### Scenario: Another eligible lead edits a lead-created clarification
- **WHEN** an eligible project lead edits the text of an open clarification that was created by another eligible lead
- **THEN** the clarification text is updated
- **THEN** `lastModifiedAt` is updated

#### Scenario: Resolver side cannot edit creator-side text
- **WHEN** the resolver side attempts to edit the text of an open clarification
- **THEN** the system rejects the edit attempt

#### Scenario: Done clarification text cannot be edited
- **WHEN** any actor attempts to edit the text of a clarification with status `DONE`
- **THEN** the system rejects the edit attempt

### Requirement: Clarifications resolve to the opposite side with an optional resolution note
The system SHALL complete clarifications through a dedicated clarification completion flow. Employee-created clarifications SHALL be resolvable by any eligible project lead in the clarification's lead snapshot. Lead-created clarifications SHALL be resolvable only by the subject employee. Resolution SHALL set the clarification status to `DONE`, populate `resolvedBy` and `resolvedAt`, and MAY store a resolution note.

#### Scenario: Eligible lead resolves an employee-created clarification
- **WHEN** an eligible project lead resolves an open employee-created clarification
- **THEN** the clarification status becomes `DONE`
- **THEN** `resolvedBy` and `resolvedAt` are populated

#### Scenario: Employee resolves a lead-created clarification with a note
- **WHEN** the subject employee resolves an open lead-created clarification and provides a resolution note
- **THEN** the clarification status becomes `DONE`
- **THEN** the resolution note is stored with the clarification

#### Scenario: Ineligible actor cannot resolve a clarification
- **WHEN** a user that is not allowed to resolve the clarification attempts completion
- **THEN** the system rejects the completion attempt

### Requirement: Clarifications do not block generated month-end task completion
The system SHALL treat clarification subtasks as independent from the generated `MonthEndTask` obligations in the same month-end context.

#### Scenario: Generated task can complete while clarification remains open
- **WHEN** a generated month-end task is completed for a project employee that still has an open clarification
- **THEN** the generated task completes successfully
- **THEN** the clarification remains open until it is resolved through the clarification flow

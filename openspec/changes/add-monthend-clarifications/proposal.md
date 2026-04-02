## Why

The month-end process needs a lightweight way for employees and project leads to raise project-specific follow-up items without leaving the month-end context. The existing `MonthEndTask` model covers generated checklist obligations, but it does not represent user-created clarification subtasks that carry text, fixed responsibility, and their own resolution lifecycle.

## What Changes

- Introduce a new `MonthEndClarification` capability for user-created month-end clarification subtasks scoped to a month, project, and subject employee.
- Allow employees to create clarification subtasks that are visible to the employee and all eligible project leads for that project-month context, with resolution owned by any eligible project lead.
- Allow eligible project leads to create clarification subtasks for a project employee that are visible to the employee and all eligible project leads, with resolution owned by the subject employee.
- Support editing open clarification text while preserving fixed responsibility, and support an optional resolution note when the clarification is completed.
- Track clarification lifecycle metadata including creator, resolver, `OPEN`/`DONE` status, `createdAt`, `resolvedAt`, and `lastModifiedAt`.
- Extend month-end worklists to surface visible clarification subtasks next to generated month-end tasks without making clarifications block task completion.

## Capabilities

### New Capabilities
- `monthend-clarifications`: User-created clarification subtasks for month-end project follow-up, including creation, visibility, editing, and completion rules.

### Modified Capabilities
- `monthend-task-worklist`: Worklists also expose clarification subtasks that are visible in the actor's month-end project context.

## Impact

- Affected domain and application areas: `com.gepardec.mega.hexagon.monthend.domain.*` and `com.gepardec.mega.hexagon.monthend.application.*`
- New persistence model and Liquibase changelog for `MonthEndClarification`
- Updated worklist read model and any inbound adapters that expose month-end worklists
- New tests for clarification aggregate behavior, visibility rules, editing, completion, and worklist composition

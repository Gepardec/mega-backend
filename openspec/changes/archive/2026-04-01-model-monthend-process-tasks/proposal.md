## Why

Month end is one business process that includes obligations for employees and obligations for project leads. The domain model should express those obligations with one consistent task concept while still supporting different completion rules for an individual actor and a set of eligible lead actors.

## What Changes

- Introduce a unified `MonthEndTask` domain concept for all month-end obligations.
- Model month-end work as one process that contains both employee-owned tasks and project-owned tasks with eligible lead actors.
- Model aggregate semantics around a single month-end task and its completion policy.
- Treat employee and project-lead checklists as read-side worklists over the same set of month-end tasks.
- Define generation, completion, and querying behavior around the unified task model.
- **BREAKING** Define month-end persistence and use cases around the fresh task-based model.

## Capabilities

### New Capabilities
- `monthend-task-aggregate`: Defines the unified month-end task aggregate, its completion policies, and invariants.
- `monthend-task-generation`: Defines how month-end tasks are generated for active employees, projects, assignments, and project leads.
- `monthend-task-completion`: Defines how employee-owned and lead-eligible tasks are completed through the unified task model.
- `monthend-task-worklist`: Defines actor-specific worklists that present open month-end tasks for employees and project leads.

### Modified Capabilities

None.

## Impact

- Affects the `com.gepardec.mega.hexagon.monthend` domain, application services, outbound ports, persistence adapters, and Liquibase schema.
- Introduces a single month-end task repository model and supporting query ports for worklists.
- Changes the shape of month-end generation and completion use cases and their tests.
- Introduces read-side worklist queries for employee and project-lead views.

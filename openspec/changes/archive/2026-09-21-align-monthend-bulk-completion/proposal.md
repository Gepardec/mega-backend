## Why

Two archived changes, `bulk-complete-monthend-tasks` and `employee-bulk-complete-own-time-checks`, added bulk task completion for project leads and employees. They were specified and built separately, and a joint review found problems that should be fixed before the frontend builds on these endpoints:

- **Lead authorization uses the wrong source of truth.** Project-lead bulk completion checks the caller against the project's *current* lead set. Everything else a lead sees and does uses the eligible actors recorded on each task at generation time: which projects the overview shows, the `canComplete` flag, and single-task completion. So a lead removed after generation sees the tasks as completable but gets `403` on bulk completion. A lead added after generation gets a silent `200` with an empty list. This breaks the rule that tasks keep the actor eligibility they had at generation time.
- **A business rule sits in the transport layer.** The rule that only `LEISTUNGSNACHWEIS` and `PROJECT_LEAD_REVIEW` can be bulk completed by a lead is enforced only in the REST adapter. Any other caller of the operation bypasses it.
- **Naming departs from the bounded context's conventions.** Contract schemas and use cases use ad-hoc names (`CompleteProjectTasksRequest`, `CompleteOwnTimeChecksRequest`, `CompletedTasksResponse`, "own", "by type"). The rest of the month-end context names things by role (employee / project lead). Contract schema names become the frontend's generated types, so this is the cheapest time to fix them.
- **The specs are duplicated and incomplete.** The bulk rules (partial success, skip-not-fail, atomicity) appear twice. The two endpoints are not described as a role-suffixed pair like the other month-end endpoints. The role-access and authenticated-actor requirements do not mention them. The employee endpoint's "empty body" scenario contradicts a contract that requires a body. And its optional `projectId` adds nothing: generation creates exactly one `EMPLOYEE_TIME_CHECK` per employee, project, and month, so a project-scoped request completes a single task, which single-task completion already does by task ID.

## What Changes

- **Task-based lead authorization.** A project lead may bulk complete a project's tasks when they lead that project in that month. "Leading" means being an eligible actor on at least one lead-eligible task of that project in that month, which is the same rule the project-lead status overview already uses. Eligibility always comes from the tasks as generated. Later changes to a project's leads no longer affect bulk completion.
- **BREAKING (status code):** A request for a project the caller does not lead in that month is rejected with `403`. This includes an unknown project or a project with no month-end tasks for that month, which currently get `400`. A caller who leads the project but has no open tasks of the requested type (for example, `LEISTUNGSNACHWEIS` on a non-billable project) gets `200` with an empty `completed` list.
- **The type restriction becomes a domain rule.** Only `LEISTUNGSNACHWEIS` and `PROJECT_LEAD_REVIEW` can be bulk completed within a project-lead scope. The operation enforces this for every caller, not just the REST adapter. The HTTP behavior stays the same (`400`).
- **BREAKING: the employee endpoint takes no request body.** `POST /monthend/{month}/tasks/complete/employee` always completes all of the caller's open time checks in that month, across all projects. The optional `projectId` is removed. To complete the time check for one project, a client uses single-task completion (`POST /monthend/tasks/{taskId}/complete`) with the task ID from the status overview.
- **BREAKING (generated client types only, the JSON stays the same):** The bulk completion request and response schemas are renamed to the context's role-based naming. Proposed names: `CompleteProjectLeadMonthEndTasksRequest` and `MonthEndTaskCompletion` for the response (mirroring `MonthEndTaskGeneration`). The employee request schema is removed along with the body. Paths, operation IDs, and the remaining JSON field names do not change.
- **Specs are restructured.** The two overlapping bulk-completion requirements in each capability are replaced by focused requirements: one per scope and rule, plus one for the rules both flows share. The role-access and authenticated-actor requirements now cover the bulk endpoints.
- **Code cleanup, no behavior change.** Use cases and services are renamed to the role-based pattern, with a consistent parameter order. The employee-only repository query, with its nullable "all projects" argument and duplicated query branches, is removed in favor of the existing lookup of an employee's open subject tasks. Task type mapping goes through the REST mapper instead of hand-written code. The resource's wildcard import and formatting are fixed.

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `monthend-task-completion`: the two bulk-completion requirements are replaced by focused requirements for the lead scope, the lead type restriction, lead authorization, the employee scope, and the shared skip/atomicity rules. Lead authorization is defined by task eligibility recorded at generation time, and the lead type restriction becomes a rule of the operation itself.
- `monthend-rest-api`: the two bulk endpoint requirements are replaced by one requirement per role-suffixed endpoint, one for the project-lead endpoint's error responses, and one for the shared response shape. An unknown or not-led project now returns `403` instead of `400`, and the employee endpoint takes no request body. The authenticated-actor and role-access requirements now include the bulk completion endpoints.

## Impact

- **API contract:** the project-lead request schema and the shared response schema are renamed, and the employee endpoint's request body and schema are removed. Paths and operation IDs do not change. The frontend's generated types must be regenerated, any code that relies on `400` for an unknown project must expect `403`, and any caller that sends a `projectId` to the employee endpoint must switch to single-task completion.
- **Application layer:** the project-lead bulk use case no longer depends on project context resolution. It authorizes the caller from the project's lead-eligible tasks for that month and enforces the type restriction itself. Both bulk use cases are renamed.
- **Domain:** the set of task types a lead can bulk complete is defined on the task type model instead of in the REST adapter.
- **Persistence:** the repository port gets one new existence check for lead authorization. The project-lead scope query is renamed and returns open tasks only. The employee-only time-check query is removed, and the employee flow reuses the existing open subject-task lookup.
- **REST adapter:** it only parses input, maps types, and shapes the response. The type validation moves out.
- **Tests:** new cases for lead changes after generation (a removed lead can still bulk complete, an added lead is rejected), `403` for an unknown project, an empty result for a type with no tasks, the type rule enforced at use-case level, and the employee endpoint called without a body.
- **Unchanged:** the `MonthEndTask` aggregate, task generation, single-task completion, the status overview, and eligibility rules.

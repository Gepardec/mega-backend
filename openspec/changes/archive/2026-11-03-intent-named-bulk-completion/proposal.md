## Why

The two bulk-completion endpoints are the only month-end commands whose paths still carry a role suffix: `POST /monthend/{month}/tasks/complete/project-lead` and `POST /monthend/{month}/tasks/complete/employee`. After `unify-monthend-reads-by-capacity`, the API follows one rule: reads are selected by capacity, and commands are named by what they act on and authorized by actor + record. A role suffix on a command path suggests a "view" that commands no longer have. It also hides what each operation targets: one completes a project's lead tasks, the other the caller's own time checks.

## What Changes

- **BREAKING:** `POST /monthend/{month}/tasks/complete/project-lead` with body `{ projectId, type }` becomes `POST /monthend/{month}/projects/{projectId}/tasks/complete` with body `{ type }`. The project moves from the body into the path because it identifies the resource being acted on.
- **BREAKING:** `POST /monthend/{month}/tasks/complete/employee` becomes `POST /monthend/{month}/my-tasks/complete`. It still takes no request body.
- Behavior does not change: required roles, the `400` for unsupported task types, the `403` for projects the caller does not lead (including unknown projects), the empty-result cases, the `{ "completed": [...] }` response, and atomicity.
- **BREAKING (generated client types only):** the project-lead request schema loses its `projectId` field. Operation IDs and schema names stay the same.
- No deprecation period. The old paths are removed in this change.

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `monthend-rest-api`: the two role-suffixed bulk endpoint requirements are replaced by requirements for the intent-named paths, and the role-access requirement's bulk scenarios refer to the new paths.

`monthend-task-completion` does not change: the bulk operations, their scopes, authorization and atomicity stay as they are.

## Impact

- **API contract:** two paths change, the project-lead request body drops `projectId`, and a `projectId` path parameter is added. The frontend must switch paths, move `projectId` into the path, and regenerate clients. Frontend work is out of scope for this change.
- **REST adapter:** `MonthEndResource` reads `projectId` from the path instead of the body. The use cases and their signatures stay the same.
- **Unchanged:** application services, domain, persistence, and the single-task completion endpoint.
- **Sequencing:** apply after `unify-monthend-reads-by-capacity`. Both modify the `monthend-rest-api` role-access requirement, and this change's delta is written on top of that change's version of it.

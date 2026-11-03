# Design

## Context

Both bulk endpoints are implemented in `MonthEndResource`. They delegate to `CompleteProjectLeadMonthEndTasksUseCase.complete(month, projectId, type, actorId)` and `CompleteEmployeeMonthEndTasksUseCase.complete(month, actorId)`. The use cases already carry all the rules: type restriction, task-based lead authorization, scope, and atomicity. This change only touches the contract and the adapter.

## Goals / Non-Goals

**Goals:**
- Paths that name the target of the command. No change to behavior.

**Non-Goals:**
- Renaming operation IDs, schema names or use cases.
- Changing status codes, the response shape or authorization.

## Decisions

### D1 — Only paths change; operation IDs, schemas and use-case names stay

`completeProjectLeadMonthEndTasks` and `completeEmployeeMonthEndTasks`, the request schema `CompleteProjectLeadMonthEndTasksRequest`, and both use cases keep their names. Those names describe the business operation ("a project lead signs off a project's tasks", "an employee completes their time checks"), which is an intent, not a view. Only the path tried to express the caller's role, and it was the only part implying a capacity.

*Alternative:* also rename the operation IDs and schemas (e.g. `completeProjectMonthEndTasks`, `completeMyMonthEndTasks`). Rejected: that adds generated-client churn for no change in meaning. It can be done separately if the naming ever stops matching the operations.

### D2 — `projectId` becomes a typed path parameter

The new path declares `projectId` as `format: uuid`, so the generator types it as `UUID`, like `taskId` on single-task completion. A malformed id is rejected by the framework's parameter conversion with a client error, consistent with `/monthend/tasks/{taskId}/complete`. The request schema drops `projectId` and keeps `type` as required.

### D3 — Path shape

```
POST /monthend/{month}/projects/{projectId}/tasks/complete   { "type": ... }
POST /monthend/{month}/my-tasks/complete
```

`my-tasks` was chosen over the `/me/...` style (as in `/users/me`) because it reads as "my tasks of this month" under the month resource. It does not collide with existing paths.

## Risks / Trade-offs

- [This change and `unify-monthend-reads-by-capacity` both modify the `monthend-rest-api` role-access requirement] → This change's delta is derived from that change's version. Archive `unify-monthend-reads-by-capacity` first. If its version of that requirement is edited before archiving, re-derive this delta before applying.
- [The frontend breaks on deploy (hard cut)] → Ship with the frontend migration.

## Migration Plan

Apply after `unify-monthend-reads-by-capacity` is archived. Deploy together with the frontend release that switches paths and regenerates the month-end client. Rollback means reverting both.

## Context

The month-end domain needs to express one business process with different actor responsibilities. A month-end obligation can be completed either by exactly one assigned actor or by any actor from a fixed eligible set, but both kinds of obligation still belong to the same process language.

This design treats month end as one business process composed of many month-end tasks. Employee and project-lead checklists are views over those tasks, while the write model stays focused on one task at a time.

This work happens in active development, so the schema can be replaced directly instead of migrated incrementally.

## Goals / Non-Goals

**Goals:**
- Model all month-end obligations with one aggregate type and one consistent language.
- Keep the aggregate boundary small so completion remains a single-aggregate transaction.
- Support both employee-owned and lead-eligible tasks without duplicating the business fact across multiple aggregates.
- Make actor-specific checklists query concerns derived from the same task set.
- Keep generation and completion use cases aligned with the new model.

**Non-Goals:**
- Introducing a large `MonthEndProcess` aggregate that owns every task for a month or project.
- Solving unrelated UI concerns beyond the read-side worklist requirements.
- Adding dynamic reassignment when project leads change after generation.

## Decisions

### 1. Use `MonthEndTask` as the single aggregate root

The core aggregate will be `MonthEndTask`. Each instance represents one obligation in the month-end process and owns all invariants required to complete that obligation safely.

Proposed business shape:

- `id`
- `month`
- `type`
- `projectId`
- `subjectEmployeeId?`
- `eligibleActorIds`
- `completionPolicy`
- `status`
- `completedBy?`

This keeps completion consistency where it belongs: on one obligation at a time.

Alternative considered:
- Use multiple write-model aggregate types for different actor groups.
- Rejected because the core business concept is still one month-end obligation, and splitting the write model would fragment that language.

### 2. Model completion semantics explicitly with a policy/value object

The aggregate will not infer completion rules indirectly from task type. Instead, it will use an explicit policy such as:

- `INDIVIDUAL_ACTOR`
- `ANY_ELIGIBLE_ACTOR`

For employee tasks, `eligibleActorIds` contains exactly one employee and the policy is `INDIVIDUAL_ACTOR`.
For project-lead tasks, `eligibleActorIds` contains the fixed set of leads captured at generation time and the policy is `ANY_ELIGIBLE_ACTOR`.

Alternative considered:
- Encode all completion rules directly in `MonthEndTaskType`.
- Rejected because the completion rule is a distinct domain concern and making it explicit keeps the aggregate behavior simpler and easier to test.

### 3. Treat `MonthEndProcess` as a business concept, not the transactional aggregate

The business speaks about “the month-end process”, and the model should reflect that language. In code, `MonthEndProcess` is best represented as the conceptual process formed by a set of generated tasks for a month, not as one large aggregate root that loads and mutates everything together.

Alternative considered:
- Create a single `MonthEndProcess` aggregate owning all tasks for a month or for a project-month.
- Rejected because it would create a large transactional boundary, increase contention, and violate the “one aggregate per true consistency boundary” guidance.

### 4. Model checklists as worklist queries

Employee and project-lead views will be modeled as read-side worklists derived from `MonthEndTask` data. A worklist groups open tasks for an actor and month, but it does not own task state.

That means:
- The domain write model owns task lifecycle.
- Application query ports assemble employee and lead worklists.
- Presentation can still use “checklist” language, but the backing model is query-based.

Alternative considered:
- Persist worklists as their own write-model containers.
- Rejected because worklists group obligations for presentation and querying, but they do not protect a distinct invariant.

### 5. Unify completion at the application boundary

With one aggregate type, completion can now become one application use case such as `CompleteMonthEndTaskUseCase`. The use case loads one task, asks the aggregate to complete it for the acting user, and persists the result.

This is preferred over separate completion services because the domain model now has a real unified write model.

### 6. Use one persistence model for tasks plus eligible actors

Persist the write model in:

- `monthend_task`
- `monthend_task_eligible_actor`

`monthend_task` stores the business identity and lifecycle fields.
`monthend_task_eligible_actor` stores the fixed set of actors allowed to complete the task.

This is preferred over multiple check tables because the aggregate is unified and the persistence should follow the aggregate boundary.

## Risks / Trade-offs

- [Worklist query complexity] -> Mitigation: keep worklists query-only and derive them from task filters instead of introducing another aggregate.
- [Optional fields like `subjectEmployeeId`] -> Mitigation: protect them with aggregate invariants and task-type-specific validation.
- [Fresh terminology requires code churn] -> Mitigation: rename aggressively now while the month-end feature is still in development.
- [One table for all task types can drift into sparse columns] -> Mitigation: keep only fields that belong to every task or are justified by stable invariants.

## Migration Plan

- Introduce the unified task schema and adapters directly.
- Implement generation, completion, and query services against the month-end task model.
- Build month-end tests against the unified task language.

Because this work is not yet production-facing, no backward-compatible migration path is required.

## Open Questions

- None at proposal time. The key domain choices for aggregate shape, completion policy, and worklist semantics are intentionally fixed by this design.

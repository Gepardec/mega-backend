## Context

The current month-end feature models only generated checklist obligations through the `MonthEndTask` aggregate and exposes actor-specific worklists as query views over open tasks. That shape works for fixed system-generated obligations, but it does not cover user-created follow-up items that carry free text, have fixed responsibility on the opposite side, and remain relevant even when the related generated task has already been completed.

The new requirement introduces project-scoped clarification subtasks between employees and project leads. A clarification belongs to a concrete month-end context `(month, project, subjectEmployee)`, carries mutable text while open, may carry an optional resolution note when completed, and must preserve creator, resolver, and timestamp metadata (`createdAt`, `resolvedAt`, `lastModifiedAt`). Clarifications are non-blocking for the existing month-end tasks and must appear in employee and lead worklists according to visibility rules.

This is a cross-cutting change across the month-end domain model, persistence adapter, worklist query model, and tests. Because the feature lives in `com.gepardec.mega.hexagon.monthend`, the design keeps domain rules in the aggregate and keeps persistence and query composition behind ports and adapters.

## Goals / Non-Goals

**Goals:**
- Introduce a dedicated `MonthEndClarification` aggregate for user-created clarification subtasks
- Preserve the existing `MonthEndTask` aggregate as the model for generated month-end obligations
- Support employee-created and lead-created clarifications with fixed responsibility and visibility rules
- Track clarification lifecycle metadata: status, creator, resolver, `createdAt`, `resolvedAt`, and `lastModifiedAt`
- Surface visible open clarifications in employee and lead worklists without blocking generated task completion

**Non-Goals:**
- Threaded conversations or reply chains
- Reopening resolved clarifications
- Blocking completion of the existing month-end checklist tasks
- Historical reporting or a separate resolved-clarification inbox

## Decisions

### Introduce a separate `MonthEndClarification` aggregate

**Decision:** Model clarifications as a new aggregate beside `MonthEndTask`, not as a field or child entity of `MonthEndTask`.

**Rationale:** Generated month-end tasks and user-created clarifications have different lifecycles and invariants. `MonthEndTask` is system-generated and keyed by business obligation, while a clarification is user-authored, text-bearing, non-blocking, and can outlive the visibility of a related open task. A separate aggregate keeps those concerns honest and avoids forcing clarification behaviour into the generated-task model.

**Alternatives considered:**
- Attach comments to `MonthEndTaskEntity`. Rejected because a clarification belongs to the broader month/project/employee context, not to one arbitrary generated task row, and clarifications must remain queryable even after related tasks are completed.
- Add clarification as a new `MonthEndTaskType`. Rejected because clarifications are user-created, can exist multiple times per context, and require text editing and resolution-note behaviour that does not fit the current generated-task aggregate.

### Scope clarifications to a month-end project employee context

**Decision:** `MonthEndClarification` stores `month`, `projectId`, `subjectEmployeeId`, `createdBy`, `creatorSide`, `eligibleProjectLeadIds`, `status`, `text`, `resolutionNote`, `resolvedBy`, `createdAt`, `resolvedAt`, and `lastModifiedAt`.

**Rationale:** The business meaning is "follow-up about this employee on this project in this month-end run". That context is stable for both employee-created and lead-created clarifications and is what both worklist roles already reason about.

**Alternatives considered:**
- Store only a foreign key to a `MonthEndTask`. Rejected because there is no single natural task owner for all clarifications and because the clarification permission model depends on the employee/lead context, not on one checklist obligation.

### Derive resolution authority from `creatorSide`

**Decision:** Clarifications store the creator side (`EMPLOYEE` or `PROJECT_LEAD`) and derive who may resolve them from that:
- `EMPLOYEE` created -> any eligible project lead may resolve
- `PROJECT_LEAD` created -> the subject employee may resolve

**Rationale:** Responsibility is fixed and always points to the opposite side. Encoding the creator side keeps the model small while preserving the business rule.

**Alternatives considered:**
- Store a second explicit resolver-side enum. Rejected because it duplicates information that can be derived directly from the creator side.
- Store one concrete responsible actor. Rejected because employee-created clarifications are intentionally resolvable by any eligible project lead.

### Preserve lead eligibility as a snapshot at clarification creation time

**Decision:** A clarification stores the eligible project lead ids that were valid at creation time in a dedicated collection table.

**Rationale:** This mirrors the existing month-end task behaviour where lead eligibility is captured as a snapshot for the month-end run. It keeps visibility, edit rights on the lead side, and lead-side resolution stable even if project lead assignments change later.

### Allow creator-side editing of open clarification text

**Decision:** The clarification has one mutable `text` field. While the clarification is `OPEN`, actors on the creator side may edit that text. The resolver side resolves the clarification and may provide an optional `resolutionNote`.

**Rationale:** This matches the requirement for a subtask rather than a conversation. It also explains why other eligible leads can edit a lead-created clarification: they are on the creator side. Restricting text edits to the creator side avoids both sides overwriting each other's ownership semantics in the same field.

**Alternatives considered:**
- Allow both sides to edit the same text field. Rejected because it blurs authorship and turns the feature into an implicit conversation log.
- Model replies as child comments. Rejected because the requirement explicitly avoids conversation semantics.

### Extend the worklist with a separate clarification collection

**Decision:** `MonthEndWorklist` should expose clarifications as a separate collection instead of mixing them into the existing `tasks` list.

**Rationale:** Generated tasks and clarifications are both actionable but have different fields and behaviour. Tasks need `MonthEndTaskType`; clarifications need text, creator/resolver metadata, and their own state. A separate list keeps the read model explicit and avoids inventing a forced common super-item.

**Alternatives considered:**
- Reuse `MonthEndWorklistItem` for both tasks and clarifications. Rejected because the two item types do not share a clean minimal shape.

### Query open clarifications directly for each worklist role

**Decision:** Employee worklists query open clarifications by `(subjectEmployeeId, month)`. Lead worklists query open clarifications where the lead id is in the clarification's eligible lead snapshot for the month. Worklist composition then combines existing task results with clarification results.

**Rationale:** Clarifications must remain visible independently of the open-task list. Querying them directly keeps worklists correct even when the related month-end tasks are already done.

## Risks / Trade-offs

- **Lead membership changes after clarification creation** -> Permissions will follow the captured snapshot, not the current project lead assignment.
  Mitigation: document snapshot semantics explicitly and keep them consistent with `MonthEndTask`.
- **Open clarifications may remain after all generated tasks are complete** -> Worklists can no longer be thought of as "open tasks only".
  Mitigation: make worklist composition query clarifications independently and describe worklists as month-end action views rather than task-only lists.
- **No conversation history means edits overwrite prior text** -> Some audit detail is lost compared to a threaded model.
  Mitigation: keep `createdBy`, `lastModifiedAt`, `resolvedBy`, `resolvedAt`, and `resolutionNote`; defer full history until there is a concrete use case.
- **Additional joins for lead worklists** -> Clarification queries add another actor-membership join path.
  Mitigation: add indexes on clarification month, subject employee, and eligible lead join rows.

## Migration Plan

1. Add a new Liquibase changelog for `monthend_clarification` and `monthend_clarification_eligible_lead`
2. Introduce the `MonthEndClarification` domain model, repository port, persistence entity, and MapStruct mapper
3. Add application services for create, update, and complete clarification use cases
4. Extend the worklist query model and services to include visible open clarifications
5. Add unit and integration tests for aggregate rules, repository queries, and worklist composition
6. Deploy with empty clarification tables; no backfill is required

Rollback is low-risk because the feature is additive. If needed, the read path can be disabled while leaving the new tables unused until a compensating migration is prepared.

## Open Questions

- Should resolved clarifications become queryable through a separate history view later, or is persistence-only history sufficient for now?

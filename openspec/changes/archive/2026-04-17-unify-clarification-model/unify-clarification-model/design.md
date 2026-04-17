## Context

The existing `MonthEndClarification` aggregate supports two clarification scenarios: an employee opens a clarification for the project leads to resolve, or a lead opens one for the subject employee to resolve. Authorization is encoded via a `creatorSide: EMPLOYEE | PROJECT_LEAD` field that acts as an indirect pointer to the resolver side (always the opposite). Adding a third scenario — leads opening clarifications visible and resolvable only by other leads — breaks this model because there is no opposite side: both creator and resolver are on the lead side.

This design unifies all three scenarios under one aggregate by removing `creatorSide` and replacing it with a simpler invariant: the creator edits, every other involved party resolves, and the creator can delete.

## Goals / Non-Goals

**Goals:**
- Support all three clarification scenarios in one aggregate without conditional side-based branching
- Remove `creatorSide` from the domain model, persistence, and API surface
- Make `subjectEmployeeId` nullable to accommodate project-level clarifications
- Add a hard-delete use case gated to the creator
- Add a project-level context resolution path for lead-only creation

**Non-Goals:**
- Changing the OPEN/DONE lifecycle, resolution notes, or timestamp fields
- Changing repository query methods for employee or lead worklists
- Audit logging or soft-delete for removed clarifications
- Changing how `eligibleProjectLeadIds` are snapshotted (same mechanism, applied to all three scenarios)

## Decisions

### 1. One aggregate, nullable `subjectEmployeeId` (vs. two aggregates)

A separate `MonthEndProjectClarification` aggregate would be fully valid at all times with no nullable fields. However, the three scenarios share identical lifecycle mechanics (OPEN/DONE, text editing, resolution, deletion, lead snapshot) and identical repository query patterns (lead queries join on `eligibleProjectLeadIds` in all cases). A second aggregate would duplicate the entire lifecycle implementation for one structural difference.

`subjectEmployeeId: UserId?` with the constraint "null implies creator must be a lead" keeps the model coherent without meaningful added complexity. The null value is semantically clear: no employee is a party to this clarification.

### 2. Remove `creatorSide`, derive authorization from identity

`creatorSide` was an indirect encoding of "who resolves" (always the other side). With scenario 3, that encoding breaks. Rather than adding a third enum value, we replace the concept entirely with direct identity checks:

```
isInvolved(actor)  = eligibleLeadIds.contains(actor)
                     OR (subjectEmployeeId != null AND subjectEmployeeId == actor)
canEditText(actor) = isOpen() AND actor == createdBy
canResolve(actor)  = isOpen() AND isInvolved(actor) AND actor != createdBy
canDelete(actor)   = actor == createdBy
```

`createdBy` is already stored on the aggregate, so no new fields are needed. The creator's role (employee vs. lead) is verifiable at creation time via the same invariant: `createdBy` must be in `isInvolved`.

### 3. Hard delete, no audit trail

The use case is creator-initiated removal of their own open clarification. Deletion is only permitted while the clarification is `OPEN` — done clarifications are considered settled records and cannot be removed. Since clarifications are user-created follow-up subtasks (not system-generated), and no audit requirement exists, a hard `DELETE` from the DB is appropriate. The `delete(MonthEndClarificationId)` repository method removes the row and all join-table rows (cascade). No soft-delete column is added.

### 4. New project-level context service (vs. overloading existing service)

`MonthEndEmployeeProjectContextService.resolve()` performs three validations: project active, employee active, employee assigned. For scenario 3, only the first is needed. Rather than adding a nullable employee parameter to the existing service (which would make the existing service's contract conditional), a new `MonthEndProjectContextService` handles the project-only path. It returns a `MonthEndProjectContext` containing the project snapshot and eligible lead IDs — a strict subset of what `MonthEndEmployeeProjectContext` carries.

Both context types are resolved at creation time only; the lead snapshot is stored on the clarification and not re-evaluated later.

### 5. REST API — optional `subjectEmployeeId` on lead creation request

The `CreateProjectLeadClarificationRequest` gains an optional `subjectEmployeeId`. When absent, the service uses the project-level context path. `CreateEmployeeClarificationRequest` is unchanged — the employee always identifies themselves as subject. The DELETE endpoint is placed on the shared API so both roles can reach it with a single endpoint.

## Risks / Trade-offs

- **Nullable field in DB** → Liquibase migration sets `subject_employee_id` to nullable; existing rows are unaffected (all have a value). The `creator_side` column is dropped in the same migration.
- **Dropped `creatorSide` column is a breaking change** → Any consumer reading `creatorSide` from the API or DB must be updated. No downstream consumers are currently known outside this service.
- **Authorization simplification allows cross-lead resolution in scenario 1** → A lead other than the creating lead can now resolve a lead-created clarification targeting an employee (in addition to the employee). This is intentional and consistent with the unified rule.

## Migration Plan

1. Liquibase changeset: set `subject_employee_id` nullable, drop `creator_side` column, drop `creator_side` from join-table if present
2. Domain model update: remove `creatorSide` field and all validation referencing it; make `subjectEmployeeId` nullable
3. Authorization methods updated in domain (`canEditText`, `canResolve`, new `canDelete`)
4. New `MonthEndProjectContextService` + `MonthEndProjectContext`
5. Updated `CreateMonthEndClarificationService` to branch on presence/absence of `subjectEmployeeId`
6. New `DeleteMonthEndClarificationService` + use case interface + repository method
7. REST: update request/response models, add DELETE endpoint
8. Update all existing tests; add new tests for scenario 3 and delete use case

Rollback: Liquibase rollback restores `creator_side` as nullable (existing rows without value allowed) and re-adds NOT NULL constraint on `subject_employee_id` after backfilling. This is feasible only before any project-level clarifications are written.

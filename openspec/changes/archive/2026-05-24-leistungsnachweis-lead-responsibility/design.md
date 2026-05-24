## Context

`LEISTUNGSNACHWEIS` tasks are currently employee-owned: the subject employee is the sole eligible actor (`INDIVIDUAL_ACTOR` policy). The business process requires a project lead to confirm receipt of the Leistungsnachweis from the employee, so the responsible party must shift to the project leads. The change mirrors the existing `PROJECT_LEAD_REVIEW` pattern: the task remains anchored to a subject employee but its eligible actors become the project leads.

## Goals / Non-Goals

**Goals:**
- LEISTUNGSNACHWEIS eligible actors become the project leads (`ANY_ELIGIBLE_ACTOR`)
- Employees see LEISTUNGSNACHWEIS as readonly (`canComplete = false`)
- Leads can complete LEISTUNGSNACHWEIS (`canComplete = true`)
- Self-service preparation stops generating LEISTUNGSNACHWEIS tasks

**Non-Goals:**
- No new task types introduced
- No changes to REST API response shape or endpoint contracts
- No changes to `EMPLOYEE_TIME_CHECK` behaviour
- Data migration of existing open LEISTUNGSNACHWEIS tasks (separate concern — see Risks)

## Decisions

### 1. Move LEISTUNGSNACHWEIS generation into `planProjectTasks()`

`planEmployeeOwnedTasks()` does not receive lead IDs; `planProjectTasks()` already has them. Moving generation there avoids polluting the employee-owned method's contract. LEISTUNGSNACHWEIS is created inside the per-employee loop in `planProjectTasks()`, guarded by `project.billable()` — the same structure as `PROJECT_LEAD_REVIEW`.

Alternative considered: pass lead IDs into `planEmployeeOwnedTasks()` — rejected because employee-owned tasks should not depend on lead data.

### 2. Adopt a PROJECT_LEAD_REVIEW-like validation branch in `MonthEndTask`

LEISTUNGSNACHWEIS will share the same validation invariant as `PROJECT_LEAD_REVIEW`: `subjectEmployeeId` is required, but is not required to be in `eligibleActorIds`. The current assertion `eligibleActorIds.contains(subjectEmployeeId)` is removed for this type.

Completion policy changes from `INDIVIDUAL_ACTOR` to `ANY_ELIGIBLE_ACTOR`.

### 3. No-leads guard consistent with `PROJECT_LEAD_REVIEW`

If a billable project has no active leads at generation time, LEISTUNGSNACHWEIS is not created — a task with no eligible actors cannot be completed. This is consistent with the existing guard for `PROJECT_LEAD_REVIEW` and `ABRECHNUNG`.

### 4. Self-service preparation drops LEISTUNGSNACHWEIS

Self-service creates employee-owned obligations early. LEISTUNGSNACHWEIS is no longer employee-owned, so it has no place in the self-service flow. Generating it there would produce a task with the employee as the eligible actor, contradicting the new model.

## Risks / Trade-offs

**Lead-only projects with no active leads produce no LEISTUNGSNACHWEIS** → If a project loses all active leads mid-cycle, no LEISTUNGSNACHWEIS is generated at month-end. This is consistent with the existing behaviour for `PROJECT_LEAD_REVIEW` and is an accepted trade-off.

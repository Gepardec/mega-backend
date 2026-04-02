## Context

The current hexagonal month-end model separates generated obligations (`MonthEndTask`) from user-authored follow-up items (`MonthEndClarification`). Scheduled generation creates the task set on the last day of the month, while clarifications are visible alongside tasks in the worklist but remain independent from task completion.

The new requirement adds an explicit employee-driven path for one project context at a time: before the scheduled run, the employee must be able to prepare the same employee-owned month-end obligations that would later be generated automatically, and optionally leave a clarification for the same month/project/employee context. This must fit the existing per-project month-end model, remain explicit rather than bulk-automatic, and avoid reintroducing a separate premature-check aggregate.

This is a cross-cutting change across month-end application services, persistence, identity-resolution support, and tests. It also has one important consistency requirement: scheduled generation and self-service preparation must converge on the same business obligations without creating duplicates.

## Goals / Non-Goals

**Goals:**
- Add an explicit per-project employee self-service workflow to prepare employee-owned month-end obligations before the scheduled generation run
- Reuse the same obligation rules and business keys as scheduled month-end generation
- Keep clarifications as a separate aggregate while allowing them to participate in the same user workflow
- Preserve explicit project-by-project user intent instead of adding bulk or hidden automation
- Harden duplicate prevention between scheduled generation and self-service preparation

**Non-Goals:**
- Reintroducing a `PrematureEmployeeCheck` aggregate or task-specific reason field
- Bulk preparation across all projects of a month
- Self-service creation of lead-owned month-end tasks (`PROJECT_LEAD_REVIEW`, `ABRECHNUNG`)
- Changing the existing clarification permission model or turning clarifications into blocking prerequisites
- Reworking the existing task completion flow
- Introducing a REST controller in the first implementation slice

## Decisions

### Model premature checking as a workflow, not as a new aggregate

**Decision:** Treat "premature check" as an application-level month-end workflow and not as a dedicated domain aggregate. The workflow ensures the real employee-owned `MonthEndTask` obligations exist early and can optionally create a `MonthEndClarification` in the same context.

**Rationale:** The new month-end architecture already has the right domain concepts: obligations live in `MonthEndTask` and free-text explanations live in `MonthEndClarification`. A separate premature-check aggregate would duplicate lifecycle and context that already exist elsewhere.

**Alternatives considered:**
- Recreate `PrematureEmployeeCheck` in the hexagon. Rejected because it duplicates task creation state and puts clarification-like text on the wrong aggregate.
- Add a `premature` flag or `reason` to `MonthEndTask`. Rejected because task obligations and clarifications intentionally have different lifecycles and behavior.

### Keep the workflow explicit and scoped to one employee project context

**Decision:** The new self-service flow operates on one `(month, projectId, subjectEmployeeId)` context at a time and can be triggered only by the subject employee for that context.

**Rationale:** The new month-end model is per project, and the user explicitly wants to avoid hidden bulk behavior. A project-scoped workflow also matches the context used by clarifications and the existing task business key.

**Alternatives considered:**
- Prepare all employee project contexts for a month in one action. Rejected because it hides which obligations are being created and makes clarification input ambiguous.
- Allow project leads or office roles to trigger the same flow for employees. Rejected for now because the requirement is employee self-service ahead of absence.

### Prepare only employee-owned tasks through self-service

**Decision:** Self-service preparation creates only the employee-owned obligations that scheduled generation would create for that project context: `EMPLOYEE_TIME_CHECK` and, on billable projects, `LEISTUNGSNACHWEIS`.

**Rationale:** Those are the obligations the employee must satisfy before absence. Lead-owned tasks remain part of the regular scheduled process and should continue to be derived from the full month-end project picture.

**Alternatives considered:**
- Also create `PROJECT_LEAD_REVIEW` during self-service preparation. Rejected because it expands the workflow into a lead-side scheduling concern and introduces more side effects than the employee intends.
- Also create `ABRECHNUNG` during self-service preparation. Rejected because it is project-owned, not employee-owned.

### Reuse shared task-planning logic instead of duplicating generation rules

**Decision:** Extract task-planning logic from scheduled generation into a reusable month-end planning component that can produce candidate employee-owned tasks for a single project context and for the full scheduled generation run.

**Rationale:** The same business rules must define which tasks exist in both paths. Sharing planning logic avoids drift between scheduled generation and self-service preparation.

**Alternatives considered:**
- Re-implement a smaller copy of the generation rules inside the self-service service. Rejected because it would inevitably drift from the scheduler over time.

### Orchestrate optional clarification creation in the same workflow while keeping aggregates separate

**Decision:** The self-service preparation workflow accepts optional clarification text. When text is provided, the workflow creates an employee-created `MonthEndClarification` in the same context using the existing clarification rules, while still keeping `MonthEndTask` and `MonthEndClarification` as separate aggregates and repositories.

**Rationale:** This matches the desired user experience of "prepare and optionally explain" without overloading the task aggregate. The user sees one explicit project-level action, while the model preserves the separation between obligation and communication.

**Alternatives considered:**
- Force the UI to call a second unrelated clarification action later. Rejected because it fragments a user journey that naturally belongs together.
- Persist clarification text directly on the preparation request or task entity. Rejected because the clarification capability already owns text, visibility, and resolution semantics.

### Add persistence-level uniqueness for month-end business obligations

**Decision:** Enforce the task business key in the database, not only in application code. Use uniqueness that covers both employee-owned and subjectless tasks, likely via:
- a unique constraint or index on `(month_value, project_id, type, subject_employee_id)` for rows with a subject employee
- a unique constraint or index on `(month_value, project_id, type)` for rows without a subject employee

**Rationale:** Scheduled generation and self-service preparation can race. Application-level duplicate checks are not sufficient once there are multiple writers for the same obligation set.

**Alternatives considered:**
- Keep duplicate prevention only in `GenerateMonthEndTasksService`. Rejected because self-service creation introduces a second write path.
- Use only one unique constraint including nullable `subject_employee_id`. Rejected because nullable uniqueness does not reliably protect subjectless tasks such as `ABRECHNUNG`.

### Resolve the authenticated actor into the hexagon user model before preparation

**Decision:** Add a hexagon-side way to resolve the authenticated employee from request identity, most likely by hexagon user lookup via email, and validate that the actor is the subject employee for the prepared project context. The first implementation should prepare this logic behind the use-case boundary without adding a REST controller yet.

**Rationale:** The month-end use cases work with hexagon `UserId`, while the current request identity in the application is email-based. The self-service flow needs a clean bridge rather than leaking legacy user models into the hexagon.

**Alternatives considered:**
- Reuse legacy `UserContext` inside the month-end application layer. Rejected because it crosses architectural boundaries and keeps the new month-end flow coupled to legacy models.

## Risks / Trade-offs

- **Combined workflow touches two aggregates** -> Keep rules inside each aggregate and let one application service coordinate them for this explicit user action
- **Per-project manual preparation can feel repetitive for employees with many projects** -> Keep the explicit flow for clarity now and defer any bulk helper to a future separate capability
- **Identity mapping between authenticated email and hexagon user can fail if data is out of sync** -> Resolve through the hexagon user repository and fail fast with a clear rejection when no matching employee exists
- **Optional clarification creation can create more worklist items for leads** -> Keep clarifications non-blocking and scoped to the same project context so the added work remains intentional and visible

## Migration Plan

1. Add the self-service preparation spec and task-generation delta spec
2. Introduce the self-service preparation use case and shared task-planning component in `com.gepardec.mega.hexagon.monthend`
3. Add identity-resolution support that maps the logged-in actor to the hexagon user model for the use case
4. Add repository support and Liquibase changes for business-key uniqueness on `monthend_task`
5. Reuse the existing clarification creation path from the self-service workflow when clarification text is provided
6. Add unit and integration tests for authorization, idempotency, scheduled-generation coexistence, and optional clarification creation

Rollback is low risk because the feature is still in development and the first implementation slice does not expose a controller yet. If needed, the new use case can remain unused while the shared planning logic and stricter task uniqueness stay in place.

## Open Questions

- Should the self-service preparation response return the ensured task identities and any newly created clarification identity, or is a simple success payload sufficient for the first iteration?

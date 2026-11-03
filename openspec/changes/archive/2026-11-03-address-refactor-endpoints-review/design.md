# Design

## Context

See proposal.md for the seven findings and the decision on each. The current state that shapes the approach:

- `WorkTimeReport.assemble` builds entries from a `HashMap` and `groupingBy`, so the order is whatever the hash order is. On `main`, `GetProjectLeadWorkTimeService` sorted by `employee().fullName().displayName()` and then `project().name()`, and `GetEmployeeWorkTimeService` sorted by `project().name()`.
- `WorkTimeScope` pairs the employees in scope with a `ProjectResolver`. The employee scope's resolver logs a warning per unknown project. The controlling scope's resolver logs nothing on purpose, because bookings on projects the lead doesn't lead are expected there.
- `MonthEndClarification`'s compact constructor calls `validateCreator`. That means the check runs on creation, on every copy (`editText`, `resolve`), and when a stored row is loaded. `createBySystem` passes only because `validateCreator` makes an exception for `SystemActor`.
- `CreateMonthEndClarificationService` resolves the project context (projects + active users), runs `resolveSubject` (403), and then, when a subject is given, resolves the employee context. That second step loads projects and active users again and recomputes the same lead set. The service test and the REST test rely on this error order: project not found (400) → caller not allowed (403) → subject not active or not assigned (400).
- `MonthEndEmployeeProjectContextService.resolve(month, projectId, subject)` is also used by `CreateClarificationFromZepMailService`.
- `MonthEndUserSnapshotPort.findByIds(ids, month)` returns users who are active in the month, plus the system actor.
- `MonthEndTaskRepositoryAdapter` already uses `panache.count(...)` for its `exists…` methods.

## Goals / Non-Goals

**Goals:**
- Each rule lives in one place, and each snapshot is loaded once per request.
- Keep the observable error order of `POST /monthend/clarifications` exactly as it is.

**Non-Goals:**
- Changing the employee-capacity payroll month. It also uses `findOpenSubjectTasks(...).isEmpty()`, but the review didn't flag it, and it only loads one employee's tasks.
- Hiding whether a project exists (finding 5 is accepted; see proposal).
- Changing how unresolved-project hours count towards `employeeMonthTotalHours`.

## Decisions

### D1 — Sort in the domain model, with id tie-breakers

`WorkTimeReport.assemble` sorts the finished entry list with one comparator: employee display name → project name → employee id → project id. The ids only decide between entries whose names are equal, so the order is deterministic, as the spec requires ("same order on every request").

- *Why the domain and not the service or REST layer:* both capacities go through `assemble`, and order is now part of the report contract (worktime-report spec).
- *Alternative:* sort in the REST mapper. Rejected, because the order would then depend on the adapter and the domain tests couldn't pin it down.

### D2 — One place reports unresolved projects; the scope sets the log level

- `ProjectResolver` becomes a plain lookup that returns `Optional` and never logs.
- `WorkTimeScope` gets a flag that says whether unresolved projects are expected: `false` for EMPLOYEE, `true` for CONTROLLING.
- `GetWorkTimeReportService.resolveProjects` collects the zep ids it couldn't resolve and logs them once per report, as a list. It logs at `WARN` when unresolved projects are unexpected (missing snapshot) and at `DEBUG` when they are expected (non-lead project).

- *Alternative:* `WARN` in both capacities. Rejected, because every controlling request for a lead whose staff also book on other projects would log a warning, and real missing snapshots would be lost in the noise.
- *Alternative:* look up every booked project in controlling, to tell "missing snapshot" apart from "not led". Rejected: extra queries just to produce a log line.

### D3 — The creator rule moves from the record constructor to the creation factory

- Remove `validateCreator` from the compact constructor.
- Call it from `create(id, month, projectId, subject, createdBy, sourceSystem, leads, text, createdAt)`. The overload without `sourceSystem` already delegates to this one.
- It throws `MonthEndActorNotAuthorizedException`, which the REST layer maps to 403.
- `createBySystem` calls the constructor directly, so the rule doesn't apply to it. Remove the `SystemActor` exception from `validateCreator`.
- `CreateMonthEndClarificationService.resolveSubject` is deleted.

The creator rule is about who may create a clarification, and it only applies at creation. Applying it to rows loaded from storage, or to `editText`/`resolve` copies, mixes a creation-time authorization check into a structural invariant. With the rule only in the factory, the API keeps its `403`.

- *Alternative:* keep the rule in the constructor and change its exception to the authorization type. Rejected: loading a stored clarification would then be able to fail with an "actor not authorized" error.

Effect on internal callers:
- `PersistZepClarificationService` (ZEP mail): an invalid creator now raises the authorization exception instead of the validation exception. `CreateClarificationFromZepMailService` catches `Exception` and fires the failure event either way, so nothing changes for it.
- `PrematureMonthEndPreparationService` always passes creator = subject, so it is not affected.

### D4 — The employee context builds on the project context; the service creates the aggregate before checking the subject

- Add `MonthEndEmployeeProjectContextService.resolve(MonthEndProjectContext projectContext, UserId subject)`.
- It loads only the subject, via `findByIds(Set.of(subject), month)`, and treats a missing user or the system actor as not found.
- It checks the project assignment.
- It returns a `MonthEndEmployeeProjectContext` that reuses the project and the lead set from the project context.
- The existing `resolve(month, projectId, subject)` becomes `resolve(projectContextService.resolve(month, projectId), subject)`. Its behavior for the ZEP mail caller stays the same.

`CreateMonthEndClarificationService.create` then runs:
1. `projectContext = projectContextService.resolve(month, projectId)`. Unknown project → 400, as today.
2. `clarification = MonthEndClarification.create(..., projectContext.eligibleProjectLeadIds(), ...)`. Caller not allowed → 403 (D3).
3. If a subject is given: `employeeContextService.resolve(projectContext, subject)`. Subject not active or not assigned → 400.
4. Save and fire the event.

Projects and active users are each loaded once, plus one indexed lookup by id for the subject. The error order matches today's.

- *Alternative:* call only the employee context and take its leads (the reviewer's suggestion). Rejected: a non-lead naming an unassigned colleague would get 400 instead of 403, which breaks the `monthend-rest-api` scenario "Employee cannot create a clarification scoped to another employee".
- *Alternative:* expose the creator rule as a public static domain check and call it in the service before the subject check. Rejected: the rule would run twice, and the service would again have to know about it.

### D5 — Remove the dead known-employee filter

`buildReport` maps each attendance key with `scope.employeesByZepUsername().get(key)`. The keys are that map's own `keySet()`, so the lookup can't miss. `isKnownEmployee` is deleted.

### D6 — Add an existence query for scoped tasks

- Add `boolean existsTasks(YearMonth month, MonthEndScope scope)` to `MonthEndTaskRepository`.
- The adapter uses the same `switch (scope)` as `findTasks`, with `panache.count(...) > 0`.
- To keep the two methods from drifting apart, the `LedProjects` and `SubjectEmployee` where-clauses go into private constants that both `findTasks` and `existsTasks` use.
- `GetPayrollMonthService.controllingPayrollMonth` calls `existsTasks`.

- *Alternative:* a `findFirst`/limit-1 query. Rejected: the adapter's existing `exists…` methods all use `count`, so this keeps them consistent.

## Risks / Trade-offs

- [Records loaded from storage are no longer checked for a valid creator.] → Every write still goes through the factories, and a stored clarification can't change its creator.
- [Sorting by display name depends on the JVM's default `String` order, not a locale-aware collator. Names with umlauts may sort differently than a German user expects.] → This matches the pre-refactor behavior. Locale-aware sorting stays a frontend concern.
- [`findByIds` also returns the system actor.] → D4 treats the system actor as "employee context not found", so it can never be a subject.

## Migration Plan

No data or deployment steps. To roll back, revert the commit.

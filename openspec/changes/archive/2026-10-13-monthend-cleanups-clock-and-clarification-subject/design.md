# Design

## Context

- `GetEmployeePayrollMonthService` and `GetProjectLeadPayrollMonthService` call `YearMonth.now()`. Other monthend services (e.g. `CreateMonthEndClarificationService`) already inject `java.time.Clock`, so a CDI producer exists.
- `MonthEndResource.createMonthEndClarification` branches on `authenticatedActorContext.hasRole(Role.PROJECT_LEAD)`. A role holder's `subjectEmployeeId` is passed through as-is, or `null` when absent. Every other caller gets the actor as subject.
- `CreateMonthEndClarificationService` already resolves the eligible leads: through `MonthEndEmployeeProjectContextService` when a subject is given, through `MonthEndProjectContextService` when not. `MonthEndClarification.validateCreator` enforces "creator is the subject or an eligible lead" and throws `MonthEndValidationException`, which is mapped to 400.
- `MonthEndDomainExceptionMapper` maps `MonthEndActorNotAuthorizedException` to 403.

## Goals / Non-Goals

**Goals:**
- The payroll-month month calculation can be tested with a fixed clock.
- The application layer decides authorization for clarification creation, based on the project relationship. The adapter only translates.

**Non-Goals:**
- Other places that read the clock directly (schedulers, `ZepMailWebhookResource`, `UserResource` CSV template, `SyncScheduler` timing). They can be handled separately.
- Changing the payroll-month rules themselves. `perspective-scoped-read-endpoints` reworks these services.
- Changing the domain invariant in `MonthEndClarification.validateCreator`. It stays as a safety net.

## Decisions

### Clock injection in the payroll-month services

Both services take a `Clock` through constructor injection and use `YearMonth.now(clock)`. Their unit tests switch to a fixed `Clock` and assert explicit months instead of months computed from `YearMonth.now()`.

### Subject resolution moves into `CreateMonthEndClarificationService`

The service resolves the subject before it builds the aggregate:

```
subject given?   actor eligible lead of project in month?   result
--------------   ----------------------------------------   ----------------------------------
no               yes                                        project-level (subject = null)
no               no                                         subject = actor
yes, = actor     either                                     subject = actor
yes, other       yes                                        subject = given employee
yes, other       no                                         MonthEndActorNotAuthorizedException (403)
```

Eligibility uses the project context's `eligibleProjectLeadIds()` for the month, which is the same source that becomes the clarification's snapshot. When the actor is not an eligible lead and becomes the subject, the service resolves the employee-project context for the actor. That context already rejects actors who are not assigned to the project (unchanged behavior for employees).

The adapter passes the request's `subjectEmployeeId` (nullable) unchanged and no longer reads roles.

**Alternatives considered:**
- *Only delete the adapter branch and rely on `validateCreator`.* Rejected: an employee who sends no subject would get a project-level clarification and a 400. Today's behavior for the most common case would break.
- *Return 400 (via the domain invariant) for a foreign subject.* Rejected: the request is well-formed, the actor just isn't allowed to make it. 403 says that, and the exception mapper already maps it.
- *Keep the silent rewrite.* Rejected: it hides frontend bugs and is the adapter-level authorization the change is meant to remove.

### Use case signature stays the same

`CreateMonthEndClarificationUseCase.create(month, projectId, subjectEmployeeId, actorId, text)` already takes a nullable subject. Only the meaning of `null` changes: it now means "not specified" instead of "project-level", and the service resolves it. `MonthEndResource` is the only caller of the use case. The ZEP mail path and `PrematureMonthEndPreparationService` build clarifications directly and are not affected.

## Risks / Trade-offs

- [The frontend may send a foreign `subjectEmployeeId` from an employee view and rely on the rewrite] → Check the frontend's create-clarification calls before release. The proposal lists this under Impact.
- [On the employee dashboard, a lead who omits the subject in a project they lead still gets a project-level clarification] → Same as today. The employee dashboard should send the user's own id. This is a frontend concern and is noted in the proposal.
- [Resolving the project context before deciding on the subject adds a lookup] → It reads the same snapshots the service already reads. The cost is negligible.

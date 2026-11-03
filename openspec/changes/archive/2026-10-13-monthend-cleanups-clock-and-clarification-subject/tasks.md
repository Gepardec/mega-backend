# Tasks

## 1. Clock injection in payroll-month services

- [x] 1.1 Inject `java.time.Clock` into `GetEmployeePayrollMonthService` and replace `YearMonth.now()` with `YearMonth.now(clock)`; update `GetEmployeePayrollMonthServiceTest` to use a fixed `Clock` and assert explicit months, and verify the test passes
- [x] 1.2 Inject `java.time.Clock` into `GetProjectLeadPayrollMonthService` and replace `YearMonth.now()` with `YearMonth.now(clock)`; update `GetProjectLeadPayrollMonthServiceTest` to use a fixed `Clock` and assert explicit months, and verify the test passes

## 2. Clarification subject resolution in the application layer

- [x] 2.1 In `CreateMonthEndClarificationService`, resolve the subject from the actor's lead eligibility for the project and month as described in design.md (project-level for eligible leads without subject, actor as subject for non-leads without subject, `MonthEndActorNotAuthorizedException` for non-leads naming another employee); verify with new `CreateMonthEndClarificationServiceTest` cases covering every row of the design's decision table
- [x] 2.2 Add a `CreateMonthEndClarificationServiceTest` case for a user who leads a different project but not the requested one and omits the subject, and verify the clarification is created with the actor as subject
- [x] 2.3 Remove the `hasRole(Role.PROJECT_LEAD)` branch from `MonthEndResource.createMonthEndClarification` and pass the request's nullable `subjectEmployeeId` through unchanged; verify `MonthEndResource` no longer references `hasRole`
- [x] 2.4 Update `MonthEndResourceTest` (use cases are `@InjectMock`ed): replace the "foreign subject is overwritten by the actor" expectation with one that asserts the request's `subjectEmployeeId` is passed to the use case unchanged, and add a case where the mocked use case throws `MonthEndActorNotAuthorizedException` and the API responds `403 Forbidden`; verify the REST test class passes

## 3. Verification

- [x] 3.1 Run `mvn test` and verify the full suite, including `ArchitectureTest`, passes

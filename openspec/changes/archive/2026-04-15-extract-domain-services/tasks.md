## 1. MonthEnd Employee Project Context

- [x] 1.1 Move `ResolveMonthEndEmployeeProjectContextService` to `monthend.domain.services` and rename to `MonthEndEmployeeProjectContextService`
- [x] 1.2 Update all call sites in the application layer (`CreateMonthEndClarificationService`, `PrematureMonthEndPreparationService`) to use the new package and name
- [x] 1.3 Write unit tests for `MonthEndEmployeeProjectContextService` covering the four scenarios: success, inactive project, inactive employee, employee not assigned

## 2. User Role Policy

- [x] 2.1 Create `OfficeManagementEmails` domain value object in `user.domain.model` wrapping a normalised `Set<String>` with a `contains(String email) → boolean` method
- [x] 2.2 Create `OfficeManagementEmailsProducer` in `user.application` that reads `@ConfigProperty(name = "mega.mail.reminder.om")` and produces an `@ApplicationScoped` CDI `OfficeManagementEmails` bean
- [x] 2.3 Create `UserRolePolicyService` as a CDI `@ApplicationScoped` domain service in `user.domain.services` injecting `OfficeManagementEmails` with `determineRoles(String email, User existingUser) → Set<Role>`
- [x] 2.4 Update `SyncUsersService` to inject `UserRolePolicyService` and delegate `buildRoles()` to it; remove the `@ConfigProperty` injection and `normalizeEmail()` from the application service
- [x] 2.5 Write unit tests for `OfficeManagementEmails.contains()` covering: exact match, case-insensitive match, whitespace trimming, no match
- [x] 2.6 Write unit tests for `UserRolePolicyService` covering: EMPLOYEE-only, OFFICE_MANAGEMENT by email match, PROJECT_LEAD preserved

## 3. Project Lead Role Assignment

- [x] 3.1 Add `grantProjectLeadRole()` and `revokeProjectLeadRole()` methods to the `User` aggregate returning new immutable instances
- [x] 3.2 Extend `UserRolePolicyService` with the single-user project lead role policy in `user.domain.services`
- [x] 3.3 Create `SyncProjectLeadRolesService` in `user.application`, update `SyncProjectLeadsService` to return shared-kernel lead IDs only, and let `SyncScheduler` trigger role assignment as a separate step
- [x] 3.4 Write unit tests for `User.grantProjectLeadRole()` and `User.revokeProjectLeadRole()` covering grant, idempotent grant, revoke, idempotent revoke
- [x] 3.5 Write unit tests for the project lead role policy in `UserRolePolicyService` covering: lead gained, lead lost

## 4. Worktime Report Assembler

- [x] 4.1 Create `WorkTimeReportAssembler` as a CDI `@ApplicationScoped` domain service in `worktime.domain.services` with `totalHours()` and `buildEntry()` methods
- [x] 4.2 Update `GetEmployeeWorkTimeService` to inject and delegate to `WorkTimeReportAssembler`, removing `totalHours()`, `sumBillableHours()`, `sumNonBillableHours()` private methods
- [x] 4.3 Update `GetProjectLeadWorkTimeService` to inject and delegate to `WorkTimeReportAssembler`, removing the same duplicated private methods
- [x] 4.4 Write unit tests for `WorkTimeReportAssembler` covering: `totalHours` with records, `totalHours` empty, `buildEntry` hour sums, `buildEntry` employee/project references

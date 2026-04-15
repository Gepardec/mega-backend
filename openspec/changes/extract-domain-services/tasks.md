## 1. MonthEnd Employee Project Context

- [ ] 1.1 Move `ResolveMonthEndEmployeeProjectContextService` to `monthend.domain.services` and rename to `MonthEndEmployeeProjectContextService`
- [ ] 1.2 Update all call sites in the application layer (`CreateMonthEndClarificationService`, `PrematureMonthEndPreparationService`) to use the new package and name
- [ ] 1.3 Write unit tests for `MonthEndEmployeeProjectContextService` covering the four scenarios: success, inactive project, inactive employee, employee not assigned

## 2. User Role Policy

- [ ] 2.1 Create `OfficeManagementEmails` domain value object in `user.domain.model` wrapping a normalised `Set<String>` with a `contains(String email) → boolean` method
- [ ] 2.2 Create `OfficeManagementEmailsProducer` in `user.application` that reads `@ConfigProperty(name = "mega.mail.reminder.om")` and produces an `@ApplicationScoped` CDI `OfficeManagementEmails` bean
- [ ] 2.3 Create `UserRolePolicyService` as a CDI `@ApplicationScoped` domain service in `user.domain.services` injecting `OfficeManagementEmails` with `determineRoles(String email, User existingUser) → Set<Role>`
- [ ] 2.4 Update `SyncUsersService` to inject `UserRolePolicyService` and delegate `buildRoles()` to it; remove the `@ConfigProperty` injection and `normalizeEmail()` from the application service
- [ ] 2.5 Write unit tests for `OfficeManagementEmails.contains()` covering: exact match, case-insensitive match, whitespace trimming, no match
- [ ] 2.6 Write unit tests for `UserRolePolicyService` covering: EMPLOYEE-only, OFFICE_MANAGEMENT by email match, PROJECT_LEAD preserved

## 3. Project Lead Role Reconciliation

- [ ] 3.1 Add `grantProjectLeadRole()` and `revokeProjectLeadRole()` methods to the `User` aggregate returning new immutable instances
- [ ] 3.2 Create `ProjectLeadRoleReconciliationService` as a CDI `@ApplicationScoped` domain service in `user.domain.services`
- [ ] 3.3 Update `SyncProjectLeadsService` to inject `ProjectLeadRoleReconciliationService` and replace `reconcileUserRoles()` and `updateLeadRole()` with calls to the domain service and the new aggregate methods
- [ ] 3.4 Write unit tests for `User.grantProjectLeadRole()` and `User.revokeProjectLeadRole()` covering grant, idempotent grant, revoke, idempotent revoke
- [ ] 3.5 Write unit tests for `ProjectLeadRoleReconciliationService` covering: lead gained, lead lost, no change excluded

## 4. Worktime Report Assembler

- [ ] 4.1 Create `WorkTimeReportAssembler` as a CDI `@ApplicationScoped` domain service in `worktime.domain.services` with `totalHours()` and `buildEntry()` methods
- [ ] 4.2 Update `GetEmployeeWorkTimeService` to inject and delegate to `WorkTimeReportAssembler`, removing `totalHours()`, `sumBillableHours()`, `sumNonBillableHours()` private methods
- [ ] 4.3 Update `GetProjectLeadWorkTimeService` to inject and delegate to `WorkTimeReportAssembler`, removing the same duplicated private methods
- [ ] 4.4 Write unit tests for `WorkTimeReportAssembler` covering: `totalHours` with records, `totalHours` empty, `buildEntry` hour sums, `buildEntry` employee/project references

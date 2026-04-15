## Why

A review of the hexagon application services revealed domain logic that has leaked into the application layer — business rules, domain policies, and calculations that belong in the domain but are currently embedded in use-case orchestrators. In two cases the same domain logic is duplicated across multiple services.

## What Changes

- Extract `ResolveMonthEndEmployeeProjectContextService` from the `application` package into the `monthend` domain layer as a proper domain service
- Extract role assignment policy (`buildRoles`) from `SyncUsersService` into a domain service in the `user` domain
- Move `updateLeadRole` from `SyncProjectLeadsService` onto the `User` aggregate as `grantProjectLeadRole()` / `revokeProjectLeadRole()`
- Extract the project lead role assignment logic into the `user` domain and trigger it as a separate user application step after project lead sync
- Extract the duplicated worktime hour calculation and report assembly logic from `GetEmployeeWorkTimeService` and `GetProjectLeadWorkTimeService` into a single `WorkTimeReportAssembler` domain service

## Capabilities

### New Capabilities

- `monthend-employee-project-context`: Domain service that validates and assembles the employee–project context required for monthend operations (employee active, project active, employee assigned to project)
- `user-role-policy`: Domain service/policy that determines which roles a user should hold during sync, and domain aggregate operations for granting/revoking the PROJECT_LEAD role on `User`
- `worktime-report-assembler`: Domain service that aggregates raw attendance data into a `WorkTimeReport`, encapsulating billable/non-billable hour calculations

### Modified Capabilities

- `user-sync`: `SyncUsersService` delegates role determination to the new domain policy instead of implementing it inline
- `project-leads-sync`: `SyncProjectLeadsService` updates project lead assignments only and returns resolved lead ids for follow-up processing
- `project-lead-role-sync`: `SyncProjectLeadRolesService` applies `PROJECT_LEAD` role changes in the `user` subdomain using `UserRolePolicyService` and the `User` aggregate methods
- `worktime-report`: Both worktime application services delegate report assembly to the new domain service

## Impact

- `com.gepardec.mega.hexagon.monthend.domain.services` — new `MonthEndEmployeeProjectContextService`
- `com.gepardec.mega.hexagon.monthend.application` — `ResolveMonthEndEmployeeProjectContextService` removed; callers updated
- `com.gepardec.mega.hexagon.user.domain.services` — enhanced `UserRolePolicyService` centralising email-based and project-lead role policy
- `com.gepardec.mega.hexagon.user.domain.model.User` — new `grantProjectLeadRole()` and `revokeProjectLeadRole()` methods
- `com.gepardec.mega.hexagon.user.application.SyncUsersService` — delegates role building to domain
- `com.gepardec.mega.hexagon.user.application.SyncProjectLeadRolesService` — applies project lead role updates inside the user subdomain
- `com.gepardec.mega.hexagon.project.application.SyncProjectLeadsService` — syncs project leads without direct access to the user domain
- `com.gepardec.mega.hexagon.worktime.domain.services` — new `WorkTimeReportAssembler`
- `com.gepardec.mega.hexagon.worktime.application.GetEmployeeWorkTimeService` — delegates assembly to domain
- `com.gepardec.mega.hexagon.worktime.application.GetProjectLeadWorkTimeService` — delegates assembly to domain; duplicate logic removed
- No API or persistence changes
